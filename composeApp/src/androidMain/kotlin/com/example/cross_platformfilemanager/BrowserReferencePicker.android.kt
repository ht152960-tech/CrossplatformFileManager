package com.example.cross_platformfilemanager

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.result.ActivityResultLauncher
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

actual fun createBrowserReferencePicker(): BrowserReferencePicker? =
    AndroidBrowserReferencePickerHolder.picker

internal object AndroidBrowserReferencePickerHolder {
    var picker: AndroidBrowserReferencePicker? = null
        private set

    fun register(picker: AndroidBrowserReferencePicker) {
        this.picker = picker
    }

    fun unregister(picker: AndroidBrowserReferencePicker) {
        if (this.picker === picker) {
            this.picker = null
        }
    }
}

internal class AndroidBrowserReferencePicker(
    private val contentResolver: ContentResolver,
    private val launcher: ActivityResultLauncher<Intent>,
    private val pickerState: AndroidFilePickerViewModel,
) : BrowserReferencePicker {
    private var pendingContinuation: CancellableContinuation<BrowserReferenceDraft?>? = null

    override suspend fun pickReference(): BrowserReferenceDraft? =
        suspendCancellableCoroutine { continuation ->
            if (pickerState.beginSingleFilePick() == null) {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            pendingContinuation = continuation
            continuation.invokeOnCancellation {
                if (pendingContinuation === continuation) {
                    pendingContinuation = null
                }
            }
            try {
                launcher.launch(createOpenDocumentIntent())
            } catch (_: Exception) {
                if (pendingContinuation === continuation) {
                    pendingContinuation = null
                }
                pickerState.clearPending()
                if (continuation.isActive) {
                    continuation.resume(null)
                }
            }
        }

    fun onDocumentPicked(uri: Uri?): Boolean {
        val hadPendingPick = pickerState.consumePickerResult()
        val continuation = pendingContinuation
        pendingContinuation = null
        if (!hadPendingPick || continuation == null || !continuation.isActive) return false

        CoroutineScope(continuation.context).launch {
            val draft = uri?.let {
                withContext(Dispatchers.IO) {
                    runCatching { it.toReferenceDraft() }.getOrNull()
                }
            }
            if (continuation.isActive) {
                continuation.resume(draft)
            }
        }
        return true
    }

    fun cancelPendingPick() {
        val continuation = pendingContinuation
        pendingContinuation = null
        pickerState.clearPending()
        if (continuation?.isActive == true) {
            continuation.resume(null)
        }
    }

    private fun Uri.toReferenceDraft(): BrowserReferenceDraft {
        runCatching {
            contentResolver.takePersistableUriPermission(
                this,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }

        val metadata = queryMetadata()
        val displayName = metadata.displayName
            ?.takeIf { it.isNotBlank() }
            ?: lastPathSegment?.substringAfterLast('/')?.takeIf { it.isNotBlank() }
            ?: "Untitled file"
        val mimeType = runCatching { contentResolver.getType(this) }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: "application/octet-stream"

        return BrowserReferenceDraft(
            title = displayName,
            source = toString(),
            fileType = inferFileType(displayName, mimeType),
            fileSizeBytes = metadata.sizeBytes,
            notes = "Selected from Android system file picker. MIME type: $mimeType",
        )
    }

    private fun Uri.queryMetadata(): AndroidDocumentMetadata =
        runCatching {
            contentResolver.query(
                this,
                arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
                null,
                null,
                null,
            )?.use { cursor ->
                if (!cursor.moveToFirst()) return@use AndroidDocumentMetadata()

                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                AndroidDocumentMetadata(
                    displayName = nameIndex.takeIf { it >= 0 && !cursor.isNull(it) }
                        ?.let(cursor::getString),
                    sizeBytes = sizeIndex.takeIf { it >= 0 && !cursor.isNull(it) }
                        ?.let(cursor::getLong)
                        ?.takeIf { it >= 0L },
                )
            }
        }.getOrNull() ?: AndroidDocumentMetadata()

    private fun createOpenDocumentIntent(): Intent =
        Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, supportedOpenDocumentMimeTypes)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
}

private data class AndroidDocumentMetadata(
    val displayName: String? = null,
    val sizeBytes: Long? = null,
)

private val supportedOpenDocumentMimeTypes = arrayOf(
    "application/zip",
    "application/x-zip-compressed",
    "application/octet-stream",
    "image/*",
    "video/*",
    "audio/*",
    "application/pdf",
    "text/*",
    "application/msword",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "application/vnd.ms-excel",
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    "application/vnd.ms-powerpoint",
    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
    "*/*",
)

private fun inferFileType(displayName: String, mimeType: String): String {
    val extension = displayName.substringAfterLast('.', "").trim()
    if (extension.isNotBlank() && extension != displayName.trim()) {
        return extension.uppercase()
    }

    return mimeType.substringAfterLast('/', "FILE")
        .substringBefore(';')
        .trim()
        .takeIf { it.isNotBlank() && it != "*" }
        ?.uppercase()
        ?: "FILE"
}

package com.example.cross_platformfilemanager

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Build

/**
 * Android 平台信息实现。
 *
 * 提供平台名称和系统级外部打开能力。
 */
class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun isReferenceExternallyOpenable(reference: FileReference): Boolean =
    reference.source.trim().startsWith("content://", ignoreCase = true)

actual suspend fun openReferenceExternally(reference: FileReference): Boolean =
    openReferenceExternallyWithResult(reference).opened

actual suspend fun openReferenceExternallyWithResult(reference: FileReference): OpenReferenceResult {
    val context = AndroidContextHolder.applicationContext
        ?: return OpenReferenceResult(
            opened = false,
            message = "Android application context is unavailable.",
        )
    val source = reference.source.trim()
    if (source.isBlank()) {
        return OpenReferenceResult(
            opened = false,
            message = "File source is empty.",
        )
    }

    val uri = runCatching { Uri.parse(source) }.getOrElse { error ->
        return OpenReferenceResult(
            opened = false,
            message = "Invalid file Uri: ${error.message ?: error::class.simpleName}",
        )
    }
    if (!uri.scheme.equals("content", ignoreCase = true)) {
        return OpenReferenceResult(
            opened = false,
            message = "Unsupported Android file Uri scheme: ${uri.scheme ?: "none"}",
        )
    }

    val mimeType = runCatching { context.contentResolver.getType(uri) }
        .getOrNull()
        ?.takeIf { it.isNotBlank() }
        ?: mimeTypeFromFileType(reference.fileType)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType)
        clipData = ClipData.newUri(context.contentResolver, reference.title, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    return try {
        context.startActivity(Intent.createChooser(intent, reference.title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
        OpenReferenceResult(opened = true)
    } catch (error: ActivityNotFoundException) {
        OpenReferenceResult(
            opened = false,
            message = "No Android app can open MIME type $mimeType.",
        )
    } catch (error: SecurityException) {
        OpenReferenceResult(
            opened = false,
            message = "Read permission was denied for $uri.",
        )
    } catch (error: Exception) {
        OpenReferenceResult(
            opened = false,
            message = "Failed to open file: ${error.message ?: error::class.simpleName}",
        )
    }
}

private fun mimeTypeFromFileType(fileType: String): String =
    when (fileType.trim().lowercase()) {
        "pdf" -> "application/pdf"
        "txt", "text", "md", "markdown" -> "text/plain"
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "gif" -> "image/gif"
        "webp" -> "image/webp"
        "mp3" -> "audio/mpeg"
        "wav" -> "audio/wav"
        "mp4" -> "video/mp4"
        "doc" -> "application/msword"
        "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        "xls" -> "application/vnd.ms-excel"
        "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        "ppt" -> "application/vnd.ms-powerpoint"
        "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        "zip" -> "application/zip"
        else -> "*/*"
    }

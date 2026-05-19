package com.example.cross_platformfilemanager

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.swing.Swing
import java.io.File
import javax.swing.JFileChooser

actual fun createAppSnapshotStore(): AppSnapshotStore? = null

actual fun createLocalDataController(): LocalDataController? = null

actual fun createBrowserReferencePicker(): BrowserReferencePicker? = DesktopFileReferencePicker()

actual fun createBrowserReferenceResolver(): BrowserReferenceResolver? = null

private class DesktopFileReferencePicker : BrowserReferencePicker {
    override suspend fun pickReference(): BrowserReferenceDraft? = withContext(Dispatchers.Swing) {
        val chooser = JFileChooser().apply {
            dialogTitle = "Select file"
            fileSelectionMode = JFileChooser.FILES_ONLY
            isMultiSelectionEnabled = false
        }

        val result = chooser.showOpenDialog(null)
        if (result != JFileChooser.APPROVE_OPTION) {
            return@withContext null
        }

        val selectedFile = chooser.selectedFile ?: return@withContext null
        selectedFile.toReferenceDraft()
    }
}

private fun File.toReferenceDraft(): BrowserReferenceDraft {
    val absolutePath = absolutePath
    val fileName = name.ifBlank { "Untitled file" }
    val fileType = inferFileType(fileName)
    val sizeBytes = takeIf { exists() && isFile }?.length()?.takeIf { it >= 0L }
    val notes = buildList {
        add("Selected from desktop file picker.")
        if (sizeBytes != null) {
            add("Size: $sizeBytes bytes")
        }
        if (lastModified() > 0L) {
            add("Modified: ${lastModified()}")
        }
    }.joinToString(" | ")

    return BrowserReferenceDraft(
        title = fileName,
        source = absolutePath,
        fileType = fileType,
        fileSizeBytes = sizeBytes,
        notes = notes,
    )
}

private fun inferFileType(fileName: String): String {
    val extension = fileName.substringAfterLast('.', "").trim()
    if (extension.isBlank() || extension == fileName.trim()) {
        return "FILE"
    }
    return extension.uppercase()
}

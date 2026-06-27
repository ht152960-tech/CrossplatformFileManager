package com.example.cross_platformfilemanager.data.adapter

private val documentExtensions = setOf(
    "pdf",
    "doc",
    "docx",
    "xls",
    "xlsx",
    "ppt",
    "pptx",
    "txt",
    "md",
    "csv",
)

private val archiveExtensions = setOf("zip", "rar", "7z", "tar", "gz")
private val imageExtensions = setOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "heic")
private val videoExtensions = setOf("mp4", "mov", "avi", "mkv", "webm", "mpg", "mpeg")
private val audioExtensions = setOf("mp3", "wav", "flac", "m4a", "aac", "ogg")

fun inferTaggoFileCategory(
    extension: String?,
    mimeType: String?,
): String {
    val normalizedMimeType = mimeType?.trim()?.lowercase().orEmpty()
    when {
        normalizedMimeType.startsWith("image/") -> return "image"
        normalizedMimeType.startsWith("video/") -> return "video"
        normalizedMimeType.startsWith("audio/") -> return "audio"
    }

    return when (extension?.trim()?.removePrefix(".")?.lowercase()) {
        in documentExtensions -> "document"
        in archiveExtensions -> "archive"
        in imageExtensions -> "image"
        in videoExtensions -> "video"
        in audioExtensions -> "audio"
        else -> "other"
    }
}

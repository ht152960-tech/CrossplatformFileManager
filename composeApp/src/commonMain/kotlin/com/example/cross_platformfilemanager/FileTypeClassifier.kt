package com.example.cross_platformfilemanager

enum class FileTypeCategory {
    TextDocument,
    PdfDocument,
    Video,
    Audio,
    Image,
    Archive,
    Code,
    Spreadsheet,
    Presentation,
    Folder,
    Unknown,
}

object FileTypeClassifier {
    // 先把最常见、最容易被维护的人类文档类型单独收拢。
    private val textDocumentTokens = setOf("txt", "text", "md", "markdown", "log", "rtf", "doc", "docx")
    private val pdfTokens = setOf("pdf")
    private val videoTokens = setOf("mp4", "mkv", "mov", "avi", "webm", "video", "movie")
    private val audioTokens = setOf("mp3", "wav", "flac", "aac", "m4a", "ogg", "audio", "music", "song")
    private val imageTokens = setOf("png", "jpg", "jpeg", "gif", "webp", "bmp", "svg", "image", "photo", "picture")
    private val archiveTokens = setOf("zip", "rar", "7z", "tar", "gz", "archive", "compress")
    private val codeTokens = setOf(
        "kt", "kts", "java", "js", "ts", "py", "go", "c", "cpp", "h", "rs", "swift",
        "dart", "php", "rb", "sql", "html", "css", "json", "yaml", "yml", "xml", "code", "script",
    )
    private val spreadsheetTokens = setOf("xls", "xlsx", "csv", "sheet", "spreadsheet", "table")
    private val presentationTokens = setOf("ppt", "pptx", "presentation", "slide", "slides")
    private val folderTokens = setOf("folder", "dir", "directory")

    fun classify(reference: FileReference): FileTypeCategory {
        val tokens = referenceTokens(reference)
        return when {
            matchesAny(tokens, textDocumentTokens) -> FileTypeCategory.TextDocument
            matchesAny(tokens, pdfTokens) -> FileTypeCategory.PdfDocument
            matchesAny(tokens, videoTokens) -> FileTypeCategory.Video
            matchesAny(tokens, audioTokens) -> FileTypeCategory.Audio
            matchesAny(tokens, imageTokens) -> FileTypeCategory.Image
            matchesAny(tokens, archiveTokens) -> FileTypeCategory.Archive
            matchesAny(tokens, codeTokens) -> FileTypeCategory.Code
            matchesAny(tokens, spreadsheetTokens) -> FileTypeCategory.Spreadsheet
            matchesAny(tokens, presentationTokens) -> FileTypeCategory.Presentation
            matchesAny(tokens, folderTokens) -> FileTypeCategory.Folder
            else -> FileTypeCategory.Unknown
        }
    }

    private fun referenceTokens(reference: FileReference): Set<String> = buildSet {
        add(reference.fileType.trim().lowercase())
        reference.title.substringAfterLast('.', "").trim().lowercase().takeIf { it.isNotBlank() }?.let(::add)
        reference.source.substringAfterLast('.', "").trim().lowercase().takeIf { it.isNotBlank() }?.let(::add)
    }

    private fun matchesAny(tokens: Set<String>, candidates: Set<String>): Boolean =
        candidates.any { candidate ->
            tokens.any { token -> token == candidate || token.contains(candidate) }
        }
}

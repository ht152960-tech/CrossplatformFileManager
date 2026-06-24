package com.example.cross_platformfilemanager

/**
 * 文件大类。
 *
 * 这个分类既服务全部文件页的类型筛选，
 * 也服务推荐结果里的轻量多样性约束。
 */
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

/**
 * 基于文件类型、标题扩展名和来源扩展名的轻量分类器。
 *
 * 第一版只使用简单 token 规则，不依赖第三方库或平台能力，
 * 重点是为筛选和推荐提供稳定、可解释的类型信号。
 */
object FileTypeClassifier {
    // 先把最常见、最容易被误判的人类文档类型单独收拢。
    private val textDocumentTokens = setOf("txt", "text", "md", "markdown", "log", "rtf", "doc", "docx")
    private val pdfTokens = setOf("pdf")
    private val videoTokens = setOf("mp4", "mkv", "mov", "avi", "webm", "mpg", "mpeg", "mpe", "video", "movie")
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

    /**
     * 给单个文件条目归类。
     *
     * 分类顺序本身也是规则的一部分：
     * 一旦某类先命中，就不会继续落到后面的更宽泛类别中。
     */
    fun classify(reference: FileReference): FileTypeCategory {
        val tokens = referenceTokens(reference)
        return when {
            matchesAny(tokens, textDocumentTokens) -> FileTypeCategory.TextDocument
            matchesAny(tokens, pdfTokens) -> FileTypeCategory.PdfDocument
            matchesAny(tokens, audioTokens) -> FileTypeCategory.Audio
            matchesAny(tokens, videoTokens) -> FileTypeCategory.Video
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
        addTokenized(reference.fileType)
        addExtensionToken(reference.title)
        addExtensionToken(reference.source)
        addTokenized(reference.source)
    }

    private fun matchesAny(tokens: Set<String>, candidates: Set<String>): Boolean =
        candidates.any(tokens::contains)

    private fun MutableSet<String>.addExtensionToken(value: String) {
        value.substringAfterLast('.', "")
            .trim()
            .lowercase()
            .takeIf { it.isNotBlank() }
            ?.let(::add)
    }

    private fun MutableSet<String>.addTokenized(value: String) {
        value.trim()
            .lowercase()
            .split('/', '.', '-', '_', '+', ';', ' ', ':', '%', '?', '&', '=')
            .map(String::trim)
            .filter(String::isNotBlank)
            .forEach(::add)
    }
}

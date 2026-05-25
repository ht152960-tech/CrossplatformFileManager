package com.example.cross_platformfilemanager

enum class SearchTagSource {
    Input,
    LibraryTag,
}

data class SearchTag(
    val value: String,
    val source: SearchTagSource,
)

internal fun normalizeSearchTagToken(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.isBlank()) return ""
    val compact = buildString(trimmed.length) {
        trimmed.forEach { char ->
            when {
                char.isLetterOrDigit() || isCjk(char) -> append(char.lowercaseChar())
                char.isWhitespace() -> append(' ')
                else -> append(' ')
            }
        }
    }.trim()
    return compact.replace(Regex("\\s+"), " ")
}

internal fun tokenizeSubmittedSearch(raw: String): List<String> =
    raw.trim()
        .split(Regex("\\s+"))
        .map(::normalizeSearchTagToken)
        .filter { token ->
            when {
                token.isBlank() -> false
                token.all(::isCjk) && token.length < 2 -> false
                else -> true
            }
        }
        .distinct()

internal fun normalizeSearchField(raw: String): String {
    val normalized = normalizeSearchTagToken(raw)
    return normalized.replace(" ", "")
}

internal fun isCjk(char: Char): Boolean {
    val code = char.code
    return code in 0x4E00..0x9FFF ||
        code in 0x3400..0x4DBF ||
        code in 0xF900..0xFAFF
}

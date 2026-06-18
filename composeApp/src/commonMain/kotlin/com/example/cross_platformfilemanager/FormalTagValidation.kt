package com.example.cross_platformfilemanager

internal const val MAX_FORMAL_TAG_DISPLAY_WIDTH = 16
internal const val FORMAL_TAG_LENGTH_LIMIT_MESSAGE = "标签太长，请使用短词"

internal fun tagDisplayWidth(tag: String): Int {
    val trimmed = tag.trim()
    var width = 0
    var index = 0
    while (index < trimmed.length) {
        val current = trimmed[index]
        val next = trimmed.getOrNull(index + 1)
        if (current.isHighSurrogate() && next?.isLowSurrogate() == true) {
            width += 2
            index += 2
        } else {
            width += when {
                current.isEmojiJoinerOrVariationSelector() -> 0
                current.isWideDisplayChar() -> 2
                else -> 1
            }
            index++
        }
    }
    return width
}

internal fun isFormalTagLengthValid(tag: String): Boolean =
    tagDisplayWidth(tag) <= MAX_FORMAL_TAG_DISPLAY_WIDTH

internal fun formalTagsLengthValidationMessage(tagsText: String): String? =
    tagsText
        .split(",")
        .map { it.trim() }
        .firstOrNull { it.isNotEmpty() && !isFormalTagLengthValid(it) }
        ?.let { FORMAL_TAG_LENGTH_LIMIT_MESSAGE }

private fun Char.isWideDisplayChar(): Boolean =
    this in '\u1100'..'\u115F' ||
        this in '\u2329'..'\u232A' ||
        this in '\u2600'..'\u27BF' ||
        this in '\u2E80'..'\uA4CF' ||
        this in '\uAC00'..'\uD7A3' ||
        this in '\uF900'..'\uFAFF' ||
        this in '\uFE10'..'\uFE19' ||
        this in '\uFE30'..'\uFE6F' ||
        this in '\uFF00'..'\uFF60' ||
        this in '\uFFE0'..'\uFFE6'

private fun Char.isEmojiJoinerOrVariationSelector(): Boolean =
    this == '\u200D' || this in '\uFE00'..'\uFE0F'

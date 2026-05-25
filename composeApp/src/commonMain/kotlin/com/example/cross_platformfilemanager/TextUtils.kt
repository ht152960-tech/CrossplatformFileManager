package com.example.cross_platformfilemanager

import kotlin.time.Clock
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()

internal fun shouldShowInNewUploadList(file: FileReference, nowMillis: Long): Boolean {
    if (isSameLocalDate(file.createdAtMillis, nowMillis)) {
        return true
    }

    val ageMillis = nowMillis - file.createdAtMillis
    if (ageMillis >= NEW_UPLOAD_RETENTION_MILLIS) {
        return false
    }

    return file.lastOpenedAtMillis <= file.createdAtMillis
}

private fun isSameLocalDate(leftMillis: Long, rightMillis: Long): Boolean {
    if (leftMillis <= 0L || rightMillis <= 0L) return false
    val timeZone = TimeZone.currentSystemDefault()
    val leftDate = Instant.fromEpochMilliseconds(leftMillis).toLocalDateTime(timeZone).date
    val rightDate = Instant.fromEpochMilliseconds(rightMillis).toLocalDateTime(timeZone).date
    return leftDate == rightDate
}

internal fun normalize(value: String): String {
    val trimmed = value.trim().lowercase()
    if (trimmed.isEmpty()) return ""
    return trimmed
        .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
}

internal fun tokenize(value: String): List<String> {
    val normalized = normalize(value)
    if (normalized.isBlank()) return emptyList()
    return normalized.split(" ").filter { it.isNotBlank() }
}

internal fun formatTagFilter(label: String, tag: String): String = label.replace("%s", tag)

internal fun formatCount(label: String, count: Int): String = label.replace("%d", count.toString())

internal fun formatRelativeTime(millis: Long): String {
    if (millis <= 0L) return "never"
    val delta = Clock.System.now().toEpochMilliseconds() - millis
    if (delta < 0L) return "just now"
    val duration = delta.toDouble()
    return when {
        delta < 60_000L -> "just now"
        delta < 3_600_000L -> "${(duration / 60_000L).toInt()}m ago"
        delta < 86_400_000L -> "${(duration / 3_600_000L).toInt()}h ago"
        else -> "${(duration / 86_400_000L).toInt()}d ago"
    }
}

internal fun formatFileSize(bytes: Long?): String =
    when {
        bytes == null || bytes < 0L -> "unknown size"
        bytes < 1024L -> "$bytes B"
        bytes < 1024L * 1024L -> "${formatOneDecimal(bytes / 1024.0)} KB"
        bytes < 1024L * 1024L * 1024L -> "${formatOneDecimal(bytes / (1024.0 * 1024.0))} MB"
        else -> "${formatOneDecimal(bytes / (1024.0 * 1024.0 * 1024.0))} GB"
    }

private fun formatOneDecimal(value: Double): String {
    val rounded = (value * 10.0).roundToInt() / 10.0
    return if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
}

internal fun displayText(value: String): String {
    val trimmed = value.trim()
    if (trimmed.isBlank()) return trimmed

    if (looksLikeMojibake(trimmed)) {
        val repaired = tryRepairUtf8Mojibake(trimmed)
        if (looksMoreReadable(repaired, trimmed)) {
            return repaired
        }
    }

    return trimmed
}

@Suppress("UNUSED_PARAMETER")
internal fun displayTextForUi(value: String, fullCjkFontReady: Boolean): String {
    val repaired = displayText(value)
    return repaired
}

internal fun guessFileSizeFromNotes(notes: String): Long? {
    val match = fileSizeFromNotesPattern.find(notes) ?: return null
    val amount = match.groupValues.getOrNull(1)?.toDoubleOrNull() ?: return null
    val unit = match.groupValues.getOrNull(2)?.uppercase() ?: return null
    val multiplier = when (unit) {
        "B" -> 1.0
        "KB" -> 1024.0
        "MB" -> 1024.0 * 1024.0
        "GB" -> 1024.0 * 1024.0 * 1024.0
        else -> return null
    }
    return (amount * multiplier).roundToLong().coerceAtLeast(0L)
}

private val fileSizeFromNotesPattern =
    Regex("""(?:^|\|\s*)Size:\s*([\d.]+)\s*(B|KB|MB|GB)""", RegexOption.IGNORE_CASE)

private val mojibakeMarkers = setOf(
    '鍒', '鍏', '鍚', '鍙', '鍗', '鍜', '鍨', '鍦', '鍫', '鏂',
    '鏄', '鏁', '鏍', '鏉', '鏌', '鏋', '鐢', '鐩', '鐪', '鐫',
    '鐯', '鎵', '鎷', '鎺', '鎻', '鎿', '杩', '娣', '娓', '瀛',
    '绛', '绫', '绯', '绱', '绲', '绾', '绗', '缁', '缃', '钘',
    '鈥', '銆', '閰', '閿', '閫', '閮', '閰', '闈', '闊', '闆',
)

private const val NEW_UPLOAD_RETENTION_MILLIS = 48L * 60L * 60L * 1000L

private fun looksLikeMojibake(value: String): Boolean =
    value.any { it.code in 0x80..0xFF } || value.any { it in mojibakeMarkers }

private fun tryRepairUtf8Mojibake(value: String): String {
    val bytes = ByteArray(value.length) { index -> value[index].code.toByte() }
    return runCatching { bytes.decodeToString() }.getOrDefault(value)
}

private fun looksMoreReadable(candidate: String, original: String): Boolean {
    if (candidate == original) return false
    return readabilityScore(candidate) > readabilityScore(original)
}

private fun containsCjk(value: String): Boolean =
    value.any { char ->
        val code = char.code
        code in 0x3400..0x4DBF ||
            code in 0x4E00..0x9FFF ||
            code in 0xF900..0xFAFF
    }

private fun readabilityScore(value: String): Int =
    value.sumOf { char ->
        when {
            char.code in 0x4E00..0x9FFF -> 3
            char.code in 0x20..0x7E -> 1
            char == '\uFFFD' -> -5
            char in mojibakeMarkers -> -2
            else -> 0
        }
    }

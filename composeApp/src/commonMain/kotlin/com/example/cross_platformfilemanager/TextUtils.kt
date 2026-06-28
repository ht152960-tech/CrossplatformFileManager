package com.example.cross_platformfilemanager

import kotlin.time.Clock
import kotlin.math.roundToInt
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * 统一提供界面层常用的文本、时间、体积格式化能力。
 *
 * 这类函数本身很薄，但会被多个页面和平台桥接层重复使用，因此集中放在这里。
 */
internal fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()

/**
 * 判断文件是否还应出现在“新上传”列表。
 *
 * 规则分两层：
 * 1. 当天创建的文件直接保留；
 * 2. 48 小时内且尚未被再次打开的文件，也暂时保留在列表里。
 */
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

/**
 * 把用户输入归一化成便于搜索和比较的形式。
 */
internal fun normalize(value: String): String {
    val trimmed = value.trim().lowercase()
    if (trimmed.isEmpty()) return ""
    return trimmed
        .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
}

/**
 * 基于归一化结果切词，供标签、标题等简单搜索匹配使用。
 */
internal fun tokenize(value: String): List<String> {
    val normalized = normalize(value)
    if (normalized.isBlank()) return emptyList()
    return normalized.split(" ").filter { it.isNotBlank() }
}

internal fun formatTagFilter(label: String, tag: String): String = label.replace("%s", tag)

internal fun formatCount(label: String, count: Int): String = label.replace("%d", count.toString())

/**
 * 把时间戳格式化成首页和详情页更易扫读的相对时间文本。
 */
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

/**
 * 把字节数格式化成人类可读的体积文本。
 */
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

/**
 * 尝试把明显的乱码文本修复成可读内容，再交给界面展示。
 */
internal fun displayText(value: String): String {
    val trimmed = value.trim()
    if (trimmed.isBlank()) return trimmed

    // 部分平台拿到的历史文本会出现 UTF-8/本地编码错配，这里做一次轻量修复。
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
    // 按单字节序列回收后再尝试 UTF-8 解码，适合处理“UTF-8 被按 ANSI 读入”的场景。
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

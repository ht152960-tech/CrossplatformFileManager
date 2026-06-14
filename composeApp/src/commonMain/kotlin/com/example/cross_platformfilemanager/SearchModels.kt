package com.example.cross_platformfilemanager

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 搜索标签的来源。
 *
 * 区分“用户输入”与“标签库点击”，
 * 便于后续在排序、展示或埋点层保留来源信息。
 */
@Serializable
enum class SearchTagSource {
    @SerialName("Input")
    Input,
    @SerialName("LibraryTag")
    LibraryTag,
}

/**
 * 单个搜索标签。
 *
 * 搜索流程不会直接依赖原始查询串，而是把查询拆成若干标签 token，
 * 供仓储层统一匹配文件名、标签、类型和路径。
 */
@Serializable
data class SearchTag(
    val value: String,
    val source: SearchTagSource,
)

/**
 * 规范化搜索 token。
 *
 * 这里会统一大小写、清理标点并压缩空白，
 * 让不同输入方式最终落到同一种可比较形式上。
 */
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

/**
 * 把用户提交的整段搜索拆成可参与匹配的 token 列表。
 *
 * 这里会滤掉空 token 和过短的中文单字 token，
 * 避免过弱信号把搜索结果噪声放大。
 */
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

/**
 * 把字段值规范化成无空格比较形式。
 *
 * 搜索匹配时希望“标题空格差异”不要影响命中结果，
 * 所以这里在 token 规范化之后进一步去掉空格。
 */
internal fun normalizeSearchField(raw: String): String {
    val normalized = normalizeSearchTagToken(raw)
    return normalized.replace(" ", "")
}

/**
 * 判断字符是否属于常见中日韩统一表意文字范围。
 *
 * 这个判断主要服务搜索 token 的清洗和最短长度约束。
 */
internal fun isCjk(char: Char): Boolean {
    val code = char.code
    return code in 0x4E00..0x9FFF ||
        code in 0x3400..0x4DBF ||
        code in 0xF900..0xFAFF
}

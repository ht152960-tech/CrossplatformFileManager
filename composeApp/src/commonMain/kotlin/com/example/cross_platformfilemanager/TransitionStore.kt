package com.example.cross_platformfilemanager

/**
 * 后继关系的可持久化快照。
 *
 * `counts` 保存“某文件之后打开另一个文件”的次数，
 * `totals` 保存某个起点文件累计产生过多少次后继跳转。
 */
data class TransitionSnapshot(
    val counts: Map<String, Map<String, Int>>,
    val totals: Map<String, Int>,
)

/**
 * 管理文件后继关系样本的内存 Store。
 *
 * 它负责统计“打开 A 后接着打开 B”的历史次数，
 * 供推荐引擎在存在上下文文件时计算后继关系分。
 */
class TransitionStore {
    private val transitionCounts = mutableMapOf<String, MutableMap<String, Int>>()
    private val transitionTotals = mutableMapOf<String, Int>()

    /**
     * 累计一次后继关系样本。
     *
     * 只有起点和终点都有效且不相同时，才把它视为有意义的后继跳转。
     */
    fun recordTransition(fromFileId: String, toFileId: String) {
        val from = fromFileId.trim()
        val to = toFileId.trim()
        if (from.isBlank() || to.isBlank() || from == to) return

        val bucket = transitionCounts.getOrPut(from) { mutableMapOf() }
        bucket[to] = (bucket[to] ?: 0) + 1
        transitionTotals[from] = (transitionTotals[from] ?: 0) + 1
    }

    fun transitionCount(fromFileId: String, toFileId: String): Int =
        transitionCounts[fromFileId.trim()]?.get(toFileId.trim()) ?: 0

    fun totalTransitionsFrom(fromFileId: String): Int = transitionTotals[fromFileId.trim()] ?: 0

    /**
     * 计算“从当前文件跳到候选文件”的后继关系分。
     *
     * 得分以历史命中率为基础，并加上轻量先验和平滑处理，
     * 避免只命中一两次就把某个候选文件误判成强后继关系。
     */
    fun transitionScore(fromFileId: String?, toFileId: String): Double {
        val from = fromFileId?.trim().orEmpty()
        val total = transitionTotals[from] ?: return 0.0
        if (total <= 0) return 0.0
        val count = (transitionCounts[from]?.get(toFileId.trim()) ?: 0).toDouble()
        // 用很轻的先验压平稀疏转移，避免 1 次命中就直接变成强后继关系。
        val rawScore = (count + 0.5) / (total.toDouble() + 1.0)
        val confidence = transitionConfidence(total)
        // 样本很少时，命中率容易被偶然行为放大，所以先做一次保守平滑。
        return ((0.15 * (1.0 - confidence)) + (rawScore * confidence)).coerceIn(0.0, 1.0)
    }

    fun snapshot(): TransitionSnapshot = TransitionSnapshot(
        counts = transitionCounts.mapValues { (_, bucket) -> bucket.toMap() },
        totals = transitionTotals.toMap(),
    )

    fun restore(snapshot: TransitionSnapshot) {
        transitionCounts.clear()
        snapshot.counts.forEach { (from, bucket) ->
            transitionCounts[from] = bucket.toMutableMap()
        }
        transitionTotals.clear()
        transitionTotals.putAll(snapshot.totals)
    }

    fun clear() {
        transitionCounts.clear()
        transitionTotals.clear()
    }

    private fun transitionConfidence(totalTransitionsFrom: Int): Double = when {
        totalTransitionsFrom <= 1 -> 0.35
        totalTransitionsFrom == 2 -> 0.55
        totalTransitionsFrom == 3 -> 0.75
        totalTransitionsFrom == 4 -> 0.9
        else -> 1.0
    }
}

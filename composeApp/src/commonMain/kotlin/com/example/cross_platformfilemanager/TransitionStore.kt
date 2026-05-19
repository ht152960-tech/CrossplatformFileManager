package com.example.cross_platformfilemanager

data class TransitionSnapshot(
    val counts: Map<String, Map<String, Int>>,
    val totals: Map<String, Int>,
)

class TransitionStore {
    private val transitionCounts = mutableMapOf<String, MutableMap<String, Int>>()
    private val transitionTotals = mutableMapOf<String, Int>()

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

    fun transitionScore(fromFileId: String?, toFileId: String): Double {
        val from = fromFileId?.trim().orEmpty()
        val total = transitionTotals[from] ?: return 0.0
        if (total <= 0) return 0.0
        val count = (transitionCounts[from]?.get(toFileId.trim()) ?: 0).toDouble()
        // 用一个很轻的先验把稀疏转移压平，避免 1 次命中就直接变成“强后继”。
        val rawScore = (count + 0.5) / (total.toDouble() + 1.0)
        val confidence = transitionConfidence(total)
        // 样本很少时，A->B 的命中率容易被偶然点击放大，所以先做保守平滑。
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

package com.example.cross_platformfilemanager.domain.recommendation

data class AfterOpenPathConfig(
    val maxGapMs: Long = 30L * 60L * 1000L,
    val maxContextSize: Int = 3,
)

data class AfterOpenPathSignal(
    val directSuccessor: Double = 0.0,
    val sequencePath: Double = 0.0,
    val matchedOrder: Int = 0,
    val confidence: Double = 0.0,
)

class AfterOpenPathModel(
    private val config: AfterOpenPathConfig = AfterOpenPathConfig(),
) {
    fun reconstruct(events: List<RecommendationOpenEvent>): List<List<RecommendationOpenEvent>> {
        val withSession = events
            .filter { !it.sessionId.isNullOrBlank() }
            .groupBy { it.sessionId.orEmpty() }
            .values
            .flatMap(::splitSession)
        val withoutSession = events
            .filter { it.sessionId.isNullOrBlank() }
            .map(::listOf)
        return (withSession + withoutSession)
            .sortedBy { path -> path.firstOrNull()?.occurredAtMs ?: Long.MAX_VALUE }
    }

    fun predict(
        events: List<RecommendationOpenEvent>,
        recentOpenFileIds: List<String>,
    ): Map<String, AfterOpenPathSignal> {
        val context = recentOpenFileIds.filter { it.isNotBlank() }.takeLast(config.maxContextSize)
        if (context.isEmpty()) return emptyMap()
        val paths = reconstruct(events)
        val signals = mutableMapOf<String, AfterOpenPathSignal>()
        for (order in 1..minOf(config.maxContextSize, context.size)) {
            val suffix = context.takeLast(order)
            val counts = mutableMapOf<String, Int>()
            paths.forEach { path ->
                val ids = path.map { it.fileId }
                for (index in 0 until (ids.size - order).coerceAtLeast(0)) {
                    if (ids.subList(index, index + order) == suffix) {
                        val nextFileId = ids[index + order]
                        counts[nextFileId] = counts.getOrElse(nextFileId) { 0 } + 1
                    }
                }
            }
            val totalMatches = counts.values.sum()
            if (totalMatches == 0) continue
            counts.forEach { (fileId, count) ->
                val probability = count.toDouble() / totalMatches.toDouble()
                val previous = signals[fileId] ?: AfterOpenPathSignal()
                signals[fileId] = if (order == 1) {
                    previous.copy(
                        directSuccessor = maxOf(previous.directSuccessor, probability),
                        matchedOrder = maxOf(previous.matchedOrder, order),
                        confidence = maxOf(previous.confidence, probability * ORDER_ONE_MULTIPLIER),
                    )
                } else {
                    val multiplier = if (order >= 3) ORDER_THREE_MULTIPLIER else ORDER_TWO_MULTIPLIER
                    val score = probability * multiplier
                    previous.copy(
                        sequencePath = maxOf(previous.sequencePath, score),
                        matchedOrder = maxOf(previous.matchedOrder, order),
                        confidence = maxOf(previous.confidence, score),
                    )
                }
            }
        }
        return signals
    }

    private fun splitSession(events: List<RecommendationOpenEvent>): List<List<RecommendationOpenEvent>> {
        val sorted = events.sortedBy { it.occurredAtMs }
        if (sorted.isEmpty()) return emptyList()
        val paths = mutableListOf<MutableList<RecommendationOpenEvent>>()
        sorted.forEach { event ->
            val current = paths.lastOrNull()
            val previous = current?.lastOrNull()
            if (previous == null || event.occurredAtMs - previous.occurredAtMs > config.maxGapMs) {
                paths += mutableListOf(event)
            } else {
                current += event
            }
        }
        return paths
    }

    private companion object {
        const val ORDER_ONE_MULTIPLIER = 0.65
        const val ORDER_TWO_MULTIPLIER = 0.85
        const val ORDER_THREE_MULTIPLIER = 1.0
    }
}

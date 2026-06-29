package com.example.cross_platformfilemanager.domain.recommendation

import com.example.cross_platformfilemanager.runtime.TaggoRuntimeFile
import kotlin.math.exp

data class ExtractedHomeFeatures(
    val vector: RecommendationFeatureVector,
    val periodicConfidence: Double,
    val estimatedPeriodMs: Long?,
)

class HomeRecommendationFeatureExtractor(
    private val periodDetector: DynamicPeriodDetector = DynamicPeriodDetector(),
) {
    fun extractAll(
        files: List<TaggoRuntimeFile>,
        history: RecommendationHistory,
        nowMs: Long,
        config: RecommendationLearningConfig,
    ): Map<String, ExtractedHomeFeatures> {
        val opensByFile = history.openEvents.groupBy { it.fileId }
        val detailsByFile = history.detailEvents.groupingBy { it.fileId }.eachCount()
        val failuresByFile = history.failedOpenEvents.groupingBy { it.fileId }.eachCount()
        val positivesByFile = history.feedbackEvents
            .filter { it.feedbackType == "positive_open" }
            .groupingBy { it.fileId }
            .eachCount()
        val penaltiesByFile = history.feedbackEvents
            .filter { it.feedbackType == "skipped_before_selected_file" }
            .groupingBy { it.fileId }
            .eachCount()
        val maxOpenCount = opensByFile.values.maxOfOrNull { it.size }?.coerceAtLeast(1) ?: 1
        val maxDetailCount = detailsByFile.values.maxOrNull()?.coerceAtLeast(1) ?: 1
        val maxFailureCount = failuresByFile.values.maxOrNull()?.coerceAtLeast(1) ?: 1
        val maxPositiveCount = positivesByFile.values.maxOrNull()?.coerceAtLeast(1) ?: 1
        val maxPenaltyCount = penaltiesByFile.values.maxOrNull()?.coerceAtLeast(1) ?: 1
        val filesById = files.associateBy { it.id }
        val recentOpenedFiles = history.openEvents
            .sortedByDescending { it.occurredAtMs }
            .mapNotNull { filesById[it.fileId] }
            .distinctBy { it.id }
            .take(RECENT_CONTEXT_FILE_COUNT)
        val recentTags = recentOpenedFiles.flatMap { it.tags }.map(::normalize).filter { it.isNotBlank() }.toSet()
        val recentTypes = recentOpenedFiles.map { normalize(it.taggoFileCategory) }.filter { it.isNotBlank() }

        return files.associate { file ->
            val opens = opensByFile[file.id].orEmpty().sortedBy { it.occurredAtMs }
            val periodic = periodDetector.detect(opens.map { it.occurredAtMs }, nowMs)
            val lastOpenAt = opens.lastOrNull()?.occurredAtMs
            val recency = lastOpenAt?.let {
                exp(-((nowMs - it).coerceAtLeast(0L).toDouble() / RECENCY_DECAY_MS))
            } ?: 0.0
            val manualSearchOpen = opens.count { open ->
                history.searchEvents.any { search ->
                    search.occurredAtMs <= open.occurredAtMs &&
                        open.occurredAtMs - search.occurredAtMs <= config.manualSearchWindowMs &&
                        sameSession(search.sessionId, open.sessionId) &&
                        queryMatchesFile(search.query, file)
                }
            }.coerceAtMost(1).toDouble()
            val normalizedTags = file.tags.map(::normalize).filter { it.isNotBlank() }.toSet()
            val tagAffinity = if (normalizedTags.isEmpty() || recentTags.isEmpty()) {
                0.0
            } else {
                normalizedTags.intersect(recentTags).size.toDouble() / normalizedTags.size.toDouble()
            }
            val type = normalize(file.taggoFileCategory)
            val weakType = if (type.isBlank() || recentTypes.isEmpty()) {
                0.0
            } else {
                recentTypes.count { it == type }.toDouble() / recentTypes.size.toDouble()
            }
            val coldStart = if (opens.isEmpty()) {
                val age = (nowMs - file.createdAtMs).coerceAtLeast(0L)
                (0.8 * exp(-(age.toDouble() / COLD_START_DECAY_MS)) + if (file.tags.isNotEmpty()) 0.2 else 0.0)
            } else {
                0.0
            }
            file.id to ExtractedHomeFeatures(
                vector = RecommendationFeatureVector(
                    periodic = periodic.score,
                    manualSearchOpen = manualSearchOpen,
                    recency = recency,
                    frequency = opens.size.toDouble() / maxOpenCount.toDouble(),
                    feedbackPositive = (positivesByFile[file.id] ?: 0).toDouble() / maxPositiveCount.toDouble(),
                    tagAffinity = tagAffinity,
                    detailInterest = (detailsByFile[file.id] ?: 0).toDouble() / maxDetailCount.toDouble(),
                    failedOpenIntent = (failuresByFile[file.id] ?: 0).toDouble() / maxFailureCount.toDouble(),
                    coldStart = coldStart,
                    feedbackPenalty = (penaltiesByFile[file.id] ?: 0).toDouble() / maxPenaltyCount.toDouble(),
                    weakTypeContext = weakType,
                ).normalized(),
                periodicConfidence = periodic.confidence,
                estimatedPeriodMs = periodic.estimatedPeriodMs,
            )
        }
    }

    private fun sameSession(left: String?, right: String?): Boolean =
        left != null && right != null && left == right

    private fun queryMatchesFile(query: String, file: TaggoRuntimeFile): Boolean {
        val tokens = normalize(query).split(Regex("\\s+")).filter { it.isNotBlank() }
        if (tokens.isEmpty()) return false
        val fields = listOf(file.displayName, file.taggoFileCategory) + file.tags
        return tokens.all { token -> fields.any { normalize(it).contains(token) } }
    }

    private fun normalize(value: String): String = value.trim().lowercase()

    private companion object {
        const val RECENCY_DECAY_MS = 7.0 * 86_400_000.0
        const val COLD_START_DECAY_MS = 30.0 * 86_400_000.0
        const val RECENT_CONTEXT_FILE_COUNT = 10
    }
}

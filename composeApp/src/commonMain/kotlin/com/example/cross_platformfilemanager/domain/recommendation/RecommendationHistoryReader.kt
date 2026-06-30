package com.example.cross_platformfilemanager.domain.recommendation

import com.example.cross_platformfilemanager.data.repository.BehaviorRepository
import com.example.cross_platformfilemanager.data.repository.RecommendationRecordRepository

data class RecommendationHistory(
    val openEvents: List<RecommendationOpenEvent>,
    val searchEvents: List<RecommendationSearchEvent>,
    val feedbackEvents: List<RecommendationFeedbackEvent>,
    val detailEvents: List<RecommendationIntentEvent> = emptyList(),
    val failedOpenEvents: List<RecommendationIntentEvent> = emptyList(),
)

data class RecommendationOpenEvent(
    val fileId: String,
    val fileReferenceId: String?,
    val occurredAtMs: Long,
    val sessionId: String?,
    val entryPoint: String?,
)

data class RecommendationSearchEvent(
    val query: String,
    val occurredAtMs: Long,
    val sessionId: String?,
)

data class RecommendationFeedbackEvent(
    val recommendationSetId: String,
    val fileId: String,
    val feedbackType: String,
    val occurredAtMs: Long,
    val rank: Int?,
    val mode: RecommendationMode? = null,
)

data class RecommendationIntentEvent(
    val fileId: String,
    val occurredAtMs: Long,
    val sessionId: String?,
    val entryPoint: String?,
)

class RecommendationHistoryReader(
    private val behaviorRepository: BehaviorRepository,
    private val recommendationRecordRepository: RecommendationRecordRepository,
) {
    suspend fun loadHistory(
        nowMs: Long,
        lookbackDays: Int = DEFAULT_HISTORY_LOOKBACK_DAYS,
    ): RecommendationHistory = RecommendationHistory(
        openEvents = loadOpenHistory(nowMs, lookbackDays),
        searchEvents = loadRecentSearchHistory(nowMs, lookbackDays),
        feedbackEvents = loadFeedbackHistory(nowMs, lookbackDays),
        detailEvents = loadIntentHistory(EVENT_VIEW_DETAIL, nowMs, lookbackDays),
        failedOpenEvents = loadIntentHistory(EVENT_OPEN_FAILED, nowMs, lookbackDays),
    )

    suspend fun loadOpenHistory(
        nowMs: Long,
        lookbackDays: Int = DEFAULT_HISTORY_LOOKBACK_DAYS,
    ): List<RecommendationOpenEvent> {
        val range = historyRange(nowMs, lookbackDays)
        return behaviorRepository
            .getEventsByTypeInRange(EVENT_OPEN_CONTENT, range.fromMs, range.toMs)
            .mapNotNull { event ->
                val fileId = event.fileId?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                RecommendationOpenEvent(
                    fileId = fileId,
                    fileReferenceId = event.fileReferenceId,
                    occurredAtMs = event.occurredAtMs,
                    sessionId = event.sessionId,
                    entryPoint = event.entryPoint,
                )
            }
    }

    suspend fun loadRecentSearchHistory(
        nowMs: Long,
        lookbackDays: Int = DEFAULT_SEARCH_LOOKBACK_DAYS,
    ): List<RecommendationSearchEvent> {
        val range = historyRange(nowMs, lookbackDays)
        return behaviorRepository
            .getEventsByTypeInRange(EVENT_SEARCH_SUBMIT, range.fromMs, range.toMs)
            .mapNotNull { event ->
                val query = event.searchQuery?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                RecommendationSearchEvent(
                    query = query,
                    occurredAtMs = event.occurredAtMs,
                    sessionId = event.sessionId,
                )
            }
    }

    suspend fun loadFeedbackHistory(
        nowMs: Long,
        lookbackDays: Int = DEFAULT_HISTORY_LOOKBACK_DAYS,
    ): List<RecommendationFeedbackEvent> {
        val range = historyRange(nowMs, lookbackDays)
        return recommendationRecordRepository
            .getFeedbackInRange(range.fromMs, range.toMs)
            .map { feedback ->
                val setType = recommendationRecordRepository
                    .getRecommendationSetById(feedback.recommendationSetId)
                    ?.setType
                RecommendationFeedbackEvent(
                    recommendationSetId = feedback.recommendationSetId,
                    fileId = feedback.fileId,
                    feedbackType = feedback.feedbackType,
                    occurredAtMs = feedback.createdAtMs,
                    rank = feedback.rankAtFeedback?.toInt(),
                    mode = setType.toRecommendationMode(),
                )
            }
    }

    private suspend fun loadIntentHistory(
        eventType: String,
        nowMs: Long,
        lookbackDays: Int,
    ): List<RecommendationIntentEvent> {
        val range = historyRange(nowMs, lookbackDays)
        return behaviorRepository
            .getEventsByTypeInRange(eventType, range.fromMs, range.toMs)
            .mapNotNull { event ->
                val fileId = event.fileId?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                RecommendationIntentEvent(
                    fileId = fileId,
                    occurredAtMs = event.occurredAtMs,
                    sessionId = event.sessionId,
                    entryPoint = event.entryPoint,
                )
            }
    }

    private fun String?.toRecommendationMode(): RecommendationMode? = when {
        this == null -> null
        contains(RecommendationMode.AFTER_OPEN.name, ignoreCase = true) -> RecommendationMode.AFTER_OPEN
        contains("after_open", ignoreCase = true) -> RecommendationMode.AFTER_OPEN
        contains(RecommendationMode.HOME_INITIAL.name, ignoreCase = true) -> RecommendationMode.HOME_INITIAL
        contains("home", ignoreCase = true) -> RecommendationMode.HOME_INITIAL
        else -> null
    }

    private fun historyRange(nowMs: Long, lookbackDays: Int): HistoryRange {
        val safeDays = lookbackDays.coerceAtLeast(0).toLong()
            .coerceAtMost(Long.MAX_VALUE / DAY_MILLIS)
        val lookbackMs = safeDays * DAY_MILLIS
        return HistoryRange(
            fromMs = (nowMs - lookbackMs).coerceAtLeast(0L),
            toMs = nowMs.coerceAtLeast(0L),
        )
    }

    private data class HistoryRange(
        val fromMs: Long,
        val toMs: Long,
    )

    private companion object {
        const val EVENT_OPEN_CONTENT = "open_content"
        const val EVENT_SEARCH_SUBMIT = "search_submit"
        const val EVENT_VIEW_DETAIL = "view_detail"
        const val EVENT_OPEN_FAILED = "open_failed"
        const val DEFAULT_HISTORY_LOOKBACK_DAYS = 90
        const val DEFAULT_SEARCH_LOOKBACK_DAYS = 30
        const val DAY_MILLIS = 86_400_000L
    }
}

package com.example.cross_platformfilemanager.domain.recommendation

import com.example.cross_platformfilemanager.data.model.TaggoBehaviorEvent
import com.example.cross_platformfilemanager.data.model.TaggoBehaviorSession
import com.example.cross_platformfilemanager.data.model.TaggoExplicitNeedSignal
import com.example.cross_platformfilemanager.data.model.TaggoRecommendationCandidateSnapshot
import com.example.cross_platformfilemanager.data.model.TaggoRecommendationContext
import com.example.cross_platformfilemanager.data.model.TaggoRecommendationFeedback
import com.example.cross_platformfilemanager.data.model.TaggoRecommendationSet
import com.example.cross_platformfilemanager.data.repository.BehaviorRepository
import com.example.cross_platformfilemanager.data.repository.RecommendationRecordRepository
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals

class RecommendationHistoryReaderTest {
    @Test
    fun readerIncludesOnlySuccessfulOpensSearchesAndFeedbackWithoutWriting() = runSuspend {
        val behavior = FakeBehaviorRepository(
            events = listOf(
                behaviorEvent("open_1", "open_content", 1_000L, fileId = "file_1"),
                behaviorEvent("detail_1", "view_detail", 1_100L, fileId = "file_2"),
                behaviorEvent("failed_1", "open_failed", 1_200L, fileId = "file_3"),
                behaviorEvent("search_1", "search_submit", 1_300L, searchQuery = "quarterly report"),
            ),
        )
        val recommendations = FakeRecommendationRecordRepository(
            feedback = listOf(
                TaggoRecommendationFeedback(
                    id = "feedback_1",
                    recommendationSetId = "set_1",
                    fileId = "file_1",
                    feedbackType = "positive_open",
                    rewardValue = 1.0,
                    rankAtFeedback = 2L,
                    createdAtMs = 1_400L,
                    behaviorEventId = "open_1",
                ),
            ),
        )

        val history = RecommendationHistoryReader(behavior, recommendations)
            .loadHistory(nowMs = 2_000L)

        assertEquals(listOf("file_1"), history.openEvents.map { it.fileId })
        assertEquals(listOf("quarterly report"), history.searchEvents.map { it.query })
        assertEquals(listOf("positive_open"), history.feedbackEvents.map { it.feedbackType })
        assertEquals(2, history.feedbackEvents.single().rank)
        assertEquals(0, behavior.writeCount)
        assertEquals(0, recommendations.writeCount)
    }

    private fun behaviorEvent(
        id: String,
        type: String,
        occurredAtMs: Long,
        fileId: String? = null,
        searchQuery: String? = null,
    ) = TaggoBehaviorEvent(
        id = id,
        sessionId = "session_1",
        occurredAtMs = occurredAtMs,
        eventType = type,
        screenName = "test",
        entryPoint = "test",
        fileId = fileId,
        fileReferenceId = fileId?.let { "ref_$it" },
        searchQuery = searchQuery,
        recommendationSetId = null,
        recommendationRank = null,
        durationMs = null,
        extraJson = null,
    )

    private fun runSuspend(block: suspend () -> Unit) {
        var outcome: Result<Unit>? = null
        block.startCoroutine(
            object : Continuation<Unit> {
                override val context = EmptyCoroutineContext

                override fun resumeWith(result: Result<Unit>) {
                    outcome = result
                }
            },
        )
        (outcome ?: error("Test block unexpectedly suspended")).getOrThrow()
    }

    private class FakeBehaviorRepository(
        private val events: List<TaggoBehaviorEvent>,
    ) : BehaviorRepository {
        var writeCount = 0

        override suspend fun startSession(session: TaggoBehaviorSession) {
            writeCount++
        }

        override suspend fun endSession(id: String, endedAtMs: Long) {
            writeCount++
        }

        override suspend fun getSessionById(id: String): TaggoBehaviorSession? = null

        override suspend fun recordEvent(event: TaggoBehaviorEvent) {
            writeCount++
        }

        override suspend fun getEventsForSession(sessionId: String) =
            events.filter { it.sessionId == sessionId }

        override suspend fun getEventsByTypeInRange(
            eventType: String,
            fromMs: Long,
            toMs: Long,
        ) = events.filter { it.eventType == eventType && it.occurredAtMs in fromMs..toMs }

        override suspend fun recordExplicitNeedSignal(signal: TaggoExplicitNeedSignal) {
            writeCount++
        }

        override suspend fun getExplicitNeedSignalsForSession(sessionId: String) =
            emptyList<TaggoExplicitNeedSignal>()
    }

    private class FakeRecommendationRecordRepository(
        private val feedback: List<TaggoRecommendationFeedback>,
    ) : RecommendationRecordRepository {
        var writeCount = 0

        override suspend fun addRecommendationContext(context: TaggoRecommendationContext) {
            writeCount++
        }

        override suspend fun getRecommendationContextById(id: String): TaggoRecommendationContext? = null

        override suspend fun addRecommendationSet(set: TaggoRecommendationSet) {
            writeCount++
        }

        override suspend fun getRecommendationSetById(id: String): TaggoRecommendationSet? = null

        override suspend fun addCandidateSnapshot(snapshot: TaggoRecommendationCandidateSnapshot) {
            writeCount++
        }

        override suspend fun getCandidateSnapshotsForSet(recommendationSetId: String) =
            emptyList<TaggoRecommendationCandidateSnapshot>()

        override suspend fun markCandidateSelected(
            recommendationSetId: String,
            fileId: String,
            selectedAtMs: Long,
        ) {
            writeCount++
        }

        override suspend fun addRecommendationFeedback(feedback: TaggoRecommendationFeedback) {
            writeCount++
        }

        override suspend fun getFeedbackForSet(recommendationSetId: String) =
            feedback.filter { it.recommendationSetId == recommendationSetId }

        override suspend fun getFeedbackInRange(fromMs: Long, toMs: Long) =
            feedback.filter { it.createdAtMs in fromMs..toMs }
    }
}

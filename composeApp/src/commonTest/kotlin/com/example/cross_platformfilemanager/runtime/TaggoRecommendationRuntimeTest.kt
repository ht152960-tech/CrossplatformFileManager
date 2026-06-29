package com.example.cross_platformfilemanager.runtime

import com.example.cross_platformfilemanager.data.adapter.TaggoClock
import com.example.cross_platformfilemanager.data.adapter.TaggoIdGenerator
import com.example.cross_platformfilemanager.data.model.TaggoRecommendationCandidateSnapshot
import com.example.cross_platformfilemanager.data.model.TaggoRecommendationContext
import com.example.cross_platformfilemanager.data.model.TaggoRecommendationFeedback
import com.example.cross_platformfilemanager.data.model.TaggoRecommendationSet
import com.example.cross_platformfilemanager.data.repository.RecommendationRecordRepository
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TaggoRecommendationRuntimeTest {
    @Test
    fun recommendationSetRecordsContextSetAndOneBasedCandidateSnapshots() = runSuspend {
        val repository = FakeRecommendationRecordRepository()
        val runtime = createRuntime(repository)

        val recorded = runtime.recordRecommendationSet(
            surface = "home_recommendations",
            trigger = "home_render",
            candidates = candidates().mapIndexed { index, candidate -> candidate.copy(rank = index + 10) },
        )

        assertNotNull(recorded)
        assertEquals("home_recommendations", repository.contexts.single().contextType)
        assertEquals("home_recommendations", repository.sets.single().setType)
        assertEquals("home_render", repository.sets.single().policyName)
        assertEquals(listOf(1L, 2L, 3L), repository.candidates.map { it.rank })
        assertEquals("""["interval_pattern"]""", repository.candidates.first().reasonsJson)
    }

    @Test
    fun successfulSelectionRecordsPositiveFeedbackAndOnlyEarlierCandidatesAsSkipped() = runSuspend {
        val repository = FakeRecommendationRecordRepository()
        val runtime = createRuntime(repository)
        val recorded = runtime.recordRecommendationSet(
            "home_recommendations",
            "home_render",
            candidates(),
        )

        runtime.recordSelectedFromRecommendation(
            recommendationSetId = recorded!!.setId,
            selectedFileId = "file_2",
            selectedRank = 2,
            openedSuccessfully = true,
            candidates = candidates(),
        )

        assertEquals(
            listOf("selected_file", "positive_open", "skipped_before_selected_file"),
            repository.feedback.map { it.feedbackType },
        )
        assertEquals("file_1", repository.feedback.last().fileId)
        assertFalse(repository.feedback.any { it.fileId == "file_3" })
        assertNotNull(repository.candidates.first { it.fileId == "file_2" }.selectedAtMs)
    }

    @Test
    fun failedOpenDoesNotRecordPositiveFeedback() = runSuspend {
        val repository = FakeRecommendationRecordRepository()
        val runtime = createRuntime(repository)
        val recorded = runtime.recordRecommendationSet(
            "home_recommendations",
            "home_render",
            candidates(),
        )

        runtime.recordSelectedFromRecommendation(
            recommendationSetId = recorded!!.setId,
            selectedFileId = "file_2",
            selectedRank = 2,
            openedSuccessfully = false,
            candidates = candidates(),
        )

        assertEquals(listOf("open_failed"), repository.feedback.map { it.feedbackType })
        assertNull(repository.candidates.first { it.fileId == "file_2" }.selectedAtMs)
        assertTrue(repository.feedback.none { it.feedbackType == "positive_open" })
    }

    private fun candidates() = listOf(
        RecommendationSnapshotInput("file_1", 1, 0.9, """["interval_pattern"]"""),
        RecommendationSnapshotInput("file_2", 2, 0.8, """["successor_relation"]"""),
        RecommendationSnapshotInput("file_3", 3, 0.7, """["recent_open"]"""),
    )

    private fun createRuntime(repository: RecommendationRecordRepository) = TaggoRecommendationRuntime(
        recommendationRecordRepository = repository,
        idGenerator = SequentialIds(),
        clock = IncrementingClock(),
        recommendationModelVersion = 1L,
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

    private class FakeRecommendationRecordRepository : RecommendationRecordRepository {
        val contexts = mutableListOf<TaggoRecommendationContext>()
        val sets = mutableListOf<TaggoRecommendationSet>()
        val candidates = mutableListOf<TaggoRecommendationCandidateSnapshot>()
        val feedback = mutableListOf<TaggoRecommendationFeedback>()

        override suspend fun addRecommendationContext(context: TaggoRecommendationContext) {
            contexts += context
        }

        override suspend fun getRecommendationContextById(id: String) = contexts.firstOrNull { it.id == id }

        override suspend fun addRecommendationSet(set: TaggoRecommendationSet) {
            sets += set
        }

        override suspend fun getRecommendationSetById(id: String) = sets.firstOrNull { it.id == id }

        override suspend fun addCandidateSnapshot(snapshot: TaggoRecommendationCandidateSnapshot) {
            candidates += snapshot
        }

        override suspend fun getCandidateSnapshotsForSet(recommendationSetId: String) =
            candidates.filter { it.recommendationSetId == recommendationSetId }

        override suspend fun markCandidateSelected(
            recommendationSetId: String,
            fileId: String,
            selectedAtMs: Long,
        ) {
            val index = candidates.indexOfFirst {
                it.recommendationSetId == recommendationSetId && it.fileId == fileId
            }
            if (index >= 0) candidates[index] = candidates[index].copy(selectedAtMs = selectedAtMs)
        }

        override suspend fun addRecommendationFeedback(feedback: TaggoRecommendationFeedback) {
            this.feedback += feedback
        }

        override suspend fun getFeedbackForSet(recommendationSetId: String) =
            feedback.filter { it.recommendationSetId == recommendationSetId }
    }

    private class IncrementingClock : TaggoClock {
        private var current = 1_000L
        override fun nowMs() = current++
    }

    private class SequentialIds : TaggoIdGenerator {
        private var current = 0
        private fun next(prefix: String) = "${prefix}_${++current}"

        override fun nextFileEntryId() = next("file")
        override fun nextFileReferenceId() = next("reference")
        override fun nextTagId(normalizedName: String) = next("tag")
        override fun nextRecentSearchId(normalizedQuery: String) = next("search")
        override fun nextBehaviorSessionId() = next("session")
        override fun nextBehaviorEventId() = next("event")
        override fun nextExplicitNeedSignalId() = next("signal")
        override fun nextRecommendationContextId() = next("context")
        override fun nextRecommendationSetId() = next("set")
        override fun nextRecommendationFeedbackId() = next("feedback")
    }
}

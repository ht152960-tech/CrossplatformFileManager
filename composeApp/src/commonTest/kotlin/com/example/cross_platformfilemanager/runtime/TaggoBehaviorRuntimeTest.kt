package com.example.cross_platformfilemanager.runtime

import com.example.cross_platformfilemanager.data.adapter.TaggoClock
import com.example.cross_platformfilemanager.data.adapter.TaggoIdGenerator
import com.example.cross_platformfilemanager.data.model.TaggoBehaviorEvent
import com.example.cross_platformfilemanager.data.model.TaggoBehaviorSession
import com.example.cross_platformfilemanager.data.model.TaggoExplicitNeedSignal
import com.example.cross_platformfilemanager.data.repository.BehaviorRepository
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TaggoBehaviorRuntimeTest {
    @Test
    fun sessionStartIsIdempotentAndSessionEndRecordsDuration() = runSuspend {
        val repository = FakeBehaviorRepository()
        val runtime = createRuntime(repository)

        runtime.startSessionIfNeeded()
        runtime.startSessionIfNeeded()
        runtime.endSession()

        assertEquals(1, repository.sessions.size)
        assertNotNull(repository.sessions.single().endedAtMs)
        assertEquals(listOf("session_start", "session_end"), repository.events.map { it.eventType })
        assertTrue(repository.events.last().durationMs!! >= 0L)
    }

    @Test
    fun recordsSearchDetailOpenSuccessAndOpenFailure() = runSuspend {
        val repository = FakeBehaviorRepository()
        val runtime = createRuntime(repository)

        runtime.recordSearchSubmit("Quarterly report")
        runtime.recordViewDetail(fileId = "file_1", entryPoint = "search_result")
        runtime.recordOpenContent(fileId = "file_1", fileReferenceId = "ref_1")
        runtime.recordOpenFailed(
            fileId = "file_2",
            fileReferenceId = "ref_2",
            errorMessage = "No application can open this file",
        )

        assertEquals(
            listOf("session_start", "search_submit", "view_detail", "open_content", "open_failed"),
            repository.events.map { it.eventType },
        )
        assertEquals("Quarterly report", repository.events[1].searchQuery)
        assertEquals("file_1", repository.events[2].fileId)
        assertEquals("ref_1", repository.events[3].fileReferenceId)
        assertTrue(repository.events[4].extraJson.orEmpty().contains("No application can open this file"))
        assertTrue(repository.events.all { it.sessionId == repository.sessions.single().id })
    }

    private fun createRuntime(repository: BehaviorRepository) = TaggoBehaviorRuntime(
        behaviorRepository = repository,
        idGenerator = SequentialIds(),
        clock = IncrementingClock(),
        platform = "common-test",
        appVersion = "test",
        databaseVersion = 1L,
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
        val completed = outcome ?: error("Test block unexpectedly suspended")
        completed.getOrThrow()
    }

    private class FakeBehaviorRepository : BehaviorRepository {
        val sessions = mutableListOf<TaggoBehaviorSession>()
        val events = mutableListOf<TaggoBehaviorEvent>()
        private val signals = mutableListOf<TaggoExplicitNeedSignal>()

        override suspend fun startSession(session: TaggoBehaviorSession) {
            sessions += session
        }

        override suspend fun endSession(id: String, endedAtMs: Long) {
            val index = sessions.indexOfFirst { it.id == id }
            if (index >= 0) sessions[index] = sessions[index].copy(endedAtMs = endedAtMs)
        }

        override suspend fun getSessionById(id: String): TaggoBehaviorSession? =
            sessions.firstOrNull { it.id == id }

        override suspend fun recordEvent(event: TaggoBehaviorEvent) {
            events += event
        }

        override suspend fun getEventsForSession(sessionId: String): List<TaggoBehaviorEvent> =
            events.filter { it.sessionId == sessionId }

        override suspend fun recordExplicitNeedSignal(signal: TaggoExplicitNeedSignal) {
            signals += signal
        }

        override suspend fun getExplicitNeedSignalsForSession(sessionId: String): List<TaggoExplicitNeedSignal> =
            signals.filter { it.sessionId == sessionId }
    }

    private class IncrementingClock : TaggoClock {
        private var current = 1_000L
        override fun nowMs(): Long = current++
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

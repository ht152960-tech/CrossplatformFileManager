package com.example.cross_platformfilemanager.data.db

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.cross_platformfilemanager.data.model.TaggoBehaviorEvent
import com.example.cross_platformfilemanager.data.model.TaggoBehaviorSession
import com.example.cross_platformfilemanager.data.model.TaggoExplicitNeedSignal
import com.example.cross_platformfilemanager.data.model.TaggoFileEntry
import com.example.cross_platformfilemanager.data.model.TaggoFileReference
import com.example.cross_platformfilemanager.data.model.TaggoRecentSearch
import com.example.cross_platformfilemanager.data.model.TaggoRecommendationCandidateSnapshot
import com.example.cross_platformfilemanager.data.model.TaggoRecommendationContext
import com.example.cross_platformfilemanager.data.model.TaggoRecommendationFeedback
import com.example.cross_platformfilemanager.data.model.TaggoRecommendationSet
import com.example.cross_platformfilemanager.data.model.TaggoTag
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TaggoDatabaseRepositorySmokeTest {
    @Test
    fun fileEntryRepositoryCompletesRealDatabaseRoundTrip() = withRepositories { repositories ->
        repositories.fileEntries.addFileEntry(fileEntry(), fileReference())

        assertEquals(listOf("file_1"), repositories.fileEntries.getActiveFileEntries().map { it.id })
        assertEquals("ref_1", repositories.fileEntries.getPrimaryReferenceForFile("file_1")?.id)

        repositories.fileEntries.updateReferenceAvailability(
            referenceId = "ref_1",
            referenceAvailable = false,
            lastVerifiedAtMs = 2_000,
            updatedAtMs = 2_000,
        )
        val unavailableReference = repositories.fileEntries.getPrimaryReferenceForFile("file_1")
        assertNotNull(unavailableReference)
        assertFalse(unavailableReference.referenceAvailable)
        assertEquals(2_000, unavailableReference.lastVerifiedAtMs)

        repositories.fileEntries.softDeleteFileEntry(
            id = "file_1",
            deletedAtMs = 3_000,
            updatedAtMs = 3_000,
        )
        assertTrue(repositories.fileEntries.getActiveFileEntries().isEmpty())
    }

    @Test
    fun tagRepositoryCompletesRealDatabaseRoundTrip() = withRepositories { repositories ->
        repositories.fileEntries.addFileEntry(fileEntry(), fileReference())
        repositories.tags.addTag(
            TaggoTag(
                id = "tag_1",
                name = "文学",
                normalizedName = "文学",
                createdAtMs = 1_000,
                updatedAtMs = 1_000,
                deletedAtMs = null,
            ),
        )

        assertEquals("tag_1", repositories.tags.findTagByNormalizedName("文学")?.id)

        repositories.tags.attachTagToFile("file_1", "tag_1", 1_100)
        assertEquals(listOf("tag_1"), repositories.tags.getTagsForFile("file_1").map { it.id })

        repositories.tags.detachTagFromFile("file_1", "tag_1")
        assertTrue(repositories.tags.getTagsForFile("file_1").isEmpty())
    }

    @Test
    fun searchHistoryRepositoryCompletesRealDatabaseRoundTrip() = withRepositories { repositories ->
        repositories.searchHistory.upsertRecentSearch(
            TaggoRecentSearch(
                id = "search_1",
                rawQuery = "文学",
                normalizedQuery = "文学",
                createdAtMs = 1_000,
                lastUsedAtMs = 1_000,
                useCount = 1,
            ),
        )

        assertEquals(
            listOf("search_1"),
            repositories.searchHistory.getRecentSearches().map { it.id },
        )

        repositories.searchHistory.deleteRecentSearch("search_1")
        assertTrue(repositories.searchHistory.getRecentSearches().isEmpty())
    }

    @Test
    fun behaviorRepositoryCompletesRealDatabaseRoundTrip() = withRepositories { repositories ->
        repositories.behavior.startSession(behaviorSession())
        assertEquals("session_1", repositories.behavior.getSessionById("session_1")?.id)

        repositories.behavior.recordEvent(behaviorEvent())
        assertEquals(
            listOf("event_1"),
            repositories.behavior.getEventsForSession("session_1").map { it.id },
        )
        assertEquals(
            listOf("event_1"),
            repositories.behavior
                .getEventsByTypeInRange("open_content", 1_000, 1_500)
                .map { it.id },
        )

        repositories.behavior.recordExplicitNeedSignal(
            TaggoExplicitNeedSignal(
                id = "signal_1",
                sessionId = "session_1",
                behaviorEventId = "event_1",
                signalType = "manual_content_open",
                signalValue = "file_1",
                createdAtMs = 1_200,
                consumedByRecommendationSetId = null,
            ),
        )
        assertEquals(
            listOf("signal_1"),
            repositories.behavior.getExplicitNeedSignalsForSession("session_1").map { it.id },
        )

        repositories.behavior.endSession("session_1", 2_000)
        assertEquals(2_000, repositories.behavior.getSessionById("session_1")?.endedAtMs)
    }

    @Test
    fun recommendationRepositoryCompletesRealDatabaseRoundTrip() =
        withRepositories { repositories ->
            repositories.fileEntries.addFileEntry(fileEntry(), fileReference())
            repositories.behavior.startSession(behaviorSession())
            repositories.behavior.recordEvent(behaviorEvent())

            repositories.recommendations.addRecommendationContext(
                TaggoRecommendationContext(
                    id = "context_1",
                    createdAtMs = 1_100,
                    contextType = "home",
                    sessionId = "session_1",
                    triggerFileId = "file_1",
                    searchQuery = null,
                    localHour = 9,
                    dayOfWeek = 1,
                    latitude = null,
                    longitude = null,
                    locationPrecision = null,
                ),
            )
            assertEquals(
                "context_1",
                repositories.recommendations
                    .getRecommendationContextById("context_1")
                    ?.id,
            )

            repositories.recommendations.addRecommendationSet(
                TaggoRecommendationSet(
                    id = "set_1",
                    contextId = "context_1",
                    generatedAtMs = 1_200,
                    setType = "home",
                    modelVersion = 1,
                    policyName = "smoke_test",
                    policyVersion = "1",
                ),
            )
            assertEquals(
                "set_1",
                repositories.recommendations.getRecommendationSetById("set_1")?.id,
            )

            repositories.recommendations.addCandidateSnapshot(
                TaggoRecommendationCandidateSnapshot(
                    recommendationSetId = "set_1",
                    fileId = "file_1",
                    rank = 1,
                    score = 0.9,
                    reasonsJson = """{"reason":"recent"}""",
                    featuresJson = """{"interval":1.0}""",
                    selectionType = "rule_based",
                    propensity = null,
                    selectedAtMs = null,
                ),
            )
            val candidates = repositories.recommendations.getCandidateSnapshotsForSet("set_1")
            assertEquals(listOf("file_1"), candidates.map { it.fileId })
            assertEquals(listOf(1L), candidates.map { it.rank })
            assertNull(candidates.single().selectedAtMs)

            repositories.recommendations.markCandidateSelected("set_1", "file_1", 1_500)
            assertEquals(
                1_500,
                repositories.recommendations
                    .getCandidateSnapshotsForSet("set_1")
                    .single()
                    .selectedAtMs,
            )

            repositories.recommendations.addRecommendationFeedback(
                TaggoRecommendationFeedback(
                    id = "feedback_1",
                    recommendationSetId = "set_1",
                    fileId = "file_1",
                    feedbackType = "open",
                    rewardValue = 1.0,
                    rankAtFeedback = 1,
                    createdAtMs = 1_600,
                    behaviorEventId = "event_1",
                ),
            )
            assertEquals(
                listOf("feedback_1"),
                repositories.recommendations.getFeedbackForSet("set_1").map { it.id },
            )
            assertEquals(
                listOf("feedback_1"),
                repositories.recommendations.getFeedbackInRange(1_500, 1_700).map { it.id },
            )
        }

    private fun withRepositories(
        block: suspend (TaggoDatabaseRepositories) -> Unit,
    ) {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        try {
            TaggoDatabase.Schema.create(driver)
            val repositories = createTaggoDatabaseRepositories(TaggoDatabase(driver))
            runBlocking {
                block(repositories)
            }
        } finally {
            driver.close()
        }
    }

    private fun fileEntry() = TaggoFileEntry(
        id = "file_1",
        displayName = "Novel.txt",
        extension = "txt",
        mimeType = "text/plain",
        taggoFileCategory = "document",
        sizeBytes = 128,
        createdAtMs = 1_000,
        updatedAtMs = 1_000,
        lastContentOpenedAtMs = null,
        contentOpenCount = 0,
        thumbnailState = "none",
        thumbnailReferenceValue = null,
        deletedAtMs = null,
    )

    private fun fileReference() = TaggoFileReference(
        id = "ref_1",
        fileId = "file_1",
        referenceType = "path",
        referenceValue = "C:/files/Novel.txt",
        referenceAvailable = true,
        platform = "jvm",
        createdAtMs = 1_000,
        updatedAtMs = 1_000,
        lastVerifiedAtMs = null,
        isPrimary = true,
    )

    private fun behaviorSession() = TaggoBehaviorSession(
        id = "session_1",
        startedAtMs = 1_000,
        endedAtMs = null,
        platform = "jvm",
        appVersion = "test",
        databaseVersion = 1,
        recommendationModelVersion = 1,
    )

    private fun behaviorEvent() = TaggoBehaviorEvent(
        id = "event_1",
        sessionId = "session_1",
        occurredAtMs = 1_100,
        eventType = "open_content",
        screenName = "test",
        entryPoint = "smoke_test",
        fileId = null,
        fileReferenceId = null,
        searchQuery = null,
        recommendationSetId = null,
        recommendationRank = null,
        durationMs = null,
        extraJson = null,
    )
}

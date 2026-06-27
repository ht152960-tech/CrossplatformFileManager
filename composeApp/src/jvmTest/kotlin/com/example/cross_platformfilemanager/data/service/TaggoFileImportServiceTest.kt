package com.example.cross_platformfilemanager.data.service

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.cross_platformfilemanager.data.adapter.TaggoClock
import com.example.cross_platformfilemanager.data.adapter.TaggoFileImportInput
import com.example.cross_platformfilemanager.data.adapter.TaggoIdGenerator
import com.example.cross_platformfilemanager.data.db.TaggoDatabase
import com.example.cross_platformfilemanager.data.db.TaggoDatabaseRepositories
import com.example.cross_platformfilemanager.data.db.createTaggoDatabaseRepositories
import com.example.cross_platformfilemanager.data.model.TaggoTag
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TaggoFileImportServiceTest {
    @Test
    fun importsFileReferenceAndDistinctTagsIntoDatabase() = withRepositories { repositories ->
        repositories.tags.addTag(
            TaggoTag(
                id = "existing_work",
                name = "Work",
                normalizedName = "work",
                createdAtMs = 500,
                updatedAtMs = 500,
                deletedAtMs = null,
            ),
        )
        val service = TaggoFileImportService(
            fileEntries = repositories.fileEntries,
            tags = repositories.tags,
            idGenerator = FixedTaggoIdGenerator,
            clock = FixedTaggoClock,
            platform = "android",
        )

        val mapping = service.importFile(
            TaggoFileImportInput(
                oldId = "old_file_1",
                displayName = "test.pdf",
                extension = "pdf",
                mimeType = "application/pdf",
                referenceValue = "content://file",
                sizeBytes = 1_024,
                tags = listOf(" 文学 ", "文学", "Work", "work", ""),
            ),
        )

        assertEquals("old_file_1", mapping.fileEntry.id)
        assertEquals("document", mapping.fileEntry.taggoFileCategory)
        assertEquals(0, mapping.fileEntry.contentOpenCount)
        assertNull(mapping.fileEntry.lastContentOpenedAtMs)
        assertEquals("reference_1", mapping.primaryReference.id)
        assertEquals("android_content_uri", mapping.primaryReference.referenceType)
        assertEquals(listOf("文学", "work"), mapping.tags.map { it.normalizedName })

        val storedEntry = repositories.fileEntries.getFileEntryById("old_file_1")
        assertNotNull(storedEntry)
        assertEquals(1_000, storedEntry.createdAtMs)
        val storedReference = repositories.fileEntries.getPrimaryReferenceForFile("old_file_1")
        assertNotNull(storedReference)
        assertEquals("reference_1", storedReference.id)
        assertTrue(storedReference.referenceAvailable)

        val storedTags = repositories.tags.getTagsForFile("old_file_1")
        assertEquals(setOf("文学", "work"), storedTags.map { it.normalizedName }.toSet())
        assertEquals("existing_work", storedTags.single { it.normalizedName == "work" }.id)
        assertEquals(2, repositories.tags.getActiveTags().size)
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

    private object FixedTaggoClock : TaggoClock {
        override fun nowMs(): Long = 1_000
    }

    private object FixedTaggoIdGenerator : TaggoIdGenerator {
        override fun nextFileEntryId(): String = "generated_file"

        override fun nextFileReferenceId(): String = "reference_1"

        override fun nextTagId(normalizedName: String): String = "tag_$normalizedName"

        override fun nextRecentSearchId(normalizedQuery: String): String = "search_1"

        override fun nextBehaviorSessionId(): String = "session_1"

        override fun nextBehaviorEventId(): String = "event_1"

        override fun nextExplicitNeedSignalId(): String = "signal_1"

        override fun nextRecommendationContextId(): String = "context_1"

        override fun nextRecommendationSetId(): String = "set_1"

        override fun nextRecommendationFeedbackId(): String = "feedback_1"
    }
}

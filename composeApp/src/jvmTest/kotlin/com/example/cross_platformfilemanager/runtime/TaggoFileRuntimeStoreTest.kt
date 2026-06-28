package com.example.cross_platformfilemanager.runtime

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.cross_platformfilemanager.data.adapter.TaggoClock
import com.example.cross_platformfilemanager.data.adapter.TaggoFileImportInput
import com.example.cross_platformfilemanager.data.adapter.TaggoIdGenerator
import com.example.cross_platformfilemanager.data.db.TaggoDatabase
import com.example.cross_platformfilemanager.data.db.createTaggoDatabaseRepositories
import com.example.cross_platformfilemanager.data.service.TaggoFileImportService
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TaggoFileRuntimeStoreTest {
    @Test
    fun addLoadsDatabaseFileAndSoftDeleteRemovesIt() = withStore { store, repositories ->
        val added = store.addFile(importInput())

        assertEquals("file_1", added.id)
        assertEquals("Report.pdf", added.displayName)
        assertEquals(listOf("work"), added.tags)
        assertEquals(listOf("file_1"), store.files.map { it.id })

        store.softDeleteFile("file_1")

        assertTrue(store.files.isEmpty())
        assertNotNull(repositories.fileEntries.getFileEntryById("file_1")?.deletedAtMs)
    }

    @Test
    fun tagChangesArePersistedAndRefreshRuntimeList() = withStore { store, repositories ->
        store.addFile(importInput(tags = emptyList()))

        assertTrue(store.addTag("file_1", "Important"))
        assertEquals(listOf("Important"), store.getFile("file_1")?.tags)
        assertEquals(listOf("Important"), repositories.tags.getTagsForFile("file_1").map { it.name })

        assertTrue(store.removeTag("file_1", "important"))
        assertTrue(store.getFile("file_1")?.tags.orEmpty().isEmpty())
    }

    @Test
    fun recentSearchUsesDatabaseAsSource() = withStore { store, repositories ->
        store.recordSearch("Quarterly report")
        store.recordSearch("Quarterly report")

        assertEquals(listOf("Quarterly report"), store.recentSearches)
        val stored = repositories.searchHistory.getRecentSearches().single()
        assertEquals(2, stored.useCount)
    }

    private fun withStore(
        block: suspend (TaggoFileRuntimeStore, com.example.cross_platformfilemanager.data.db.TaggoDatabaseRepositories) -> Unit,
    ) {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        try {
            TaggoDatabase.Schema.create(driver)
            val repositories = createTaggoDatabaseRepositories(TaggoDatabase(driver))
            val ids = SequentialIds()
            val clock = FixedClock()
            val service = TaggoFileImportService(
                fileEntries = repositories.fileEntries,
                tags = repositories.tags,
                idGenerator = ids,
                clock = clock,
                platform = "jvm-test",
            )
            val store = TaggoFileRuntimeStore(repositories, service, ids, clock)
            runBlocking { block(store, repositories) }
        } finally {
            driver.close()
        }
    }

    private fun importInput(tags: List<String> = listOf("work")) = TaggoFileImportInput(
        oldId = "file_1",
        displayName = "Report.pdf",
        extension = "pdf",
        mimeType = "application/pdf",
        referenceValue = "C:/files/Report.pdf",
        sizeBytes = 512,
        tags = tags,
    )

    private class FixedClock : TaggoClock {
        private var value = 1_000L
        override fun nowMs(): Long = value++
    }

    private class SequentialIds : TaggoIdGenerator {
        private var next = 0
        private fun id(prefix: String) = "${prefix}_${++next}"
        override fun nextFileEntryId() = id("file")
        override fun nextFileReferenceId() = "ref_1"
        override fun nextTagId(normalizedName: String) = id("tag")
        override fun nextRecentSearchId(normalizedQuery: String) = id("search")
        override fun nextBehaviorSessionId() = id("session")
        override fun nextBehaviorEventId() = id("event")
        override fun nextExplicitNeedSignalId() = id("signal")
        override fun nextRecommendationContextId() = id("context")
        override fun nextRecommendationSetId() = id("set")
        override fun nextRecommendationFeedbackId() = id("feedback")
    }
}

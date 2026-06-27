package com.example.cross_platformfilemanager.data.adapter

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TaggoDataAdapterTest {
    @Test
    fun infersReferenceTypeFromReferenceValue() {
        val cases = mapOf(
            "content://xxx" to "android_content_uri",
            "browser-handle:abc" to "web_file_handle",
            "browser-file:abc" to "web_file_token",
            "https://example.com/a.pdf" to "remote_url",
            """D:\test\a.pdf""" to "local_path",
            "/home/user/a.pdf" to "local_path",
            "custom://abc" to "external_uri",
            "plain-token" to "unknown_reference",
        )

        cases.forEach { (referenceValue, expectedType) ->
            assertEquals(expectedType, inferTaggoReferenceType(referenceValue))
        }
    }

    @Test
    fun infersTaggoFileCategoryFromMimeTypeOrExtension() {
        assertEquals("image", inferTaggoFileCategory(null, "image/png"))
        assertEquals("video", inferTaggoFileCategory(null, "video/mp4"))
        assertEquals("audio", inferTaggoFileCategory(null, "audio/mpeg"))
        assertEquals("document", inferTaggoFileCategory("pdf", null))
        assertEquals("archive", inferTaggoFileCategory("zip", null))
        assertEquals("image", inferTaggoFileCategory("HEIC", null))
        assertEquals("video", inferTaggoFileCategory("MPG", null))
        assertEquals("other", inferTaggoFileCategory("unknown", null))
    }

    @Test
    fun normalizesTagNameWithoutChangingHashPrefix() {
        assertEquals("文学", normalizeTagName(" 文学 "))
        assertEquals("work", normalizeTagName(" Work "))
        assertEquals("#文学", normalizeTagName("#文学"))
    }

    @Test
    fun createsFileImportMappingWithDeterministicIdentityAndTime() {
        val mapping = createTaggoFileImportMapping(
            input = TaggoFileImportInput(
                oldId = null,
                displayName = "test.pdf",
                extension = "pdf",
                mimeType = "application/pdf",
                referenceValue = "content://file",
                sizeBytes = 1_024,
                tags = listOf(" 文学 ", "文学", "Work", "work", ""),
            ),
            idGenerator = FixedTaggoIdGenerator,
            clock = FixedTaggoClock,
            platform = "android",
        )

        assertEquals("file_1", mapping.fileEntry.id)
        assertEquals("document", mapping.fileEntry.taggoFileCategory)
        assertEquals(0, mapping.fileEntry.contentOpenCount)
        assertNull(mapping.fileEntry.lastContentOpenedAtMs)
        assertEquals(1_000, mapping.fileEntry.createdAtMs)
        assertEquals(1_000, mapping.fileEntry.updatedAtMs)

        assertEquals("reference_1", mapping.primaryReference.id)
        assertEquals("file_1", mapping.primaryReference.fileId)
        assertEquals("android_content_uri", mapping.primaryReference.referenceType)
        assertTrue(mapping.primaryReference.referenceAvailable)
        assertTrue(mapping.primaryReference.isPrimary)
        assertEquals("android", mapping.primaryReference.platform)
        assertFalse(mapping.tags.isEmpty())
        assertEquals(listOf("文学", "work"), mapping.tags.map { it.normalizedName })
        assertEquals(listOf("文学", "Work"), mapping.tags.map { it.name })
        assertEquals(listOf("tag_文学", "tag_work"), mapping.tags.map { it.id })
    }

    private object FixedTaggoClock : TaggoClock {
        override fun nowMs(): Long = 1_000
    }

    private object FixedTaggoIdGenerator : TaggoIdGenerator {
        override fun nextFileEntryId(): String = "file_1"

        override fun nextFileReferenceId(): String = "reference_1"

        override fun nextTagId(normalizedName: String): String = "tag_$normalizedName"

        override fun nextRecentSearchId(normalizedQuery: String): String = "search_$normalizedQuery"

        override fun nextBehaviorSessionId(): String = "session_1"

        override fun nextBehaviorEventId(): String = "event_1"

        override fun nextExplicitNeedSignalId(): String = "signal_1"

        override fun nextRecommendationContextId(): String = "context_1"

        override fun nextRecommendationSetId(): String = "set_1"

        override fun nextRecommendationFeedbackId(): String = "feedback_1"
    }
}

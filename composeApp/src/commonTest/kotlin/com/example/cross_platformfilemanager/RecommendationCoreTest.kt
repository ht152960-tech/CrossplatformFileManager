package com.example.cross_platformfilemanager

import com.example.cross_platformfilemanager.runtime.TaggoRuntimeFile
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecommendationCoreTest {
    @Test
    fun intervalScoreRisesWithRepeatedFiveDayOpenings() {
        val store = FilePatternStore()
        val day = 86_400_000L
        store.recordOpen(FileOpenLog("file", 0L))
        val before = store.intervalScore("file", 5L * day)
        listOf(5L, 10L, 15L, 20L).forEach {
            store.recordOpen(FileOpenLog("file", it * day))
        }
        assertTrue(store.intervalScore("file", 25L * day) > before)
    }

    @Test
    fun transitionHistoryPushesSuccessorToTop() {
        val engine = RecommendationEngine()
        val day = 86_400_000L
        repeat(5) { index ->
            val openedAt = index * day
            engine.recordFileOpen("A", openedAt, null)
            engine.recordFileOpen("B", openedAt + day / 10, "A")
        }

        val result = engine.recommend(
            references = listOf(
                runtimeFile("B", lastOpenedAtMs = 5L * day),
                runtimeFile("C", lastOpenedAtMs = 5L * day),
                runtimeFile("A", lastOpenedAtMs = 5L * day),
            ),
            previousFileId = "A",
            nowMillis = 6L * day,
        )

        assertEquals("B", result.first().file.id)
    }

    @Test
    fun sparseTransitionScoreRemainsConservative() {
        val store = TransitionStore()
        store.recordTransition("A", "B")
        val score = store.transitionScore("A", "B")
        assertTrue(score in 0.0..0.5)
    }

    @Test
    fun uiSnapshotExcludesDatabaseAndRecommendationBusinessState() {
        val snapshot = AppSnapshot(
            locale = AppLocale.EnUs,
            query = "report",
            searchTags = listOf(SearchTag("report", SearchTagSource.Input)),
            selectedTag = null,
            selectedFileType = null,
            activeReferenceId = "B",
        )

        val encoded = SnapshotCodec.encode(snapshot)
        val decoded = requireNotNull(SnapshotCodec.decode(encoded))
        assertEquals(11, Json.parseToJsonElement(encoded).jsonObject
            .getValue("schemaVersion").jsonPrimitive.content.toInt())
        assertEquals(snapshot, decoded)
        assertTrue(!encoded.contains("\"references\""))
        assertTrue(!encoded.contains("\"recentSearches\""))
        assertTrue(!encoded.contains("\"recommendationState\""))
    }

    @Test
    fun snapshotCodecRejectsInvalidPayloads() {
        assertEquals(null, SnapshotCodec.decode(""))
        assertEquals(null, SnapshotCodec.decode("not-json"))
    }

    @Test
    fun fileTypeClassifierUsesRuntimeMetadata() {
        assertEquals(FileTypeCategory.PdfDocument, FileTypeClassifier.classify(runtimeFile("guide.pdf")))
        assertEquals(FileTypeCategory.Video, FileTypeClassifier.classify(runtimeFile("clip.mp4")))
        assertEquals(FileTypeCategory.Archive, FileTypeClassifier.classify(runtimeFile("bundle.zip")))
    }

    @Test
    fun recommendationDeduplicatesSamePrimaryReference() {
        val engine = RecommendationEngine()
        val result = engine.recommend(
            references = listOf(
                runtimeFile("one", referenceValue = "C:/same.txt"),
                runtimeFile("two", referenceValue = "C:/same.txt"),
                runtimeFile("three", referenceValue = "C:/other.txt"),
            ),
            previousFileId = null,
            nowMillis = 10_000,
            limit = 10,
        )
        assertEquals(2, result.size)
    }

    private fun runtimeFile(
        id: String,
        referenceValue: String = "C:/files/$id",
        lastOpenedAtMs: Long? = null,
    ): TaggoRuntimeFile {
        val extension = id.substringAfterLast('.', "").takeIf { it.isNotBlank() }
        return TaggoRuntimeFile(
            id = id,
            displayName = id,
            extension = extension,
            mimeType = null,
            taggoFileCategory = extension?.uppercase() ?: "FILE",
            sizeBytes = 128,
            primaryReferenceId = "ref-$id",
            referenceType = "path",
            referenceValue = referenceValue,
            referenceAvailable = true,
            platform = "test",
            tags = listOf("test"),
            createdAtMs = 1_000,
            updatedAtMs = 1_000,
            lastContentOpenedAtMs = lastOpenedAtMs,
            contentOpenCount = if (lastOpenedAtMs == null) 0 else 1,
            thumbnailState = "none",
            thumbnailReferenceValue = null,
        )
    }
}

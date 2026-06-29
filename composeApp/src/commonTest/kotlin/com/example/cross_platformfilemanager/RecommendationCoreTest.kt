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

    private fun runtimeFile(id: String): TaggoRuntimeFile {
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
            referenceValue = "C:/files/$id",
            referenceAvailable = true,
            platform = "test",
            tags = listOf("test"),
            createdAtMs = 1_000,
            updatedAtMs = 1_000,
            lastContentOpenedAtMs = null,
            contentOpenCount = 0,
            thumbnailState = "none",
            thumbnailReferenceValue = null,
        )
    }
}

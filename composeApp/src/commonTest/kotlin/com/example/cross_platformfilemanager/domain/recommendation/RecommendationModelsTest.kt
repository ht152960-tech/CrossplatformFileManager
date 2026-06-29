package com.example.cross_platformfilemanager.domain.recommendation

import com.example.cross_platformfilemanager.ScoredRecommendation
import com.example.cross_platformfilemanager.runtime.TaggoRuntimeFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RecommendationModelsTest {
    @Test
    fun modesSeparateInitialHomeFromSuccessfulOpenContext() {
        val context = RecommendationRequestContext()

        val initial = context.createRequest(nowMs = 1_000L, limit = 10)
        context.recordOpenContent("file_1")
        val afterOpen = context.createRequest(nowMs = 2_000L, limit = 5)

        assertEquals(RecommendationMode.HOME_INITIAL, initial.mode)
        assertNull(initial.triggerFileId)
        assertEquals(RecommendationMode.AFTER_OPEN, afterOpen.mode)
        assertEquals("file_1", afterOpen.triggerFileId)
    }

    @Test
    fun legacyResultKeepsFileIdFinalScoreAndLegacyScoreParts() {
        val legacy = ScoredRecommendation(
            file = runtimeFile("file_1"),
            intervalScore = 0.7,
            transitionScore = 0.5,
            recencyScore = 0.3,
            finalScore = 1.25,
        )

        val result = legacy.toRecommendationResult(RecommendationMode.HOME_INITIAL)

        assertEquals("file_1", result.fileId)
        assertEquals(1.25, result.finalScore)
        assertEquals(0.7, result.scoreParts.legacyIntervalScore)
        assertEquals(0.5, result.scoreParts.legacyTransitionScore)
        assertEquals(0.3, result.scoreParts.legacyRecencyScore)
        assertEquals(RecommendationMode.HOME_INITIAL, result.mode)
        assertNull(result.featuresJson)
    }

    private fun runtimeFile(id: String) = TaggoRuntimeFile(
        id = id,
        displayName = "$id.pdf",
        extension = "pdf",
        mimeType = "application/pdf",
        taggoFileCategory = "PDF",
        sizeBytes = 128L,
        primaryReferenceId = "ref_$id",
        referenceType = "path",
        referenceValue = "C:/files/$id.pdf",
        referenceAvailable = true,
        platform = "test",
        tags = listOf("test"),
        createdAtMs = 1_000L,
        updatedAtMs = 1_000L,
        lastContentOpenedAtMs = null,
        contentOpenCount = 0L,
        thumbnailState = "none",
        thumbnailReferenceValue = null,
    )
}

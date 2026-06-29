package com.example.cross_platformfilemanager.domain.recommendation

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
}

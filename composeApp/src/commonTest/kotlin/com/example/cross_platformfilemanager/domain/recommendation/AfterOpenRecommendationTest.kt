package com.example.cross_platformfilemanager.domain.recommendation

import com.example.cross_platformfilemanager.runtime.TaggoRuntimeFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class AfterOpenRecommendationTest {
    private val pathModel = AfterOpenPathModel()

    @Test
    fun reconstructionSplitsDifferentSessionsAndGapsOverThirtyMinutes() {
        val events = listOf(
            open("A", 0, "session-1"),
            open("B", 2 * MINUTE, "session-1"),
            open("C", 33 * MINUTE, "session-1"),
            open("D", MINUTE, "session-2"),
            open("E", 3 * MINUTE, "session-2"),
        )

        val paths = pathModel.reconstruct(events).map { path -> path.map { it.fileId } }

        assertEquals(listOf(listOf("A", "B"), listOf("D", "E"), listOf("C")), paths)
    }

    @Test
    fun predictsFirstSecondThirdOrderAndLongPathSuffix() {
        val events = buildList {
            addPath("one", "A", "B")
            addPath("two", "A", "B")
            addPath("second", "X", "A", "B", "C")
            addPath("third", "A", "B", "C", "D")
            addPath("long", "A", "B", "C", "D", "E")
        }

        val first = pathModel.predict(events, listOf("A")).getValue("B")
        val second = pathModel.predict(events, listOf("A", "B")).getValue("C")
        val third = pathModel.predict(events, listOf("A", "B", "C")).getValue("D")
        val longSuffix = pathModel.predict(events, listOf("B", "C", "D")).getValue("E")

        assertTrue(first.directSuccessor > 0.0)
        assertEquals(1, first.matchedOrder)
        assertTrue(second.sequencePath > first.confidence)
        assertEquals(2, second.matchedOrder)
        assertTrue(third.sequencePath > second.sequencePath)
        assertEquals(3, third.matchedOrder)
        assertEquals(3, longSuffix.matchedOrder)
        assertTrue(longSuffix.sequencePath > 0.0)
    }

    @Test
    fun requestContextKeepsThreeRecentOpensAndCutsOnSessionOrGap() {
        val context = RecommendationRequestContext()
        context.recordOpenContent("A", 0, "session")
        context.recordOpenContent("B", MINUTE, "session")
        context.recordOpenContent("C", 2 * MINUTE, "session")
        context.recordOpenContent("D", 3 * MINUTE, "session")
        assertEquals(listOf("B", "C", "D"), context.createRequest(4 * MINUTE, 10).recentOpenFileIds)

        context.recordOpenContent("E", 40 * MINUTE, "session")
        assertEquals(listOf("E"), context.createRequest(40 * MINUTE, 10).recentOpenFileIds)

        context.recordOpenContent("F", 41 * MINUTE, "other-session")
        val request = context.createRequest(41 * MINUTE, 10)
        assertEquals(RecommendationMode.AFTER_OPEN, request.mode)
        assertEquals("F", request.triggerFileId)
        assertEquals(listOf("F"), request.recentOpenFileIds)
    }

    @Test
    fun rankingUsesPathThenHomeFallbackWithoutFilteringOrSourceDeduplication() {
        val files = listOf(
            file("A", source = "same"),
            file("B", source = "same"),
            file("C", source = "other"),
        )
        val history = RecommendationHistory(
            openEvents = buildList {
                addPath("history-1", "A", "B")
                addPath("history-2", "A", "B")
            },
            searchEvents = emptyList(),
            feedbackEvents = emptyList(),
        )
        val request = RecommendationRequest(
            mode = RecommendationMode.AFTER_OPEN,
            nowMs = 100 * MINUTE,
            limit = 10,
            triggerFileId = "A",
            sessionId = "current",
            recentOpenFileIds = listOf("A"),
        )
        val homePolicy = policy(RecommendationMode.HOME_INITIAL)
        val afterPolicy = policy(RecommendationMode.AFTER_OPEN)
        val homeFeatures = HomeRecommendationFeatureExtractor().extractAll(
            files,
            history,
            request.nowMs,
            homePolicy.learningConfig,
        )
        val fallback = HomeRecommendationRanker().rank(files, homeFeatures, homePolicy, files.size)
        val extracted = AfterOpenFeatureExtractor().extractAll(
            files,
            history,
            request,
            afterPolicy.learningConfig,
        )

        val ranked = AfterOpenRecommendationRanker().rank(files, extracted, afterPolicy, request, fallback)

        assertEquals(3, ranked.size)
        assertEquals(setOf("A", "B", "C"), ranked.map { it.file.id }.toSet())
        assertEquals(listOf("same", "same", "other"), files.map { it.referenceValue })
        assertEquals("B", ranked.first().file.id)
        assertTrue(ranked.first().result.featuresJson?.contains("\"mode\":\"AFTER_OPEN\"") == true)
        assertTrue(ranked.first().result.featuresJson?.contains("\"pathContext\"") == true)
        assertTrue(ranked.first().result.reasons.any { it.code == "direct_successor" })
        assertTrue(ranked.first().result.reasons.all { it.contribution != 0.0 })
    }

    @Test
    fun afterOpenFeaturesConsumeOnlyAfterOpenFeedback() {
        val files = listOf(file("home"), file("after"), file("unknown"))
        val history = RecommendationHistory(
            openEvents = emptyList(),
            searchEvents = emptyList(),
            feedbackEvents = listOf(
                RecommendationFeedbackEvent("home-set", "home", "positive_open", 1, 1, RecommendationMode.HOME_INITIAL),
                RecommendationFeedbackEvent("after-set", "after", "positive_open", 2, 1, RecommendationMode.AFTER_OPEN),
                RecommendationFeedbackEvent("unknown-set", "unknown", "positive_open", 3, 1, null),
            ),
        )
        val request = RecommendationRequest(
            mode = RecommendationMode.AFTER_OPEN,
            nowMs = 10 * MINUTE,
            limit = 10,
            triggerFileId = "after",
            recentOpenFileIds = listOf("after"),
        )

        val after = AfterOpenFeatureExtractor().extractAll(
            files,
            history,
            request,
            RecommendationLearningConfig(),
        )
        val home = HomeRecommendationFeatureExtractor().extractAll(
            files,
            history,
            request.nowMs,
            RecommendationLearningConfig(),
            RecommendationMode.HOME_INITIAL,
        )

        assertEquals(0.0, after.getValue("home").vector.feedbackPositive)
        assertEquals(1.0, after.getValue("after").vector.feedbackPositive)
        assertEquals(0.0, after.getValue("unknown").vector.feedbackPositive)
        assertEquals(1.0, home.getValue("home").vector.feedbackPositive)
        assertEquals(1.0, home.getValue("unknown").vector.feedbackPositive)
        assertEquals(0.0, home.getValue("after").vector.feedbackPositive)
    }

    @Test
    fun noPathFallsBackAndTriggerIsNotFirstUnlessItIsOnlyFile() {
        val files = listOf(file("A"), file("B"))
        val history = RecommendationHistory(emptyList(), emptyList(), emptyList())
        val request = RecommendationRequest(
            RecommendationMode.AFTER_OPEN,
            nowMs = 10 * MINUTE,
            limit = 10,
            triggerFileId = "A",
            recentOpenFileIds = listOf("A"),
        )
        val homePolicy = policy(RecommendationMode.HOME_INITIAL)
        val afterPolicy = policy(RecommendationMode.AFTER_OPEN)
        val fallbackFeatures = HomeRecommendationFeatureExtractor().extractAll(
            files,
            history,
            request.nowMs,
            homePolicy.learningConfig,
        )
        val fallback = HomeRecommendationRanker().rank(files, fallbackFeatures, homePolicy, files.size)
        val afterFeatures = AfterOpenFeatureExtractor().extractAll(
            files,
            history,
            request,
            afterPolicy.learningConfig,
        )

        val ranked = AfterOpenRecommendationRanker().rank(
            files,
            afterFeatures,
            afterPolicy,
            request,
            fallback,
        )
        assertEquals(2, ranked.size)
        assertNotEquals("A", ranked.first().file.id)

        val only = listOf(file("A"))
        val onlyFallbackFeatures = HomeRecommendationFeatureExtractor().extractAll(
            only,
            history,
            request.nowMs,
            homePolicy.learningConfig,
        )
        val onlyFallback = HomeRecommendationRanker().rank(only, onlyFallbackFeatures, homePolicy, 1)
        val onlyAfter = AfterOpenFeatureExtractor().extractAll(
            only,
            history,
            request.copy(limit = 1),
            afterPolicy.learningConfig,
        )
        assertEquals(
            "A",
            AfterOpenRecommendationRanker()
                .rank(only, onlyAfter, afterPolicy, request.copy(limit = 1), onlyFallback)
                .single()
                .file
                .id,
        )
    }

    private fun policy(mode: RecommendationMode) = RecommendationPolicy(
        id = mode.name,
        policyName = if (mode == RecommendationMode.AFTER_OPEN) {
            RecommendationPolicyStore.AFTER_OPEN_POLICY_NAME
        } else {
            RecommendationPolicyStore.POLICY_NAME
        },
        mode = mode,
        modelVersion = 1,
        weights = if (mode == RecommendationMode.AFTER_OPEN) {
            RecommendationWeights.afterOpen()
        } else {
            RecommendationWeights()
        },
        learningConfig = RecommendationLearningConfig(),
        updateCount = 0,
        createdAtMs = 0,
        lastUpdatedAtMs = 0,
    )

    private fun MutableList<RecommendationOpenEvent>.addPath(
        sessionId: String,
        vararg fileIds: String,
    ) {
        fileIds.forEachIndexed { index, fileId ->
            add(open(fileId, index * MINUTE, sessionId))
        }
    }

    private fun open(fileId: String, at: Long, sessionId: String) =
        RecommendationOpenEvent(fileId, null, at, sessionId, null)

    private fun file(
        id: String,
        source: String = "C:/files/$id.pdf",
    ) = TaggoRuntimeFile(
        id = id,
        displayName = "$id.pdf",
        extension = "pdf",
        mimeType = "application/pdf",
        taggoFileCategory = "PDF",
        sizeBytes = 1,
        primaryReferenceId = "ref_$id",
        referenceType = "path",
        referenceValue = source,
        referenceAvailable = true,
        platform = "test",
        tags = listOf("work"),
        createdAtMs = 1,
        updatedAtMs = 1,
        lastContentOpenedAtMs = null,
        contentOpenCount = 0,
        thumbnailState = "none",
        thumbnailReferenceValue = null,
    )

    private companion object {
        const val MINUTE = 60_000L
    }
}

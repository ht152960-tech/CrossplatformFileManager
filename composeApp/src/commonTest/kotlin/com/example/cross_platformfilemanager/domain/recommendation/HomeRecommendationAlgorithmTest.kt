package com.example.cross_platformfilemanager.domain.recommendation

import com.example.cross_platformfilemanager.runtime.TaggoRuntimeFile
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HomeRecommendationAlgorithmTest {
    private val extractor = HomeRecommendationFeatureExtractor()
    private val config = RecommendationLearningConfig()
    private val now = 10L * DAY

    @Test
    fun eventSignalsRemainSeparatedAndManualSearchRequiresAnOpen() {
        val opened = file("opened", tags = listOf("work"))
        val detailOnly = file("detail", tags = listOf("work"))
        val failedOnly = file("failed")
        val searchOnly = file("search-only")
        val history = RecommendationHistory(
            openEvents = listOf(
                open("opened", now - 3 * DAY, "session"),
                open("opened", now - 2 * DAY, "session"),
                open("opened", now - DAY, "session"),
            ),
            searchEvents = listOf(
                RecommendationSearchEvent("opened", now - DAY - 5 * MINUTE, "session"),
                RecommendationSearchEvent("search-only", now - MINUTE, "session"),
            ),
            feedbackEvents = listOf(
                RecommendationFeedbackEvent("set", "opened", "positive_open", now - DAY, 1),
                RecommendationFeedbackEvent(
                    "set",
                    "detail",
                    "skipped_before_selected_file",
                    now - DAY,
                    1,
                ),
                RecommendationFeedbackEvent("set", "failed", "open_failed", now - DAY, 1),
            ),
            detailEvents = listOf(RecommendationIntentEvent("detail", now - HOUR, "session", "home")),
            failedOpenEvents = listOf(RecommendationIntentEvent("failed", now - HOUR, "session", "home")),
        )

        val features = extractor.extractAll(
            listOf(opened, detailOnly, failedOnly, searchOnly),
            history,
            now,
            config,
        )

        with(features.getValue("opened").vector) {
            assertTrue(recency > 0.0)
            assertTrue(frequency > 0.0)
            assertTrue(periodic > 0.0)
            assertEquals(1.0, manualSearchOpen)
            assertTrue(feedbackPositive > 0.0)
        }
        with(features.getValue("detail").vector) {
            assertTrue(detailInterest > 0.0)
            assertEquals(0.0, recency)
            assertEquals(0.0, frequency)
            assertEquals(0.0, periodic)
            assertTrue(tagAffinity > 0.0)
            assertTrue(feedbackPenalty > 0.0)
        }
        with(features.getValue("failed").vector) {
            assertTrue(failedOpenIntent > 0.0)
            assertEquals(0.0, recency)
            assertEquals(0.0, frequency)
            assertEquals(0.0, periodic)
            assertEquals(0.0, feedbackPenalty)
        }
        assertEquals(0.0, features.getValue("search-only").vector.manualSearchOpen)
    }

    @Test
    fun dynamicPeriodUsesObservedIntervalsAndNeedsEnoughSamples() {
        val detector = DynamicPeriodDetector()

        val stable = detector.detect(listOf(now - 3 * DAY, now - 2 * DAY, now - DAY), now)
        val insufficient = detector.detect(listOf(now - DAY, now), now)

        assertEquals(DAY, stable.estimatedPeriodMs)
        assertTrue(stable.score > 0.0)
        assertEquals(0.0, insufficient.score)
        assertEquals(null, insufficient.estimatedPeriodMs)
    }

    @Test
    fun rankerReturnsEveryCandidateWithoutSourceOrTypeReordering() {
        val files = listOf(
            file("b", source = "same", category = "PDF"),
            file("a", source = "same", category = "PDF"),
            file("c", source = "other", category = "PDF"),
        )
        val extracted = files.associate { file ->
            file.id to ExtractedHomeFeatures(RecommendationFeatureVector(), 0.0, null)
        }
        val results = HomeRecommendationRanker().rank(files, extracted, policy(), limit = 10)

        assertEquals(3, results.size)
        assertEquals(listOf("a", "b", "c"), results.map { it.file.id })
        assertEquals(listOf("same", "same", "other"), results.map { it.file.referenceValue })
        assertTrue(results.all { it.result.reasons.isEmpty() })
    }

    @Test
    fun rankerUsesOriginalDisplayNameForTieBreaker() {
        val files = listOf(
            file("lowercase", displayName = "a.pdf"),
            file("uppercase", displayName = "B.pdf"),
        )
        val extracted = files.associate { file ->
            file.id to ExtractedHomeFeatures(RecommendationFeatureVector(), 0.0, null)
        }

        val results = HomeRecommendationRanker().rank(files, extracted, policy(), limit = 10)

        assertEquals(listOf("B.pdf", "a.pdf"), results.map { it.file.displayName })
    }

    @Test
    fun finalScoreEqualsContributionSumAndReasonsOnlyDescribeNonZeroSignals() {
        val file = file("one")
        val vector = RecommendationFeatureVector(recency = 0.5, feedbackPenalty = 0.4)
        val ranked = HomeRecommendationRanker().rank(
            files = listOf(file),
            extracted = mapOf(file.id to ExtractedHomeFeatures(vector, 0.0, null)),
            policy = policy(),
            limit = 1,
        ).single()
        val expected = 0.5 * policy().weights.recency - 0.4 * policy().weights.feedbackPenalty

        assertTrue(abs(expected - ranked.result.finalScore) < 1e-9)
        assertEquals(setOf("recency", "feedbackPenalty"), ranked.result.reasons.map { it.code }.toSet())
        assertTrue(ranked.result.featuresJson?.contains("\"mode\":\"HOME_INITIAL\"") == true)
    }

    private fun policy() = RecommendationPolicy(
        id = "policy",
        policyName = RecommendationPolicyStore.POLICY_NAME,
        mode = RecommendationMode.HOME_INITIAL,
        modelVersion = RecommendationPolicyStore.MODEL_VERSION,
        weights = RecommendationWeights(),
        learningConfig = config,
        updateCount = 0,
        createdAtMs = 0,
        lastUpdatedAtMs = 0,
    )

    private fun open(fileId: String, at: Long, sessionId: String) =
        RecommendationOpenEvent(fileId, null, at, sessionId, "home")

    private fun file(
        id: String,
        displayName: String = "$id.pdf",
        tags: List<String> = emptyList(),
        source: String = "C:/files/$id.pdf",
        category: String = "PDF",
    ) = TaggoRuntimeFile(
        id = id,
        displayName = displayName,
        extension = "pdf",
        mimeType = "application/pdf",
        taggoFileCategory = category,
        sizeBytes = 1,
        primaryReferenceId = "ref_$id",
        referenceType = "path",
        referenceValue = source,
        referenceAvailable = true,
        platform = "test",
        tags = tags,
        createdAtMs = 1,
        updatedAtMs = 1,
        lastContentOpenedAtMs = null,
        contentOpenCount = 0,
        thumbnailState = "none",
        thumbnailReferenceValue = null,
    )

    private companion object {
        const val MINUTE = 60_000L
        const val HOUR = 60 * MINUTE
        const val DAY = 24 * HOUR
    }
}

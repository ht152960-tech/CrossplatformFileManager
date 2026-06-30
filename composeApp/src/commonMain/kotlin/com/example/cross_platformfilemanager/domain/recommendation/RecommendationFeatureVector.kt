package com.example.cross_platformfilemanager.domain.recommendation

import kotlinx.serialization.Serializable

@Serializable
data class RecommendationFeatureVector(
    val periodic: Double = 0.0,
    val sequencePath: Double = 0.0,
    val directSuccessor: Double = 0.0,
    val manualSearchOpen: Double = 0.0,
    val recency: Double = 0.0,
    val frequency: Double = 0.0,
    val feedbackPositive: Double = 0.0,
    val tagAffinity: Double = 0.0,
    val detailInterest: Double = 0.0,
    val failedOpenIntent: Double = 0.0,
    val coldStart: Double = 0.0,
    val feedbackPenalty: Double = 0.0,
    val weakTypeContext: Double = 0.0,
) {
    fun normalized() = RecommendationFeatureVector(
        periodic = periodic.unit(),
        sequencePath = sequencePath.unit(),
        directSuccessor = directSuccessor.unit(),
        manualSearchOpen = manualSearchOpen.unit(),
        recency = recency.unit(),
        frequency = frequency.unit(),
        feedbackPositive = feedbackPositive.unit(),
        tagAffinity = tagAffinity.unit(),
        detailInterest = detailInterest.unit(),
        failedOpenIntent = failedOpenIntent.unit(),
        coldStart = coldStart.unit(),
        feedbackPenalty = feedbackPenalty.unit(),
        weakTypeContext = weakTypeContext.unit(),
    )

    fun values(): Map<String, Double> = linkedMapOf(
        "periodic" to periodic,
        "sequencePath" to sequencePath,
        "directSuccessor" to directSuccessor,
        "manualSearchOpen" to manualSearchOpen,
        "recency" to recency,
        "frequency" to frequency,
        "feedbackPositive" to feedbackPositive,
        "tagAffinity" to tagAffinity,
        "detailInterest" to detailInterest,
        "failedOpenIntent" to failedOpenIntent,
        "coldStart" to coldStart,
        "feedbackPenalty" to feedbackPenalty,
        "weakTypeContext" to weakTypeContext,
    )

    private fun Double.unit() = coerceIn(0.0, 1.0)
}

@Serializable
data class RecommendationPathContext(
    val triggerFileId: String? = null,
    val recentOpenFileIds: List<String> = emptyList(),
    val matchedOrder: Int = 0,
)

@Serializable
data class RecommendationFeatureSnapshot(
    val mode: String,
    val features: Map<String, Double>,
    val weights: Map<String, Double>,
    val contributions: Map<String, Double>,
    val confidence: Map<String, Double> = emptyMap(),
    val pathContext: RecommendationPathContext? = null,
)

data class PeriodicSignal(
    val score: Double,
    val confidence: Double,
    val estimatedPeriodMs: Long?,
)

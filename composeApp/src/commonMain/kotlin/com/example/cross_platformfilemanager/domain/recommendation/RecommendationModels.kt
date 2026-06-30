package com.example.cross_platformfilemanager.domain.recommendation

enum class RecommendationMode {
    HOME_INITIAL,
    AFTER_OPEN,
}

data class RecommendationRequest(
    val mode: RecommendationMode,
    val nowMs: Long,
    val limit: Int,
    val triggerFileId: String? = null,
    val sessionId: String? = null,
    val recentOpenFileIds: List<String> = emptyList(),
)

data class RecommendationResult(
    val fileId: String,
    val finalScore: Double,
    val scoreParts: RecommendationScoreParts,
    val reasons: List<RecommendationReason>,
    val featuresJson: String? = null,
    val mode: RecommendationMode,
)

data class RecommendationScoreParts(
    val recencyScore: Double = 0.0,
    val frequencyScore: Double = 0.0,
    val periodicScore: Double = 0.0,
    val tagAffinityScore: Double = 0.0,
    val manualSearchAffinityScore: Double = 0.0,
    val feedbackScore: Double = 0.0,
    val weakTypeContextScore: Double = 0.0,
    val sequencePathScore: Double = 0.0,
    val successorScore: Double = 0.0,
    val recentCoOpenScore: Double = 0.0,
    val tagContinuationScore: Double = 0.0,
    val legacyIntervalScore: Double = 0.0,
    val legacyTransitionScore: Double = 0.0,
    val legacyRecencyScore: Double = 0.0,
)

data class RecommendationReason(
    val code: String,
    val message: String,
    val contribution: Double? = null,
    val confidence: Double? = null,
)

package com.example.cross_platformfilemanager.domain.recommendation

import com.example.cross_platformfilemanager.ScoredRecommendation

fun ScoredRecommendation.toRecommendationResult(
    mode: RecommendationMode,
): RecommendationResult = RecommendationResult(
    fileId = file.id,
    finalScore = finalScore,
    scoreParts = RecommendationScoreParts(
        legacyIntervalScore = intervalScore,
        legacyTransitionScore = transitionScore,
        legacyRecencyScore = recencyScore,
    ),
    reasons = buildList {
        if (intervalScore > 0.0) {
            add(
                RecommendationReason(
                    code = "legacy_interval",
                    message = "Legacy interval signal",
                    contribution = intervalScore,
                ),
            )
        }
        if (transitionScore > 0.0) {
            add(
                RecommendationReason(
                    code = "legacy_transition",
                    message = "Legacy successor signal",
                    contribution = transitionScore,
                ),
            )
        }
        if (recencyScore > 0.0) {
            add(
                RecommendationReason(
                    code = "legacy_recency",
                    message = "Legacy recency signal",
                    contribution = recencyScore,
                ),
            )
        }
    },
    featuresJson = null,
    mode = mode,
)

fun List<ScoredRecommendation>.toRecommendationResults(
    mode: RecommendationMode,
): List<RecommendationResult> = map { it.toRecommendationResult(mode) }

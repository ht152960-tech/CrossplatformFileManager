package com.example.cross_platformfilemanager

interface RecommendationReadOnlyState {
    val recommendedReferences: List<FileReference>
    val scoredRecommendedReferences: List<ScoredRecommendation>
}

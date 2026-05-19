package com.example.cross_platformfilemanager

data class ScoredRecommendation(
    val file: FileReference,
    val intervalScore: Double,
    val transitionScore: Double,
    val recencyScore: Double,
    val finalScore: Double,
)

data class RecommendationEngineSnapshot(
    val filePatterns: Map<String, FilePattern>,
    val transitionSnapshot: TransitionSnapshot,
    val weightSnapshot: WeightSnapshot,
    val lastOpenedFileId: String?,
)

data class RecommendationClickLog(
    val clickedFileId: String,
    val openedAtMillis: Long,
    val previousFileId: String?,
    val shownFileIds: List<String>,
)

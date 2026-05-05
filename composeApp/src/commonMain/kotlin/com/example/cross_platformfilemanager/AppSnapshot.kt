package com.example.cross_platformfilemanager

data class AppSnapshot(
    val locale: AppLocale,
    val query: String,
    val selectedTag: String?,
    val activeReferenceId: String?,
    val references: List<FileReference>,
    val recentSearches: List<String>,
    val recommendationLogs: List<RecommendationLog>,
)


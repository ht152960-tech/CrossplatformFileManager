package com.example.cross_platformfilemanager

data class AppSnapshot(
    val schemaVersion: Int = 3,
    val locale: AppLocale,
    val query: String,
    val selectedTag: String?,
    val selectedFileType: String?,
    val favoritesOnly: Boolean,
    val activeReferenceId: String?,
    val references: List<FileReference>,
    val recentSearches: List<String>,
    val recommendationLogs: List<RecommendationLog>,
)

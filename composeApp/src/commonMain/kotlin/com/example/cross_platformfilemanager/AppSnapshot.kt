package com.example.cross_platformfilemanager

data class AppSnapshot(
    val schemaVersion: Int = 9,
    val locale: AppLocale,
    val query: String,
    val searchTags: List<SearchTag> = emptyList(),
    val selectedTag: String?,
    val selectedFileType: String?,
    val favoritesOnly: Boolean,
    val activeReferenceId: String?,
    val references: List<FileReference>,
    val recentSearches: List<String>,
    val recommendationLogs: List<RecommendationLog>,
    val recommendationState: RecommendationEngineSnapshot? = null,
)

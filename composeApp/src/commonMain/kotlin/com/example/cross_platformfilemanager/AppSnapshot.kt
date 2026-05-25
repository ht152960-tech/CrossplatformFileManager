package com.example.cross_platformfilemanager

/**
 * 应用工作区快照。
 *
 * 除了普通文件列表、查询条件和最近搜索外，
 * 这里还把推荐日志与推荐引擎状态一起纳入快照，
 * 这样恢复工作区时，推荐算法可以延续之前的学习结果。
 */
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

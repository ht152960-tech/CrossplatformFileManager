package com.example.cross_platformfilemanager

import kotlinx.serialization.Serializable

/**
 * 非业务界面状态快照。
 *
 * 文件、标签和最近搜索只从 SQLDelight 数据库恢复，不进入 JSON 快照。
 */
@Serializable
data class AppSnapshot(
    val schemaVersion: Int = 11,
    val locale: AppLocale,
    val query: String,
    val searchTags: List<SearchTag> = emptyList(),
    val selectedTag: String?,
    val selectedFileType: String?,
    val activeReferenceId: String?,
)

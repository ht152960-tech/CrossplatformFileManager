package com.example.cross_platformfilemanager.data.model

/**
 * 文件条目的数据库领域模型。
 */
data class TaggoFileEntry(
    val id: String,
    val displayName: String,
    val extension: String?,
    val mimeType: String?,
    /** Taggo 自己归类后的文件类别，不是 MIME 类型。 */
    val taggoFileCategory: String,
    val sizeBytes: Long?,
    val createdAtMs: Long,
    val updatedAtMs: Long,
    /** 只统计真正的 open_content，不统计 view_detail。 */
    val lastContentOpenedAtMs: Long?,
    /** 只统计真正的 open_content，不统计 view_detail。 */
    val contentOpenCount: Long,
    val thumbnailState: String,
    val thumbnailReferenceValue: String?,
    val deletedAtMs: Long?,
)

/**
 * 文件引用的数据库领域模型。
 */
data class TaggoFileReference(
    val id: String,
    val fileId: String,
    val referenceType: String,
    val referenceValue: String,
    val referenceAvailable: Boolean,
    val platform: String?,
    val createdAtMs: Long,
    val updatedAtMs: Long,
    val lastVerifiedAtMs: Long?,
    val isPrimary: Boolean,
)

/**
 * 标签的数据库领域模型。
 */
data class TaggoTag(
    val id: String,
    val name: String,
    val normalizedName: String,
    val createdAtMs: Long,
    val updatedAtMs: Long,
    val deletedAtMs: Long?,
)

/**
 * 最近搜索记录的数据库领域模型。
 */
data class TaggoRecentSearch(
    val id: String,
    val rawQuery: String,
    val normalizedQuery: String,
    val createdAtMs: Long,
    val lastUsedAtMs: Long,
    val useCount: Long,
)

/**
 * 行为会话的数据库领域模型。
 */
data class TaggoBehaviorSession(
    val id: String,
    val startedAtMs: Long,
    val endedAtMs: Long?,
    val platform: String,
    val appVersion: String?,
    val databaseVersion: Long,
    val recommendationModelVersion: Long,
)

/**
 * 行为事件的数据库领域模型。
 */
data class TaggoBehaviorEvent(
    val id: String,
    val sessionId: String?,
    val occurredAtMs: Long,
    /** 事件类型必须区分 view_detail 和 open_content。 */
    val eventType: String,
    val screenName: String?,
    val entryPoint: String?,
    val fileId: String?,
    val fileReferenceId: String?,
    val searchQuery: String?,
    val recommendationSetId: String?,
    val recommendationRank: Long?,
    val durationMs: Long?,
    val extraJson: String?,
)

/**
 * 显式需求信号的数据库领域模型。
 *
 * 这里只记录用户显式表达的需求事实，不存算法 strength。
 */
data class TaggoExplicitNeedSignal(
    val id: String,
    val sessionId: String?,
    val behaviorEventId: String?,
    val signalType: String,
    val signalValue: String,
    val createdAtMs: Long,
    val consumedByRecommendationSetId: String?,
)

/**
 * 推荐上下文的数据库领域模型。
 */
data class TaggoRecommendationContext(
    val id: String,
    val createdAtMs: Long,
    val contextType: String,
    val sessionId: String?,
    val triggerFileId: String?,
    val searchQuery: String?,
    val localHour: Long?,
    val dayOfWeek: Long?,
    val latitude: Double?,
    val longitude: Double?,
    val locationPrecision: String?,
)

/**
 * 推荐结果集的数据库领域模型。
 */
data class TaggoRecommendationSet(
    val id: String,
    val contextId: String?,
    val generatedAtMs: Long,
    val setType: String,
    val modelVersion: Long,
    val policyName: String?,
    val policyVersion: String?,
)

/**
 * 推荐候选快照的数据库领域模型。
 */
data class TaggoRecommendationCandidateSnapshot(
    val recommendationSetId: String,
    val fileId: String,
    val rank: Long,
    /** 当时推荐模型输出快照，不是文件属性。 */
    val score: Double,
    val reasonsJson: String?,
    /** 为未来 bandit 和离线复盘预留。 */
    val featuresJson: String?,
    val selectionType: String?,
    /** 为未来 bandit 和离线复盘预留。 */
    val propensity: Double?,
    val selectedAtMs: Long?,
)

/**
 * 推荐反馈的数据库领域模型。
 */
data class TaggoRecommendationFeedback(
    val id: String,
    val recommendationSetId: String,
    val fileId: String,
    val feedbackType: String,
    /** 推荐反馈解释值，不是文件事实。 */
    val rewardValue: Double?,
    val rankAtFeedback: Long?,
    val createdAtMs: Long,
    val behaviorEventId: String?,
)

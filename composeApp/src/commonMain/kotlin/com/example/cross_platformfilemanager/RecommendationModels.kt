package com.example.cross_platformfilemanager

import kotlinx.serialization.Serializable

/**
 * 单个候选文件的推荐打分结果。
 *
 * 这个对象把三类基础信号和最终综合分放在一起，
 * 便于上层展示结果，也便于在线学习阶段回看每个候选文件当时的得分依据。
 */
data class ScoredRecommendation(
    val file: FileReference,
    val intervalScore: Double,
    val transitionScore: Double,
    val recencyScore: Double,
    val finalScore: Double,
)

/**
 * 推荐引擎的可持久化快照。
 *
 * 这里显式保留各个子模块的状态边界，
 * 让存储层只负责保存快照，而不需要理解推荐算法内部的更新细节。
 */
@Serializable
data class RecommendationEngineSnapshot(
    val filePatterns: Map<String, FilePattern>,
    val transitionSnapshot: TransitionSnapshot,
    val weightSnapshot: WeightSnapshot,
    val lastOpenedFileId: String?,
)

/**
 * 一次推荐点击反馈的事件记录。
 *
 * 它描述的是“推荐列表展示之后，用户最终点开了哪个文件”，
 * 与普通打开事件区分开来，供后续在线学习和行为回放使用。
 */
data class RecommendationClickLog(
    val clickedFileId: String,
    val openedAtMillis: Long,
    val previousFileId: String?,
    val shownFileIds: List<String>,
)

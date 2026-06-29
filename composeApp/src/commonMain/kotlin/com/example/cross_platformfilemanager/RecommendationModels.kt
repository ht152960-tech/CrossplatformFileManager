package com.example.cross_platformfilemanager

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

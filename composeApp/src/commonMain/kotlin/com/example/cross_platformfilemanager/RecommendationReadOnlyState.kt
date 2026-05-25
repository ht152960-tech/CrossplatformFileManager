package com.example.cross_platformfilemanager

/**
 * 面向界面或只读调用方暴露的推荐结果视图。
 *
 * 这个接口只提供推荐结果，不暴露推荐引擎的可变操作，
 * 让上层可以读取“推荐文件列表”和“带综合分明细的推荐结果”。
 */
interface RecommendationReadOnlyState {
    val recommendedReferences: List<FileReference>
    val scoredRecommendedReferences: List<ScoredRecommendation>
}

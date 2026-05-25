package com.example.cross_platformfilemanager

/**
 * 根据推荐点击反馈在线调整推荐权重。
 *
 * 它不直接改动时间规律或后继关系样本，
 * 只负责根据“被点击文件”和“未被点击的已展示文件”之间的差异，
 * 小步修正三类信号在综合分中的权重。
 */
class OnlineLearner(
    private val weightStore: WeightStore,
    private val positiveLearningRate: Double = 0.05,
    private val negativeLearningRate: Double = 0.01,
) {
    /**
     * 用一次推荐点击反馈更新学习权重。
     *
     * 被点击文件视为正样本；
     * 同一批已展示但未被点击的文件视为弱负样本。
     */
    fun learnFromClick(
        clickedFileId: String,
        shownRecommendations: List<ScoredRecommendation>,
    ) {
        val clicked = shownRecommendations.firstOrNull { it.file.id == clickedFileId } ?: return
        val skipped = shownRecommendations.filterNot { it.file.id == clickedFileId }
        val confidence = recommendationConfidence(shownRecommendations.size)

        val skippedInterval = meanOfOrZero(skipped) { it.intervalScore }
        val skippedTransition = meanOfOrZero(skipped) { it.transitionScore }
        val skippedRecency = meanOfOrZero(skipped) { it.recencyScore }

        // 推荐列表越短，样本越少，学习幅度就越保守，避免单次点击把权重推得太快。
        weightStore.updateLearning(
            intervalDelta = confidence * (
                (positiveLearningRate * clicked.intervalScore) - (negativeLearningRate * skippedInterval)
            ),
            transitionDelta = confidence * (
                (positiveLearningRate * clicked.transitionScore) - (negativeLearningRate * skippedTransition)
            ),
            recencyDelta = confidence * (
                (positiveLearningRate * clicked.recencyScore) - (negativeLearningRate * skippedRecency)
            ),
        )
    }

    private fun meanOfOrZero(items: List<ScoredRecommendation>, selector: (ScoredRecommendation) -> Double): Double {
        if (items.isEmpty()) return 0.0
        var sum = 0.0
        for (item in items) {
            sum += selector(item)
        }
        return sum / items.size.toDouble()
    }

    private fun recommendationConfidence(recommendationCount: Int): Double = when {
        recommendationCount <= 1 -> 0.35
        recommendationCount == 2 -> 0.55
        recommendationCount == 3 -> 0.75
        recommendationCount == 4 -> 0.9
        else -> 1.0
    }
}

package com.example.cross_platformfilemanager

class OnlineLearner(
    private val weightStore: WeightStore,
    private val positiveLearningRate: Double = 0.05,
    private val negativeLearningRate: Double = 0.01,
) {
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

        // 推荐列表越短，样本越少，学习幅度就越保守，避免单次点击把权重抬得太快。
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

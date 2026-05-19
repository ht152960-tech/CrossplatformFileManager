package com.example.cross_platformfilemanager

data class WeightSnapshot(
    val baseIntervalWeight: Double,
    val baseTransitionWeight: Double,
    val baseRecencyWeight: Double,
    val learnedIntervalWeight: Double,
    val learnedTransitionWeight: Double,
    val learnedRecencyWeight: Double,
)

class WeightStore(
    val baseIntervalWeight: Double = 1.2,
    val baseTransitionWeight: Double = 1.5,
    val baseRecencyWeight: Double = 0.4,
    var learnedIntervalWeight: Double = 0.0,
    var learnedTransitionWeight: Double = 0.0,
    var learnedRecencyWeight: Double = 0.0,
) {
    val learnedWeight: Double
        get() = learnedIntervalWeight + learnedTransitionWeight + learnedRecencyWeight

    fun totalIntervalWeight(): Double = baseIntervalWeight + learnedIntervalWeight

    fun totalTransitionWeight(): Double = baseTransitionWeight + learnedTransitionWeight

    fun totalRecencyWeight(): Double = baseRecencyWeight + learnedRecencyWeight

    fun updateLearning(
        intervalDelta: Double = 0.0,
        transitionDelta: Double = 0.0,
        recencyDelta: Double = 0.0,
    ) {
        // 学习权重只做小步更新，再轻微向 0 回拉，避免一次偶发点击长期主导推荐结果。
        learnedIntervalWeight = stabilize(learnedIntervalWeight + intervalDelta)
        learnedTransitionWeight = stabilize(learnedTransitionWeight + transitionDelta)
        learnedRecencyWeight = stabilize(learnedRecencyWeight + recencyDelta)
    }

    fun snapshot(): WeightSnapshot = WeightSnapshot(
        baseIntervalWeight = baseIntervalWeight,
        baseTransitionWeight = baseTransitionWeight,
        baseRecencyWeight = baseRecencyWeight,
        learnedIntervalWeight = learnedIntervalWeight,
        learnedTransitionWeight = learnedTransitionWeight,
        learnedRecencyWeight = learnedRecencyWeight,
    )

    fun restore(snapshot: WeightSnapshot) {
        learnedIntervalWeight = snapshot.learnedIntervalWeight
        learnedTransitionWeight = snapshot.learnedTransitionWeight
        learnedRecencyWeight = snapshot.learnedRecencyWeight
    }

    fun resetLearning() {
        learnedIntervalWeight = 0.0
        learnedTransitionWeight = 0.0
        learnedRecencyWeight = 0.0
    }

    private fun clamp(value: Double): Double = value.coerceIn(-1.5, 2.5)

    private fun stabilize(value: Double): Double {
        val clamped = clamp(value)
        // 这里做轻微衰减，是为了让学习权重更像“趋势”而不是“瞬时噪声”。
        return (clamped * learningRetentionFactor).coerceIn(-1.5, 2.5)
    }

    private companion object {
        const val learningRetentionFactor = 0.98
    }
}

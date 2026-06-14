package com.example.cross_platformfilemanager

import kotlinx.serialization.Serializable

/**
 * 推荐权重的可持久化快照。
 *
 * 基础权重负责冷启动阶段的稳定性，
 * 学习权重负责根据推荐反馈逐步修正三类信号的重要性。
 */
@Serializable
data class WeightSnapshot(
    val baseIntervalWeight: Double,
    val baseTransitionWeight: Double,
    val baseRecencyWeight: Double,
    val learnedIntervalWeight: Double,
    val learnedTransitionWeight: Double,
    val learnedRecencyWeight: Double,
)

/**
 * 管理推荐打分权重的内存 Store。
 *
 * 它把固定基础权重和在线学习得到的动态权重拆开保存，
 * 这样既能保证冷启动表现，又能让模型逐步适应用户的个人习惯。
 */
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

    /**
     * 按增量更新三类学习权重。
     *
     * 这里只允许小步调整，并在每次更新后做钳制和轻微衰减，
     * 防止一次偶然点击长期主导推荐结果。
     */
    fun updateLearning(
        intervalDelta: Double = 0.0,
        transitionDelta: Double = 0.0,
        recencyDelta: Double = 0.0,
    ) {
        // 学习权重只做小步更新，再轻微向 0 回拉，避免一次偶发反馈长期主导结果。
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
        // 这里做轻微衰减，是为了让学习权重更像趋势，而不是瞬时噪声。
        return (clamped * learningRetentionFactor).coerceIn(-1.5, 2.5)
    }

    private companion object {
        const val learningRetentionFactor = 0.98
    }
}

package com.example.cross_platformfilemanager

import kotlinx.serialization.Serializable
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.roundToLong

/**
 * 单个文件条目的时间规律状态。
 *
 * 它描述的是某个文件最近一次打开时间、估计周期和样本数量，
 * 供推荐引擎计算“当前是否到了它常被再次打开的时候”。
 */
@Serializable
data class FilePattern(
    val fileId: String,
    val lastOpenTimeMillis: Long,
    val estimatedPeriodMillis: Long,
    val openCount: Int,
)

/**
 * 管理文件时间规律样本的内存 Store。
 *
 * 第一版直接使用内存 Map 保存每个文件的规律状态，
 * 重点是验证时间规律打分是否有效，而不是追求最终存储方案。
 */
class FilePatternStore(
    private val defaultEstimatedPeriodMillis: Long = 7L * DAY_MILLIS,
    private val learningRate: Double = 0.2,
    private val intervalToleranceRatio: Double = 0.5,
    private val minimumToleranceMillis: Long = DAY_MILLIS,
    private val minimumMeaningfulIntervalMillis: Long = 30L * 60L * 1000L,
) {
    private val patterns = mutableMapOf<String, FilePattern>()

    /**
     * 根据一次打开事件更新时间规律状态。
     *
     * 首次出现的文件使用默认周期起步；
     * 非首次出现时，用最新时间间隔对估计周期做平滑更新。
     */
    fun recordOpen(log: FileOpenLog): FilePattern {
        val current = patterns[log.fileId]
        val updated = if (current == null) {
            FilePattern(
                fileId = log.fileId,
                lastOpenTimeMillis = log.openedAtMillis,
                estimatedPeriodMillis = defaultEstimatedPeriodMillis,
                openCount = 1,
            )
        } else {
            val interval = (log.openedAtMillis - current.lastOpenTimeMillis).coerceAtLeast(0L)
            val estimatedPeriod = if (interval >= minimumMeaningfulIntervalMillis) {
                // 很短的连续打开更像重复点击、刷新或预览，不把它当成稳定周期样本。
                ((current.estimatedPeriodMillis * (1.0 - learningRate)) + (interval * learningRate))
                    .roundToLong()
                    .coerceAtLeast(DAY_MILLIS)
            } else {
                // 样本过短时只更新时间和计数，避免把周期模型带偏。
                current.estimatedPeriodMillis
            }

            current.copy(
                lastOpenTimeMillis = max(current.lastOpenTimeMillis, log.openedAtMillis),
                estimatedPeriodMillis = estimatedPeriod,
                openCount = current.openCount + 1,
            )
        }

        patterns[log.fileId] = updated
        return updated
    }

    fun get(fileId: String): FilePattern? = patterns[fileId]

    /**
     * 计算单个文件的时间规律分。
     *
     * 当前时间越接近该文件的估计周期，得分越高；
     * 但当样本很少时，会向中性分做保守回退，避免少量样本造成过强判断。
     */
    fun intervalScore(fileId: String, currentTimeMillis: Long): Double {
        val pattern = patterns[fileId] ?: return 0.5
        val gap = (currentTimeMillis - pattern.lastOpenTimeMillis).coerceAtLeast(0L)
        val tolerance = max(
            (pattern.estimatedPeriodMillis * intervalToleranceRatio).roundToLong(),
            minimumToleranceMillis,
        )
        val distance = kotlin.math.abs(gap - pattern.estimatedPeriodMillis)
        val rawScore = exp(-(distance.toDouble() / tolerance.toDouble())).coerceIn(0.0, 1.0)
        val confidence = signalConfidence(pattern.openCount)
        // 打开次数少时，时间规律还不够稳定，用中性分向下做保守回退。
        return ((0.5 * (1.0 - confidence)) + (rawScore * confidence)).coerceIn(0.0, 1.0)
    }

    fun snapshot(): Map<String, FilePattern> = patterns.mapValues { it.value.copy() }

    fun restore(snapshot: Map<String, FilePattern>) {
        patterns.clear()
        patterns.putAll(snapshot.mapValues { it.value.copy() })
    }

    fun clear() {
        patterns.clear()
    }

    private fun signalConfidence(openCount: Int): Double = when {
        openCount <= 1 -> 0.35
        openCount == 2 -> 0.6
        openCount == 3 -> 0.8
        else -> 1.0
    }
}

private const val DAY_MILLIS = 86_400_000L

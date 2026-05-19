package com.example.cross_platformfilemanager

import kotlin.math.exp
import kotlin.math.max
import kotlin.math.roundToLong

data class FilePattern(
    val fileId: String,
    val lastOpenTimeMillis: Long,
    val estimatedPeriodMillis: Long,
    val openCount: Int,
)

class FilePatternStore(
    private val defaultEstimatedPeriodMillis: Long = 7L * DAY_MILLIS,
    private val learningRate: Double = 0.2,
    private val intervalToleranceRatio: Double = 0.5,
    private val minimumToleranceMillis: Long = DAY_MILLIS,
    private val minimumMeaningfulIntervalMillis: Long = 30L * 60L * 1000L,
) {
    private val patterns = mutableMapOf<String, FilePattern>()

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
                // 样本太短时保守不改周期，只更新时间戳和计数，避免把周期模型带歪。
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
        // 打开次数少时，周期规律还不够稳，用中性分数向下做保守回退。
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

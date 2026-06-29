package com.example.cross_platformfilemanager.domain.recommendation

import kotlin.math.abs
import kotlin.math.exp

class DynamicPeriodDetector {
    fun detect(openedAtMs: List<Long>, nowMs: Long): PeriodicSignal {
        val ordered = openedAtMs.distinct().sorted()
        val intervals = ordered.zipWithNext { left, right -> right - left }
            .filter { it >= MIN_INTERVAL_MS }
        if (intervals.size < MIN_INTERVAL_SAMPLES) {
            return PeriodicSignal(0.0, 0.0, null)
        }

        val period = median(intervals)
        val deviations = intervals.map { abs(it - period) }
        val medianDeviation = median(deviations)
        val stability = (1.0 - (medianDeviation.toDouble() / period.coerceAtLeast(1L))).coerceIn(0.0, 1.0)
        val support = (intervals.size.toDouble() / FULL_CONFIDENCE_SAMPLES).coerceIn(0.0, 1.0)
        val confidence = stability * support
        val expectedAt = ordered.last() + period
        val tolerance = maxOf((period * TOLERANCE_RATIO).toLong(), MIN_TOLERANCE_MS)
        val distance = abs(nowMs - expectedAt).toDouble()
        val proximity = exp(-(distance / tolerance.toDouble())).coerceIn(0.0, 1.0)
        return PeriodicSignal(
            score = (proximity * confidence).coerceIn(0.0, 1.0),
            confidence = confidence,
            estimatedPeriodMs = period,
        )
    }

    private fun median(values: List<Long>): Long {
        val sorted = values.sorted()
        val middle = sorted.size / 2
        return if (sorted.size % 2 == 0) {
            (sorted[middle - 1] + sorted[middle]) / 2L
        } else {
            sorted[middle]
        }
    }

    private companion object {
        const val MIN_INTERVAL_MS = 30L * 60L * 1000L
        const val MIN_INTERVAL_SAMPLES = 2
        const val FULL_CONFIDENCE_SAMPLES = 4.0
        const val TOLERANCE_RATIO = 0.25
        const val MIN_TOLERANCE_MS = 60L * 60L * 1000L
    }
}

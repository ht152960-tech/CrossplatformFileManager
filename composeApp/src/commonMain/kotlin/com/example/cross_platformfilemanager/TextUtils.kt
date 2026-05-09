package com.example.cross_platformfilemanager

import kotlin.time.Clock

internal fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()

internal fun normalize(value: String): String {
    val trimmed = value.trim().lowercase()
    if (trimmed.isEmpty()) return ""
    return trimmed
        .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
}

internal fun tokenize(value: String): List<String> {
    val normalized = normalize(value)
    if (normalized.isBlank()) return emptyList()
    return normalized.split(" ").filter { it.isNotBlank() }
}

internal fun formatTagFilter(label: String, tag: String): String = label.replace("%s", tag)

internal fun formatCount(label: String, count: Int): String = label.replace("%d", count.toString())

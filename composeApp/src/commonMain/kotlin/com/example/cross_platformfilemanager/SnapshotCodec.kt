package com.example.cross_platformfilemanager

internal object SnapshotCodec {
    private const val FORMAT_VERSION = 7

    fun encode(snapshot: AppSnapshot): String {
        val out = StringBuilder()
        out.appendString(snapshot.schemaVersion.toString())
        out.appendString(snapshot.locale.name)
        out.appendString(snapshot.query)
        out.appendString(snapshot.selectedTag.orEmpty())
        out.appendString(snapshot.selectedFileType.orEmpty())
        out.appendString(if (snapshot.favoritesOnly) "1" else "0")
        out.appendString(snapshot.activeReferenceId.orEmpty())

        out.appendString(snapshot.references.size.toString())
        snapshot.references.forEach { reference ->
            out.appendString(reference.id)
            out.appendString(reference.title)
            out.appendString(reference.source)
            out.appendString(reference.sourceKind.name)
            out.appendString(reference.fileType)
            out.appendString(reference.fileSizeBytes?.toString().orEmpty())
            out.appendString(reference.coverArtSource.orEmpty())
            out.appendString(reference.notes)
            out.appendString(reference.createdAtMillis.toString())
            out.appendString(reference.lastOpenedAtMillis.toString())
            out.appendString(if (reference.isFavorite) "1" else "0")
            out.appendString(reference.tags.size.toString())
            reference.tags.forEach { tag ->
                out.appendString(tag)
            }
        }

        out.appendString(snapshot.recentSearches.size.toString())
        snapshot.recentSearches.forEach { search ->
            out.appendString(search)
        }

        out.appendString(snapshot.recommendationLogs.size.toString())
        snapshot.recommendationLogs.forEach { log ->
            out.appendString(log.id)
            out.appendString(log.query)
            out.appendString(log.selectedTag.orEmpty())
            out.appendString(log.generatedAtMillis.toString())
            out.appendString(log.topSuggestions.size.toString())
            log.topSuggestions.forEach { suggestion ->
                out.appendString(suggestion)
            }
        }

        val recommendationState = snapshot.recommendationState
        out.appendString(if (recommendationState == null) "0" else "1")
        if (recommendationState != null) {
            encodeRecommendationState(out, recommendationState)
        }
        return out.toString()
    }

    fun decode(payload: String): AppSnapshot? {
        if (payload.isBlank()) return null

        val cursor = Cursor(payload)
        return try {
            val firstToken = cursor.readString()
            val version = firstToken.toIntOrNull()
            if (version != null && version >= 2) {
                decodeBody(cursor, schemaVersion = version)
            } else {
                decodeBody(cursor, schemaVersion = 1, localeToken = firstToken)
            }
        } catch (_: Throwable) {
            null
        }
    }

    private fun decodeBody(
        cursor: Cursor,
        schemaVersion: Int,
        localeToken: String? = null,
    ): AppSnapshot {
        val locale = AppLocale.valueOf(localeToken ?: cursor.readString()).let { decoded ->
            if (schemaVersion < 4 && decoded == AppLocale.ZhCn) {
                AppLocale.EnUs
            } else {
                decoded
            }
        }
        val query = cursor.readString()
        val selectedTag = cursor.readString().takeIf { it.isNotBlank() }
        val selectedFileType = if (schemaVersion >= 3) cursor.readString().takeIf { it.isNotBlank() } else null
        val favoritesOnly = if (schemaVersion >= 3) cursor.readString() == "1" else false
        val activeReferenceId = cursor.readString().takeIf { it.isNotBlank() }

        val referenceCount = cursor.readString().toInt()
        val references = buildList {
            repeat(referenceCount) {
                val id = cursor.readString()
                val title = cursor.readString()
                val source = cursor.readString()
                val sourceKind = FileSourceKind.valueOf(cursor.readString())
                val fileType = cursor.readString()
                val fileSizeBytes = if (schemaVersion >= 5) cursor.readString().toLongOrNull() else null
                val coverArtSource = if (schemaVersion >= 7) cursor.readString().takeIf { it.isNotBlank() } else null
                val notes = cursor.readString()
                val createdAtMillis = cursor.readString().toLong()
                val lastOpenedAtMillis = cursor.readString().toLong()
                val isFavorite = cursor.readString() == "1"
                val tagCount = cursor.readString().toInt()
                val tags = buildList {
                    repeat(tagCount) { add(cursor.readString()) }
                }

                add(
                    FileReference(
                        id = id,
                        title = title,
                        source = source,
                        sourceKind = sourceKind,
                        fileType = fileType,
                        fileSizeBytes = fileSizeBytes,
                        coverArtSource = coverArtSource,
                        tags = tags,
                        notes = notes,
                        createdAtMillis = createdAtMillis,
                        lastOpenedAtMillis = lastOpenedAtMillis,
                        isFavorite = isFavorite,
                    )
                )
            }
        }

        val recentSearchCount = cursor.readString().toInt()
        val recentSearches = buildList {
            repeat(recentSearchCount) { add(cursor.readString()) }
        }

        val recommendationCount = cursor.readString().toInt()
        val recommendationLogs = buildList {
            repeat(recommendationCount) {
                val id = cursor.readString()
                val logQuery = cursor.readString()
                val logSelectedTag = cursor.readString().takeIf { it.isNotBlank() }
                val generatedAtMillis = cursor.readString().toLong()
                val suggestionCount = cursor.readString().toInt()
                val suggestions = buildList {
                    repeat(suggestionCount) { add(cursor.readString()) }
                }
                add(
                    RecommendationLog(
                        id = id,
                        query = logQuery,
                        selectedTag = logSelectedTag,
                        generatedAtMillis = generatedAtMillis,
                        topSuggestions = suggestions,
                    )
                )
            }
        }

        val recommendationState = if (schemaVersion >= 6 && cursor.readString() == "1") {
            decodeRecommendationState(cursor)
        } else {
            null
        }

        return AppSnapshot(
            schemaVersion = schemaVersion,
            locale = locale,
            query = query,
            selectedTag = selectedTag,
            selectedFileType = selectedFileType,
            favoritesOnly = favoritesOnly,
            activeReferenceId = activeReferenceId,
            references = references,
            recentSearches = recentSearches,
            recommendationLogs = recommendationLogs,
            recommendationState = recommendationState,
        )
    }

    private fun encodeRecommendationState(
        out: StringBuilder,
        recommendationState: RecommendationEngineSnapshot,
    ) {
        out.appendString(recommendationState.lastOpenedFileId.orEmpty())

        val filePatterns = recommendationState.filePatterns.entries.sortedBy { it.key }
        out.appendString(filePatterns.size.toString())
        filePatterns.forEach { entry ->
            val pattern = entry.value
            out.appendString(pattern.fileId)
            out.appendString(pattern.lastOpenTimeMillis.toString())
            out.appendString(pattern.estimatedPeriodMillis.toString())
            out.appendString(pattern.openCount.toString())
        }

        val counts = recommendationState.transitionSnapshot.counts.entries.sortedBy { it.key }
        out.appendString(counts.size.toString())
        counts.forEach { entry ->
            out.appendString(entry.key)
            val sortedBucket = entry.value.entries.sortedBy { it.key }
            out.appendString(sortedBucket.size.toString())
            sortedBucket.forEach { bucketEntry ->
                out.appendString(bucketEntry.key)
                out.appendString(bucketEntry.value.toString())
            }
        }

        val totals = recommendationState.transitionSnapshot.totals.entries.sortedBy { it.key }
        out.appendString(totals.size.toString())
        totals.forEach { entry ->
            out.appendString(entry.key)
            out.appendString(entry.value.toString())
        }

        out.appendString(recommendationState.weightSnapshot.baseIntervalWeight.toString())
        out.appendString(recommendationState.weightSnapshot.baseTransitionWeight.toString())
        out.appendString(recommendationState.weightSnapshot.baseRecencyWeight.toString())
        out.appendString(recommendationState.weightSnapshot.learnedIntervalWeight.toString())
        out.appendString(recommendationState.weightSnapshot.learnedTransitionWeight.toString())
        out.appendString(recommendationState.weightSnapshot.learnedRecencyWeight.toString())
    }

    private fun decodeRecommendationState(cursor: Cursor): RecommendationEngineSnapshot {
        val lastOpenedFileId = cursor.readString().takeIf { it.isNotBlank() }

        val filePatternCount = cursor.readString().toInt()
        val filePatterns = buildMap<String, FilePattern> {
            repeat(filePatternCount) {
                val fileId = cursor.readString()
                val lastOpenTimeMillis = cursor.readString().toLong()
                val estimatedPeriodMillis = cursor.readString().toLong()
                val openCount = cursor.readString().toInt()
                put(
                    fileId,
                    FilePattern(
                        fileId = fileId,
                        lastOpenTimeMillis = lastOpenTimeMillis,
                        estimatedPeriodMillis = estimatedPeriodMillis,
                        openCount = openCount,
                    ),
                )
            }
        }

        val transitionCount = cursor.readString().toInt()
        val counts = buildMap<String, Map<String, Int>> {
            repeat(transitionCount) {
                val from = cursor.readString()
                val bucketCount = cursor.readString().toInt()
                val bucket = buildMap<String, Int> {
                    repeat(bucketCount) {
                        val to = cursor.readString()
                        val count = cursor.readString().toInt()
                        put(to, count)
                    }
                }
                put(from, bucket)
            }
        }

        val totalsCount = cursor.readString().toInt()
        val totals = buildMap<String, Int> {
            repeat(totalsCount) {
                val from = cursor.readString()
                val total = cursor.readString().toInt()
                put(from, total)
            }
        }

        val weightSnapshot = WeightSnapshot(
            baseIntervalWeight = cursor.readString().toDouble(),
            baseTransitionWeight = cursor.readString().toDouble(),
            baseRecencyWeight = cursor.readString().toDouble(),
            learnedIntervalWeight = cursor.readString().toDouble(),
            learnedTransitionWeight = cursor.readString().toDouble(),
            learnedRecencyWeight = cursor.readString().toDouble(),
        )

        return RecommendationEngineSnapshot(
            filePatterns = filePatterns,
            transitionSnapshot = TransitionSnapshot(counts = counts, totals = totals),
            weightSnapshot = weightSnapshot,
            lastOpenedFileId = lastOpenedFileId,
        )
    }

    private class Cursor(private val text: String) {
        private var index = 0

        fun readString(): String {
            var lengthEnd = index
            while (lengthEnd < text.length && text[lengthEnd] != ':') {
                lengthEnd++
            }
            require(lengthEnd < text.length) { "Invalid snapshot payload" }
            val length = text.substring(index, lengthEnd).toInt()
            val start = lengthEnd + 1
            val end = start + length
            require(end <= text.length) { "Invalid snapshot payload" }
            val value = text.substring(start, end)
            index = end
            return value
        }
    }

    private fun StringBuilder.appendString(value: String) {
        append(value.length)
        append(':')
        append(value)
    }
}

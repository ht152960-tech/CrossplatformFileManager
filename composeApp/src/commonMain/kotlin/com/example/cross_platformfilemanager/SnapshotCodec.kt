package com.example.cross_platformfilemanager

internal object SnapshotCodec {
    private const val FORMAT_VERSION = 2

    fun encode(snapshot: AppSnapshot): String {
        val out = StringBuilder()
        out.appendString(snapshot.schemaVersion.toString())
        out.appendString(snapshot.locale.name)
        out.appendString(snapshot.query)
        out.appendString(snapshot.selectedTag.orEmpty())
        out.appendString(snapshot.activeReferenceId.orEmpty())

        out.appendString(snapshot.references.size.toString())
        snapshot.references.forEach { reference ->
            out.appendString(reference.id)
            out.appendString(reference.title)
            out.appendString(reference.source)
            out.appendString(reference.sourceKind.name)
            out.appendString(reference.fileType)
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
        return out.toString()
    }

    fun decode(payload: String): AppSnapshot? {
        if (payload.isBlank()) return null

        val cursor = Cursor(payload)
        return try {
            val firstToken = cursor.readString()
            if (firstToken.toIntOrNull() == FORMAT_VERSION) {
                decodeBody(cursor, schemaVersion = FORMAT_VERSION)
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
        val locale = AppLocale.valueOf(localeToken ?: cursor.readString())
        val query = cursor.readString()
        val selectedTag = cursor.readString().takeIf { it.isNotBlank() }
        val activeReferenceId = cursor.readString().takeIf { it.isNotBlank() }

        val referenceCount = cursor.readString().toInt()
        val references = buildList {
            repeat(referenceCount) {
                val id = cursor.readString()
                val title = cursor.readString()
                val source = cursor.readString()
                val sourceKind = FileSourceKind.valueOf(cursor.readString())
                val fileType = cursor.readString()
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

        return AppSnapshot(
            schemaVersion = schemaVersion,
            locale = locale,
            query = query,
            selectedTag = selectedTag,
            activeReferenceId = activeReferenceId,
            references = references,
            recentSearches = recentSearches,
            recommendationLogs = recommendationLogs,
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

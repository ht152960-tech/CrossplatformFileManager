package com.example.cross_platformfilemanager

//数据访问层，抽象文件数据来源�?
data class FileReference(
    val id: String,
    val title: String,
    val source: String,
    val sourceKind: FileSourceKind,
    val fileType: String,
    val fileSizeBytes: Long? = null,
    val coverArtSource: String? = null,
    val thumbnailPath: String? = null,
    val thumbnailStatus: ThumbnailStatus = ThumbnailStatus.NONE,
    val tags: List<String>,
    val notes: String,
    val createdAtMillis: Long,
    val modifiedAtMillis: Long = createdAtMillis,
    val lastOpenedAtMillis: Long,
    val isFavorite: Boolean = false,
)

enum class FileSourceKind {
    ManualPath,
    BrowserHandle,
    Url,
    RemoteReference,
}

data class SearchResult(
    val reference: FileReference,
    val score: Double,
    val reason: String,
    val matchedTagCount: Int = 0,
) {
    val scoreLabel: String get() = "Score ${(score * 100).toInt()}"
}

data class Suggestion(
    val label: String,
    val reason: String,
    val kind: SuggestionKind,
    val score: Double,
)

enum class SuggestionKind {
    Query,
    Tag,
    File,
}

data class RecommendationLog(
    val id: String,
    val query: String,
    val selectedTag: String?,
    val generatedAtMillis: Long,
    val topSuggestions: List<String>,
)

class InMemoryFileRepository {
    val references = androidx.compose.runtime.mutableStateListOf<FileReference>()

    val recentSearches = androidx.compose.runtime.mutableStateListOf<String>()

    val recommendationLogs = androidx.compose.runtime.mutableStateListOf<RecommendationLog>()

    fun addReference(reference: FileReference) {
        references.add(0, reference.copy(tags = normalizeTags(reference.tags)))
    }

    fun upsertReference(reference: FileReference): FileReference {
        val inserted = reference.copy(tags = normalizeTags(reference.tags))
        references.add(0, inserted)
        return inserted
    }

    fun replaceReference(referenceId: String, updatedReference: FileReference): FileReference? {
        val index = references.indexOfFirst { it.id == referenceId }
        if (index < 0) return null

        val current = references[index]
        val replaced = updatedReference.copy(
            id = current.id,
            createdAtMillis = current.createdAtMillis,
            modifiedAtMillis = nowMillis(),
            lastOpenedAtMillis = maxOf(current.lastOpenedAtMillis, updatedReference.lastOpenedAtMillis),
            isFavorite = current.isFavorite || updatedReference.isFavorite,
            tags = normalizeTags(updatedReference.tags),
        )
        references[index] = replaced
        return replaced
    }

    fun updateReference(referenceId: String, updater: (FileReference) -> FileReference) {
        val index = references.indexOfFirst { it.id == referenceId }
        if (index >= 0) {
            val updated = updater(references[index])
            references[index] = updated.copy(tags = normalizeTags(updated.tags))
        }
    }

    fun updateReferenceTags(referenceId: String, tags: List<String>) {
        val index = references.indexOfFirst { it.id == referenceId }
        if (index >= 0) {
            references[index] = references[index].copy(tags = normalizeTags(tags))
        }
    }

    fun removeTagEverywhere(tag: String): Int {
        val normalizedTag = normalize(tag)
        if (normalizedTag.isBlank()) return 0

        var changedCount = 0
        references.forEachIndexed { index, reference ->
            val updatedTags = reference.tags.filterNot { normalize(it) == normalizedTag }
            if (updatedTags.size != reference.tags.size) {
                references[index] = reference.copy(tags = normalizeTags(updatedTags))
                changedCount++
            }
        }
        return changedCount
    }

    fun deleteReference(referenceId: String): Boolean {
        val index = references.indexOfFirst { it.id == referenceId }
        if (index < 0) {
            return false
        }
        references.removeAt(index)
        return true
    }

    fun findReferenceById(referenceId: String): FileReference? =
        references.firstOrNull { it.id == referenceId }


    fun replaceReferences(items: List<FileReference>) {
        references.clear()
        references.addAll(items.map { reference ->
            reference.copy(tags = normalizeTags(reference.tags))
        })
    }

    fun replaceRecentSearches(items: List<String>) {
        recentSearches.clear()
                recentSearches.addAll(
            items
                .map(::normalize)
                .filter { it.isNotBlank() }
                .distinct()
                .take(12)
        )
    }

    fun replaceRecommendationLogs(items: List<RecommendationLog>) {
        recommendationLogs.clear()
                val cleaned = items
            .map { log ->
                log.copy(
                    query = normalize(log.query),
                    selectedTag = log.selectedTag?.trim()?.takeIf { it.isNotBlank() },
                    topSuggestions = log.topSuggestions
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .distinct(),
                )
            }
            .asReversed()
            .distinctBy { it.id }
            .asReversed()
            .sortedBy { it.generatedAtMillis }
        recommendationLogs.addAll(cleaned.takeLast(30))
    }

    fun clearAllData() {
        references.clear()
        recentSearches.clear()
        recommendationLogs.clear()
    }

    fun toggleFavorite(referenceId: String) {
        val index = references.indexOfFirst { it.id == referenceId }
        if (index >= 0) {
            val current = references[index]
            references[index] = current.copy(isFavorite = !current.isFavorite)
        }
    }

    fun open(referenceId: String) {
        val index = references.indexOfFirst { it.id == referenceId }
        if (index >= 0) {
            val current = references[index]
            val openedAtMillis = nowMillis()
                        if (current.lastOpenedAtMillis > 0L && openedAtMillis - current.lastOpenedAtMillis < RECENT_OPEN_NOISE_WINDOW_MILLIS) {
                return
            }
            references[index] = current.copy(lastOpenedAtMillis = openedAtMillis)
        }
    }

    fun recordSearch(query: String) {
        val normalized = normalize(query)
        if (normalized.isBlank()) return
                if (recentSearches.firstOrNull() == normalized) return
        recentSearches.remove(normalized)
        recentSearches.add(0, normalized)
        if (recentSearches.size > 12) {
            recentSearches.removeAt(recentSearches.lastIndex)
        }
    }

    fun recordRecommendation(
        query: String,
        selectedTag: String?,
        suggestions: List<Suggestion>,
    ) {
        val topSuggestions = suggestions.take(5).map { it.label }
        val normalizedQuery = normalize(query)
        val normalizedSelectedTag = selectedTag?.trim()?.takeIf { it.isNotBlank() }

        val lastLog = recommendationLogs.lastOrNull()
        if (
            lastLog != null &&
            lastLog.query == normalizedQuery &&
            lastLog.selectedTag == normalizedSelectedTag &&
            lastLog.topSuggestions == topSuggestions
        ) {
            return
        }

        recommendationLogs.add(
            RecommendationLog(
                id = "rec-${nowMillis()}",
                query = normalizedQuery,
                selectedTag = normalizedSelectedTag,
                generatedAtMillis = nowMillis(),
                topSuggestions = topSuggestions,
            )
        )
        while (recommendationLogs.size > 30) {
            recommendationLogs.removeAt(0)
        }
    }

    fun allTags(): List<String> = references
        .flatMap { it.tags }
        .groupingBy { it }
        .eachCount()
        .entries
        .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
        .map { it.key }

    fun topTags(limit: Int = 10): List<String> = allTags().take(limit)

    fun recentReferences(limit: Int = 5): List<FileReference> = references
        .sortedByDescending { it.lastOpenedAtMillis }
        .take(limit)

    fun search(searchTags: List<SearchTag>): List<SearchResult> {
        val activeTags = searchTags
            .map { tag -> tag.copy(value = normalizeSearchTagToken(tag.value)) }
            .filter { it.value.isNotBlank() }
            .distinctBy { normalize(it.value) }

        if (activeTags.isEmpty()) {
            return emptyList()
        }

        return references
            .mapNotNull { reference ->
                val matchDetails = activeTags.mapNotNull { tag ->
                    buildSearchTagMatch(reference, tag)
                }
                if (matchDetails.isEmpty()) {
                    return@mapNotNull null
                }

                val matchedTagCount = matchDetails.size
                val fieldScore = matchDetails.sumOf { it.score }
                val score = matchedTagCount * 1000.0 + fieldScore
                SearchResult(
                    reference = reference,
                    score = score,
                    matchedTagCount = matchedTagCount,
                    reason = matchDetails.joinToString(separator = "、") { detail ->
                        "${detail.tagValue}（${detail.fieldLabel}）"
                    },
                )
            }
            .sortedWith(
                compareByDescending<SearchResult> { it.matchedTagCount }
                    .thenByDescending { it.score }
                    .thenByDescending { it.reference.lastOpenedAtMillis }
                    .thenByDescending { it.reference.createdAtMillis }
                    .thenBy { normalize(it.reference.title) },
            )
    }

    private fun normalizeTags(tags: List<String>): List<String> =
        tags.map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

    private fun buildSearchTagMatch(reference: FileReference, tag: SearchTag): SearchTagMatch? {
        val normalizedToken = normalizeSearchTagToken(tag.value)
        if (normalizedToken.isBlank()) return null

        val titleMatch = scoreFieldMatch(reference.title, normalizedToken)
        val tagMatch = reference.tags.maxOfOrNull { scoreFieldMatch(it, normalizedToken) ?: Double.NEGATIVE_INFINITY }
            ?.takeIf { it > 0.0 }
        val typeMatch = scoreFieldMatch(reference.fileType, normalizedToken)
        val pathMatch = scoreFieldMatch(reference.source, normalizedToken)

        val candidates = buildList {
            titleMatch?.let {
                add(SearchTagMatch(normalizedToken, "文件名", it + 400.0))
            }
            tagMatch?.let {
                add(SearchTagMatch(normalizedToken, "标签", it + if (tag.source == SearchTagSource.LibraryTag) 320.0 else 300.0))
            }
            typeMatch?.let {
                add(SearchTagMatch(normalizedToken, "类型", it + 200.0))
            }
            pathMatch?.let {
                add(SearchTagMatch(normalizedToken, "路径", it + 100.0))
            }
        }
        return candidates.maxByOrNull { it.score }
    }

    private fun scoreFieldMatch(raw: String, token: String): Double? {
        val normalizedRaw = normalizeSearchField(raw)
        val normalizedToken = normalizeSearchField(token)
        if (normalizedRaw.isBlank() || normalizedToken.isBlank()) return null

        return when {
            normalizedRaw == normalizedToken -> 100.0
            normalizedRaw.startsWith(normalizedToken) -> 88.0
            shouldAllowContainsMatch(normalizedToken) && normalizedRaw.contains(normalizedToken) -> 74.0
            else -> null
        }
    }

    private fun shouldAllowContainsMatch(token: String): Boolean {
        if (token.isBlank()) return false
        val isAllCjk = token.all(::isCjk)
        return when {
            isAllCjk -> token.length >= 2
            else -> token.length >= 3
        }
    }

    private data class SearchTagMatch(
        val tagValue: String,
        val fieldLabel: String,
        val score: Double,
    )

    private companion object {
        const val RECENT_OPEN_NOISE_WINDOW_MILLIS = 2L * 60L * 1000L
    }
}










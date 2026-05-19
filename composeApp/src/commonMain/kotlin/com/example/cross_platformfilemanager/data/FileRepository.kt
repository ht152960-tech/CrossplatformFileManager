package com.example.cross_platformfilemanager

//数据访问层，抽象文件数据来源。
data class FileReference(
    val id: String,
    val title: String,
    val source: String,
    val sourceKind: FileSourceKind,
    val fileType: String,
    val fileSizeBytes: Long? = null,
    val coverArtSource: String? = null,
    val tags: List<String>,
    val notes: String,
    val createdAtMillis: Long,
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
) {
    val scoreLabel: String get() = "Score ${(score * 100).toInt()}"
}

data class Suggestion(
    val label: String,
    val reason: String,
    val kind: SuggestionKind,
    val score: Double,
)

data class TagSummary(
    val tag: String,
    val referenceCount: Int,
)

data class FileTypeSummary(
    val fileType: String,
    val referenceCount: Int,
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

data class DashboardStats(
    val fileCount: Int,
    val tagCount: Int,
    val recentSearchCount: Int,
    val recommendationCount: Int,
)

class InMemoryFileRepository {
    val references = androidx.compose.runtime.mutableStateListOf<FileReference>()

    val recentSearches = androidx.compose.runtime.mutableStateListOf<String>()

    val recommendationLogs = androidx.compose.runtime.mutableStateListOf<RecommendationLog>()

    fun addReference(reference: FileReference) {
        upsertReference(reference)
    }

    fun upsertReference(reference: FileReference): FileReference {
        val normalizedSource = normalizeSource(reference.source)
        val indexBySource = if (normalizedSource.isBlank()) {
            -1
        } else {
            // 空 source 不能当作稳定身份键，否则两个不同文件会被误认为同一个引用。
            references.indexOfFirst { normalizeSource(it.source) == normalizedSource }
        }
        val indexById = if (indexBySource >= 0) {
            indexBySource
        } else {
            references.indexOfFirst { it.id == reference.id }
        }

        if (indexById >= 0) {
            val current = references[indexById]
            val merged = reference.copy(
                id = current.id,
                createdAtMillis = current.createdAtMillis,
                lastOpenedAtMillis = maxOf(current.lastOpenedAtMillis, reference.lastOpenedAtMillis),
                isFavorite = current.isFavorite || reference.isFavorite,
                fileSizeBytes = reference.fileSizeBytes ?: current.fileSizeBytes,
                tags = normalizeTags(reference.tags),
            )
            references[indexById] = merged
            return merged
        }

        val inserted = reference.copy(tags = normalizeTags(reference.tags))
        references.add(0, inserted)
        return inserted
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

    fun findReferenceBySource(source: String): FileReference? {
        val normalizedSource = normalizeSource(source)
        return references.firstOrNull { normalizeSource(it.source) == normalizedSource }
    }

    fun hasReferenceWithSource(source: String): Boolean =
        findReferenceBySource(source) != null

    fun replaceReferences(items: List<FileReference>) {
        references.clear()
        // 恢复快照或合并外部数据时，顺手做一次清洗，避免重复引用把列表和推荐信号一起污染。
        val sanitized = linkedMapOf<String, FileReference>()
        items.forEach { reference ->
            val id = reference.id.trim()
            if (id.isBlank()) return@forEach

            val sourceKey = normalizeSource(reference.source)
            val dedupeKey = if (sourceKey.isNotBlank()) {
                "source:$sourceKey"
            } else {
                "id:$id"
            }

            sanitized[dedupeKey] = reference.copy(tags = normalizeTags(reference.tags))
        }
        references.addAll(sanitized.values)
    }

    fun replaceRecentSearches(items: List<String>) {
        recentSearches.clear()
        // 导入快照时顺手清洗搜索词，避免空字符串、重复项和大小写抖动污染最近搜索。
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
        // 恢复时把历史推荐日志也做一次轻量清洗，避免脏快照把重复记录重新灌回去。
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
            // 同一个文件在很短时间内被重复打开时，通常是连点、刷新或回看，不把它当成新的“最近打开”信号。
            if (current.lastOpenedAtMillis > 0L && openedAtMillis - current.lastOpenedAtMillis < RECENT_OPEN_NOISE_WINDOW_MILLIS) {
                return
            }
            references[index] = current.copy(lastOpenedAtMillis = openedAtMillis)
        }
    }

    fun recordSearch(query: String) {
        val normalized = normalize(query)
        if (normalized.isBlank()) return
        // 连续输入相同查询时，不重复挤占最近搜索位置，避免把重复刷新当成新搜索信号。
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

        // 如果连续生成的推荐内容完全一样，就不重复记一条历史，避免刷新或重组把日志撑大。
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

    fun tagSummaries(limit: Int = Int.MAX_VALUE): List<TagSummary> {
        val counts = linkedMapOf<String, Pair<String, Int>>()

        references.forEach { reference ->
            reference.tags
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinctBy(::normalize)
                .forEach { tag ->
                    val normalizedTag = normalize(tag)
                    val current = counts[normalizedTag]
                    if (current == null) {
                        counts[normalizedTag] = tag to 1
                    } else {
                        counts[normalizedTag] = current.first to (current.second + 1)
                    }
                }
        }

        return counts.values
            .map { (tag, count) -> TagSummary(tag = tag, referenceCount = count) }
            .sortedWith(compareByDescending<TagSummary> { it.referenceCount }.thenBy { normalize(it.tag) })
            .take(limit)
    }

    fun fileTypeSummaries(limit: Int = Int.MAX_VALUE): List<FileTypeSummary> {
        val counts = linkedMapOf<String, Pair<String, Int>>()

        references.forEach { reference ->
            val fileType = reference.fileType.trim().ifBlank { "FILE" }
            val normalizedType = normalize(fileType)
            val current = counts[normalizedType]
            if (current == null) {
                counts[normalizedType] = fileType to 1
            } else {
                counts[normalizedType] = current.first to (current.second + 1)
            }
        }

        return counts.values
            .map { (fileType, count) -> FileTypeSummary(fileType = fileType, referenceCount = count) }
            .sortedWith(compareByDescending<FileTypeSummary> { it.referenceCount }.thenBy { normalize(it.fileType) })
            .take(limit)
    }

    fun topTags(limit: Int = 10): List<String> = allTags().take(limit)

    fun recentReferences(limit: Int = 5): List<FileReference> = references
        .sortedByDescending { it.lastOpenedAtMillis }
        .take(limit)

    fun search(
        query: String,
        selectedTag: String?,
        selectedFileType: String?,
        favoritesOnly: Boolean,
    ): List<SearchResult> {
        val normalizedQuery = normalize(query)
        val normalizedTagFilter = selectedTag?.let(::normalize)
        val normalizedTypeFilter = selectedFileType?.let(::normalize)

        if (normalizedQuery.isBlank() && normalizedTagFilter == null && normalizedTypeFilter == null && !favoritesOnly) {
            return emptyList()
        }

        return references
            .map { reference ->
                val title = normalize(reference.title)
                var score = 0.0
                val reasons = mutableListOf<String>()

                val queryMatch = if (normalizedQuery.isBlank()) {
                    0.0
                } else {
                    val queryTokens = tokenize(normalizedQuery)
                    val tokenScores = queryTokens.map { token -> titleMatchScore(title, token) }
                    if (tokenScores.isEmpty() || tokenScores.any { it <= 0.0 }) {
                        0.0
                    } else {
                        tokenScores.average()
                    }
                }

                if (queryMatch > 0.0) {
                    score += queryMatch
                    reasons += "title match"
                }

                if (normalizedTagFilter != null && reference.tags.any { normalize(it) == normalizedTagFilter }) {
                    score += 0.30
                    reasons += "tag filter"
                }

                if (normalizedTypeFilter != null && normalize(reference.fileType) == normalizedTypeFilter) {
                    score += 0.20
                    reasons += "type filter"
                }

                if (favoritesOnly && reference.isFavorite) {
                    score += 0.18
                    reasons += "favorite only"
                }

                SearchResult(
                    reference = reference,
                    score = score,
                    reason = if (reasons.isEmpty()) {
                        if (normalizedQuery.isBlank()) "general candidate" else "title candidate"
                    } else {
                        reasons.distinct().joinToString(", ")
                    },
                )
            }
            .filter {
                val matchesTag = normalizedTagFilter == null || it.reference.tags.any { tag -> normalize(tag) == normalizedTagFilter }
                val matchesType = normalizedTypeFilter == null || normalize(it.reference.fileType) == normalizedTypeFilter
                val matchesFavorite = !favoritesOnly || it.reference.isFavorite
                val matchesQuery = normalizedQuery.isBlank() || normalizedTagFilter != null || normalizedTypeFilter != null || favoritesOnly || it.score > 0.0
                matchesQuery && matchesTag && matchesType && matchesFavorite
            }
            .sortedWith(
                compareByDescending<SearchResult> { it.score }
                    .thenByDescending { it.reference.lastOpenedAtMillis }
                    .thenBy { it.reference.title },
            )
    }

    fun stats(): DashboardStats = DashboardStats(
        fileCount = references.size,
        tagCount = allTags().size,
        recentSearchCount = recentSearches.size,
        recommendationCount = recommendationLogs.size,
    )

    private fun normalizeSource(source: String): String = source.trim().lowercase()

    private fun normalizeTags(tags: List<String>): List<String> =
        tags.map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

    private fun titleMatchScore(title: String, token: String): Double {
        if (token.isBlank()) return 0.0
        return when {
            title == token -> 1.0
            title.startsWith(token) -> 0.92
            title.contains(token) -> 0.78
            isSubsequence(title, token) -> 0.60
            else -> 0.0
        }
    }

    private fun isSubsequence(text: String, pattern: String): Boolean {
        var textIndex = 0
        var patternIndex = 0
        while (textIndex < text.length && patternIndex < pattern.length) {
            if (text[textIndex] == pattern[patternIndex]) {
                patternIndex++
            }
            textIndex++
        }
        return patternIndex == pattern.length
    }

    private companion object {
        // 这个窗口只用于去掉短时间重复打开的噪声，不影响正常的后续推荐学习。
        const val RECENT_OPEN_NOISE_WINDOW_MILLIS = 2L * 60L * 1000L
    }
}

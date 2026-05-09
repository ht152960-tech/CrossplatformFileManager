package com.example.cross_platformfilemanager

//数据访问层，抽象文件数据来源。
data class FileReference(
    val id: String,
    val title: String,
    val source: String,
    val sourceKind: FileSourceKind,
    val fileType: String,
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
    val references = androidx.compose.runtime.mutableStateListOf(
        FileReference(
            id = "ref-001",
            title = "Quarterly contract draft",
            source = "/docs/legal/contract-q3-draft.docx",
            sourceKind = FileSourceKind.ManualPath,
            fileType = "DOCX",
            tags = listOf("contract", "legal", "draft"),
            notes = "Needs approval and a signature pass.",
            createdAtMillis = 1710000000000,
            lastOpenedAtMillis = 1710100000000,
            isFavorite = true,
        ),
        FileReference(
            id = "ref-002",
            title = "Product roadmap",
            source = "/notes/roadmap-2026.md",
            sourceKind = FileSourceKind.ManualPath,
            fileType = "MD",
            tags = listOf("product", "planning", "roadmap"),
            notes = "Quarterly milestone overview.",
            createdAtMillis = 1710200000000,
            lastOpenedAtMillis = 1710500000000,
        ),
        FileReference(
            id = "ref-003",
            title = "Invoice archive",
            source = "/finance/invoices/2026",
            sourceKind = FileSourceKind.ManualPath,
            fileType = "FOLDER",
            tags = listOf("finance", "invoice", "archive"),
            notes = "Folder-like reference for repeated searches.",
            createdAtMillis = 1710300000000,
            lastOpenedAtMillis = 1710600000000,
        ),
        FileReference(
            id = "ref-004",
            title = "UI mockup board",
            source = "figma://design-system-v2",
            sourceKind = FileSourceKind.RemoteReference,
            fileType = "FIGMA",
            tags = listOf("ui", "design", "mockup"),
            notes = "Shared visual direction for the app.",
            createdAtMillis = 1710400000000,
            lastOpenedAtMillis = 1710550000000,
        ),
        FileReference(
            id = "ref-005",
            title = "Research note pack",
            source = "/research/notes-ml-retrieval.txt",
            sourceKind = FileSourceKind.ManualPath,
            fileType = "TXT",
            tags = listOf("research", "retrieval", "recommendation"),
            notes = "Useful for search and ranking experiments.",
            createdAtMillis = 1710450000000,
            lastOpenedAtMillis = 1710700000000,
        ),
    )

    val recentSearches = androidx.compose.runtime.mutableStateListOf(
        "contract",
        "roadmap",
        "invoice",
    )

    val recommendationLogs = androidx.compose.runtime.mutableStateListOf<RecommendationLog>()

    fun addReference(reference: FileReference) {
        upsertReference(reference)
    }

    fun upsertReference(reference: FileReference): FileReference {
        val normalizedSource = normalizeSource(reference.source)
        val indexBySource = references.indexOfFirst { normalizeSource(it.source) == normalizedSource }
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
        references.addAll(items)
    }

    fun replaceRecentSearches(items: List<String>) {
        recentSearches.clear()
        recentSearches.addAll(items.distinct())
    }

    fun replaceRecommendationLogs(items: List<RecommendationLog>) {
        recommendationLogs.clear()
        recommendationLogs.addAll(items)
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
            references[index] = current.copy(lastOpenedAtMillis = nowMillis())
        }
    }

    fun recordSearch(query: String) {
        val normalized = normalize(query)
        if (normalized.isBlank()) return
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
        recommendationLogs.add(
            RecommendationLog(
                id = "rec-${nowMillis()}",
                query = normalize(query),
                selectedTag = selectedTag?.trim()?.takeIf { it.isNotBlank() },
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
        val tokens = tokenize(normalizedQuery)
        val normalizedTagFilter = selectedTag?.let(::normalize)
        val normalizedTypeFilter = selectedFileType?.let(::normalize)

        return references
            .map { reference ->
                val title = normalize(reference.title)
                val source = normalize(reference.source)
                val notes = normalize(reference.notes)
                val tagText = reference.tags.joinToString(" ") { normalize(it) }
                val fileTypeText = normalize(reference.fileType)

                var score = 0.0
                val reasons = mutableListOf<String>()

                if (normalizedQuery.isBlank()) {
                    score += 0.20
                    reasons += "ready for browsing"
                }

                if (normalizedTagFilter != null && reference.tags.any { normalize(it) == normalizedTagFilter }) {
                    score += 0.35
                    reasons += "matches tag \"$selectedTag\""
                }

                if (normalizedTypeFilter != null && fileTypeText == normalizedTypeFilter) {
                    score += 0.22
                    reasons += "matches type \"$selectedFileType\""
                }

                if (favoritesOnly && reference.isFavorite) {
                    score += 0.18
                    reasons += "favorite only"
                }

                tokens.forEach { token ->
                    when {
                        title == token -> {
                            score += 0.45
                            reasons += "title match"
                        }
                        title.startsWith(token) -> {
                            score += 0.30
                            reasons += "title prefix"
                        }
                        title.contains(token) -> {
                            score += 0.20
                            reasons += "title contains"
                        }
                    }

                    when {
                        source.contains(token) -> {
                            score += 0.16
                            reasons += "source contains"
                        }
                        notes.contains(token) -> {
                            score += 0.12
                            reasons += "notes contain"
                        }
                        tagText.contains(token) -> {
                            score += 0.28
                            reasons += "tag match"
                        }
                    }
                }

                if (reference.isFavorite) {
                    score += 0.05
                    reasons += "favorite"
                }

                if (recentSearches.any { normalize(it).contains(normalizedQuery) && normalizedQuery.isNotBlank() }) {
                    score += 0.05
                    reasons += "recently searched"
                }

                if (reference.lastOpenedAtMillis > 0L) {
                    score += 0.10
                }

                SearchResult(
                    reference = reference,
                    score = score,
                    reason = if (reasons.isEmpty()) "general candidate" else reasons.distinct().joinToString(", "),
                )
            }
            .filter {
                val matchesTag = normalizedTagFilter == null || it.reference.tags.any { tag -> normalize(tag) == normalizedTagFilter }
                val matchesType = normalizedTypeFilter == null || normalize(it.reference.fileType) == normalizedTypeFilter
                val matchesFavorite = !favoritesOnly || it.reference.isFavorite
                (it.score > 0.15 || normalizedQuery.isBlank() || normalizedTagFilter != null || normalizedTypeFilter != null || favoritesOnly) &&
                    matchesTag && matchesType && matchesFavorite
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
}

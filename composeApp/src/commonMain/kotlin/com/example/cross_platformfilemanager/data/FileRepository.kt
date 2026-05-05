package com.example.cross_platformfilemanager

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
        references.add(0, reference)
    }

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

    fun topTags(limit: Int = 10): List<String> = allTags().take(limit)

    fun recentReferences(limit: Int = 5): List<FileReference> = references
        .sortedByDescending { it.lastOpenedAtMillis }
        .take(limit)

    fun search(query: String, selectedTag: String?): List<SearchResult> {
        val normalizedQuery = normalize(query)
        val tokens = tokenize(normalizedQuery)

        return references
            .map { reference ->
                val title = normalize(reference.title)
                val source = normalize(reference.source)
                val notes = normalize(reference.notes)
                val tagText = reference.tags.joinToString(" ") { normalize(it) }

                var score = 0.0
                val reasons = mutableListOf<String>()

                if (normalizedQuery.isBlank()) {
                    score += 0.20
                    reasons += "ready for browsing"
                }

                if (selectedTag != null && reference.tags.any { normalize(it) == normalize(selectedTag) }) {
                    score += 0.35
                    reasons += "matches tag \"$selectedTag\""
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
            .filter { it.score > 0.15 || normalizedQuery.isBlank() || selectedTag != null }
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
}

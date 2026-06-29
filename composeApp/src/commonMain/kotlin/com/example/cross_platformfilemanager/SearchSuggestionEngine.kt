package com.example.cross_platformfilemanager

class SearchSuggestionEngine {
    fun suggest(
        query: String,
        references: List<FileReference>,
        recentSearches: List<String>,
        selectedTag: String?,
    ): List<Suggestion> {
        val normalizedQuery = normalize(query)
        val tokens = normalizedQuery.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (normalizedQuery.isBlank()) {
            val recent = recentSearches.take(4).mapIndexed { index, value ->
                Suggestion(value, "recent query", SuggestionKind.Query, 1.0 - index * 0.05)
            }
            val tags = references.flatMap { it.tags }
                .groupingBy(::normalize)
                .eachCount()
                .entries
                .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
                .take(5)
                .mapIndexed { index, entry ->
                    Suggestion(entry.key, "popular tag", SuggestionKind.Tag, 0.9 - index * 0.05)
                }
            val files = references.sortedByDescending { it.lastOpenedAtMillis }.take(4).mapIndexed { index, file ->
                Suggestion(file.title, "recent file", SuggestionKind.File, 0.85 - index * 0.04)
            }
            return (recent + tags + files).distinctBy { normalize(it.label) }.sortedByDescending { it.score }
        }

        val suggestions = mutableListOf<Suggestion>()
        references.forEach { reference ->
            val fields = listOf(reference.title, reference.source) + reference.tags
            val matched = tokens.count { token -> fields.any { normalize(it).contains(token) } }
            if (matched > 0) {
                suggestions += Suggestion(
                    label = reference.title,
                    reason = "file match",
                    kind = SuggestionKind.File,
                    score = matched.toDouble(),
                )
            }
        }
        references.flatMap { it.tags }.distinctBy(::normalize).forEach { tag ->
            val score = tokens.count { normalize(tag).contains(it) }.toDouble()
            if (score > 0.0) suggestions += Suggestion(tag, "tag match", SuggestionKind.Tag, score)
        }
        if (!selectedTag.isNullOrBlank()) {
            suggestions += Suggestion(selectedTag, "current filter", SuggestionKind.Tag, 0.4)
        }
        return suggestions
            .distinctBy { "${it.kind}:${normalize(it.label)}" }
            .sortedWith(compareByDescending<Suggestion> { it.score }.thenBy { it.label })
            .take(8)
    }

    private fun normalize(value: String): String = value.trim().lowercase()
}

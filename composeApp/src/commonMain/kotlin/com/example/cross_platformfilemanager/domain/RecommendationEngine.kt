package com.example.cross_platformfilemanager

//领域逻辑，名字看起来像文件推荐/智能建议相关。
class RecommendationEngine {
    fun suggest(
        query: String,
        references: List<FileReference>,
        recentSearches: List<String>,
        selectedTag: String?,
    ): List<Suggestion> {
        val normalizedQuery = normalize(query)
        val tokens = tokenize(normalizedQuery)
        val tagFrequency = references
            .flatMap { it.tags }
            .groupingBy { normalize(it) }
            .eachCount()

        val tagCoOccurrence = buildCoOccurrence(references)
        val suggestions = mutableListOf<Suggestion>()

        if (normalizedQuery.isBlank()) {
            recentSearches.take(4).forEachIndexed { index, recent ->
                suggestions += Suggestion(
                    label = recent,
                    reason = "recent query",
                    kind = SuggestionKind.Query,
                    score = 1.0 - (index * 0.05),
                )
            }

            tagFrequency.entries
                .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
                .take(5)
                .forEachIndexed { index, entry ->
                    suggestions += Suggestion(
                        label = entry.key,
                        reason = "popular tag",
                        kind = SuggestionKind.Tag,
                        score = 0.9 - (index * 0.05),
                    )
                }

            references.sortedByDescending { it.lastOpenedAtMillis }.take(4).forEachIndexed { index, reference ->
                suggestions += Suggestion(
                    label = reference.title,
                    reason = "recent file",
                    kind = SuggestionKind.File,
                    score = 0.85 - (index * 0.04),
                )
            }

            return suggestions
                .distinctBy { normalize(it.label) }
                .sortedByDescending { it.score }
        }

        references.forEach { reference ->
            val title = normalize(reference.title)
            val source = normalize(reference.source)
            val notes = normalize(reference.notes)
            val combinedTags = reference.tags.map { normalize(it) }

            var score = 0.0
            val reasons = mutableListOf<String>()

            tokens.forEach { token ->
                if (title == token) {
                    score += 1.0
                    reasons += "exact title"
                } else if (title.startsWith(token)) {
                    score += 0.75
                    reasons += "title prefix"
                } else if (title.contains(token)) {
                    score += 0.55
                    reasons += "title contains"
                }

                if (source.contains(token)) {
                    score += 0.25
                    reasons += "source match"
                }
                if (notes.contains(token)) {
                    score += 0.20
                    reasons += "notes match"
                }
                if (combinedTags.any { it.contains(token) }) {
                    score += 0.65
                    reasons += "tag match"
                }
            }

            if (selectedTag != null && reference.tags.any { normalize(it) == normalize(selectedTag) }) {
                score += 0.35
                reasons += "selected tag"
            }

            if (score > 0.0) {
                suggestions += Suggestion(
                    label = reference.title,
                    reason = reasons.distinct().joinToString(", "),
                    kind = SuggestionKind.File,
                    score = score,
                )
            }
        }

        references.forEach { reference ->
            reference.tags.forEach { tag ->
                val normalizedTag = normalize(tag)
                var score = 0.0
                val reasons = mutableListOf<String>()

                tokens.forEach { token ->
                    when {
                        normalizedTag == token -> {
                            score += 0.9
                            reasons += "exact tag"
                        }
                        normalizedTag.startsWith(token) -> {
                            score += 0.7
                            reasons += "tag prefix"
                        }
                        normalizedTag.contains(token) -> {
                            score += 0.5
                            reasons += "tag contains"
                        }
                    }
                }

                if (selectedTag != null && normalize(selectedTag) == normalizedTag) {
                    score += 0.4
                    reasons += "current filter"
                }

                tagCoOccurrence[selectedTag?.let(::normalize) ?: normalizedTag]
                    ?.get(normalizedTag)
                    ?.let { weight ->
                        score += weight / 10.0
                        reasons += "co-occurrence"
                    }

                if (score > 0.0) {
                    suggestions += Suggestion(
                        label = tag,
                        reason = reasons.distinct().joinToString(", "),
                        kind = SuggestionKind.Tag,
                        score = score,
                    )
                }
            }
        }

        return suggestions
            .distinctBy { "${it.kind}:${normalize(it.label)}" }
            .sortedWith(compareByDescending<Suggestion> { it.score }.thenBy { it.label })
            .take(8)
    }

    private fun buildCoOccurrence(references: List<FileReference>): Map<String, Map<String, Int>> {
        val map = mutableMapOf<String, MutableMap<String, Int>>()

        references.forEach { reference ->
            val tags = reference.tags.map { normalize(it) }.distinct()
            tags.forEach { left ->
                val bucket = map.getOrPut(left) { mutableMapOf() }
                tags.forEach { right ->
                    if (left != right) {
                        bucket[right] = (bucket[right] ?: 0) + 1
                    }
                }
            }
        }

        return map
    }
}


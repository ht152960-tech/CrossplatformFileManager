package com.example.cross_platformfilemanager

import kotlin.math.exp

class RecommendationEngine(
    private val stateDao: RecommendationStateDao = InMemoryRecommendationRepository(),
    private val eventDao: RecommendationEventDao? = null,
    val filePatternStore: FilePatternStore = FilePatternStore(),
    val transitionStore: TransitionStore = TransitionStore(),
    val weightStore: WeightStore = WeightStore(),
    learner: OnlineLearner? = null,
) {
    private val onlineLearner: OnlineLearner = learner ?: OnlineLearner(weightStore)
    private val resolvedEventDao: RecommendationEventDao =
        eventDao ?: (stateDao as? RecommendationEventDao) ?: InMemoryRecommendationEventDao()
    private var lastOpenedFileId: String? = null

    init {
        stateDao.loadState()?.let { restoreSnapshot(it) }
    }

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

    fun exportSnapshot(): RecommendationEngineSnapshot = RecommendationEngineSnapshot(
        filePatterns = filePatternStore.snapshot(),
        transitionSnapshot = transitionStore.snapshot(),
        weightSnapshot = weightStore.snapshot(),
        lastOpenedFileId = lastOpenedFileId,
    )

    fun restoreSnapshot(snapshot: RecommendationEngineSnapshot?) {
        if (snapshot == null) {
            clear()
            return
        }
        filePatternStore.restore(snapshot.filePatterns)
        transitionStore.restore(snapshot.transitionSnapshot)
        weightStore.restore(snapshot.weightSnapshot)
        lastOpenedFileId = snapshot.lastOpenedFileId
        persist()
    }

    fun clear() {
        filePatternStore.clear()
        transitionStore.clear()
        weightStore.resetLearning()
        lastOpenedFileId = null
        stateDao.clearState()
        resolvedEventDao.clearEvents()
    }

    fun recordFileOpen(
        fileId: String,
        openedAtMillis: Long = nowMillis(),
        previousFileId: String? = lastOpenedFileId,
    ): FileOpenLog {
        val log = FileOpenLog(
            fileId = fileId,
            openedAtMillis = openedAtMillis,
            previousFileId = previousFileId,
        )

        filePatternStore.recordOpen(log)
        if (!previousFileId.isNullOrBlank()) {
            transitionStore.recordTransition(previousFileId, fileId)
        }
        lastOpenedFileId = fileId
        resolvedEventDao.appendOpenEvent(log)
        persist()
        return log
    }

    fun recommend(
        references: List<FileReference>,
        previousFileId: String? = lastOpenedFileId,
        nowMillis: Long = nowMillis(),
        limit: Int = 10,
    ): List<ScoredRecommendation> {
        if (limit <= 0 || references.isEmpty()) return emptyList()
        val contextFileId = previousFileId?.takeIf { it.isNotBlank() }
        val scoredCandidates = references
            .asSequence()
            .filter { it.id.isNotBlank() }
            // 候选列表做一次最末端去重，避免上游重复引用把同一个文件算进推荐结果两次。
            .distinctBy { recommendationDedupKey(it) }
            .filterNot { contextFileId != null && it.id == contextFileId }
            .map { reference ->
                val intervalScore = filePatternStore.intervalScore(reference.id, nowMillis)
                val transitionScore = transitionStore.transitionScore(contextFileId, reference.id)
                val recencyScore = recencyScore(reference.lastOpenedAtMillis, nowMillis)
                val openCount = filePatternStore.get(reference.id)?.openCount ?: 0
                val confidence = recommendationConfidence(
                    openCount = openCount,
                    hasContext = contextFileId != null,
                )
                val structuralScore =
                    (weightStore.totalIntervalWeight() * intervalScore) +
                        (weightStore.totalTransitionWeight() * transitionScore) +
                        (weightStore.totalRecencyWeight() * recencyScore)
                // 样本少或者上下文缺失时，不让结构化信号单独决定排序，而是回退到更稳的最近打开时间。
                val finalScore =
                    (structuralScore * confidence) +
                        (recencyScore * (1.0 - confidence))

                ScoredRecommendation(
                    file = reference,
                    intervalScore = intervalScore,
                    transitionScore = transitionScore,
                    recencyScore = recencyScore,
                    finalScore = finalScore,
                )
            }
            .sortedWith(
                compareByDescending<ScoredRecommendation> { it.finalScore }
                    // 分数接近时，优先选择历史样本更多的文件，减少低样本偶然性。
                    .thenByDescending { filePatternStore.get(it.file.id)?.openCount ?: 0 }
                    .thenByDescending { it.file.lastOpenedAtMillis }
                    .thenBy { normalize(it.file.title) }
                    .thenBy { it.file.id },
            )
            .toList()

        return selectWithDiversity(scoredCandidates, limit)
    }

    fun recordRecommendationClick(
        clickedFileId: String,
        shownRecommendations: List<ScoredRecommendation>,
        openedAtMillis: Long = nowMillis(),
        previousFileId: String? = lastOpenedFileId,
    ): FileOpenLog? {
        val clicked = shownRecommendations.firstOrNull { it.file.id == clickedFileId } ?: return null
        val log = recordFileOpen(
            fileId = clicked.file.id,
            openedAtMillis = openedAtMillis,
            previousFileId = previousFileId,
        )
        onlineLearner.learnFromClick(
            clickedFileId = clickedFileId,
            shownRecommendations = shownRecommendations,
        )
        resolvedEventDao.appendClickEvent(
            RecommendationClickLog(
                clickedFileId = clickedFileId,
                openedAtMillis = openedAtMillis,
                previousFileId = previousFileId,
                shownFileIds = shownRecommendations.map { it.file.id },
            ),
        )
        persist()
        return log
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

    private fun recencyScore(lastOpenedAtMillis: Long, nowMillis: Long): Double {
        if (lastOpenedAtMillis <= 0L) return 0.4
        val age = (nowMillis - lastOpenedAtMillis).coerceAtLeast(0L)
        return exp(-(age.toDouble() / RECENCY_HALFLIFE_MILLIS.toDouble())).coerceIn(0.0, 1.0)
    }

    private fun recommendationDedupKey(reference: FileReference): String {
        val sourceKey = reference.source.trim().lowercase()
        return if (sourceKey.isNotBlank()) {
            "source:$sourceKey"
        } else {
            "id:${reference.id.trim()}"
        }
    }

    private fun recommendationConfidence(
        openCount: Int,
        hasContext: Boolean,
    ): Double {
        val supportConfidence = when {
            openCount <= 1 -> 0.35
            openCount == 2 -> 0.55
            openCount == 3 -> 0.75
            openCount == 4 -> 0.9
            else -> 1.0
        }

        // 没有 previousFileId 时，后继关系无法提供有效信息，所以整体信心再保守一点。
        return if (hasContext) {
            supportConfidence
        } else {
            (supportConfidence * 0.8) + 0.2
        }
    }

    private fun selectWithDiversity(
        scoredCandidates: List<ScoredRecommendation>,
        limit: Int,
    ): List<ScoredRecommendation> {
        if (limit <= 0 || scoredCandidates.isEmpty()) return emptyList()

        val remaining = scoredCandidates.toMutableList()
        val selected = mutableListOf<ScoredRecommendation>()

        while (remaining.isNotEmpty() && selected.size < limit) {
            var bestIndex = 0
            var bestScore = diversityAdjustedScore(selected, remaining[0])

            for (index in 1 until remaining.size) {
                val candidate = remaining[index]
                val adjustedScore = diversityAdjustedScore(selected, candidate)
                if (adjustedScore > bestScore) {
                    bestScore = adjustedScore
                    bestIndex = index
                }
            }

            selected += remaining.removeAt(bestIndex)
        }

        return selected
    }

    private fun diversityAdjustedScore(
        selected: List<ScoredRecommendation>,
        candidate: ScoredRecommendation,
    ): Double {
        if (selected.isEmpty()) return candidate.finalScore

        val candidateCategory = FileTypeClassifier.classify(candidate.file)
        val sameCategoryCount = selected.count { FileTypeClassifier.classify(it.file) == candidateCategory }
        val sameSourceKindCount = selected.count { it.file.sourceKind == candidate.file.sourceKind }

        val penalty = (sameCategoryCount * 0.04) + (sameSourceKindCount * 0.01)
        return candidate.finalScore - penalty
    }

    private fun persist() {
        stateDao.saveState(exportSnapshot())
    }

    private companion object {
        const val RECENCY_HALFLIFE_MILLIS = 7L * 86_400_000L
    }
}

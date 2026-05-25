package com.example.cross_platformfilemanager

import kotlin.math.exp

/**
 * 本地推荐引擎，负责把打开事件、时间规律、后继关系和在线学习结果组织成完整推荐流程。
 *
 * 引擎只依赖仓储接口保存和恢复状态，不依赖具体存储实现，
 * 这样后续从内存实现切换到数据库实现时，主要改动可以收敛在仓储层。
 */
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

    /**
     * 生成查询词、标签和文件条目的搜索建议。
     *
     * 这个入口主要服务搜索联想，不直接参与基于打开事件的综合分计算，
     * 因此只补充方法职责和数据来源，不展开推荐算法细节。
     */
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

    /**
     * 导出当前推荐状态快照。
     *
     * 快照包含时间规律、后继关系、学习权重和最近一次打开文件，
     * 供上层整体保存，而不是直接暴露各个 Store 的内部结构。
     */
    fun exportSnapshot(): RecommendationEngineSnapshot = RecommendationEngineSnapshot(
        filePatterns = filePatternStore.snapshot(),
        transitionSnapshot = transitionStore.snapshot(),
        weightSnapshot = weightStore.snapshot(),
        lastOpenedFileId = lastOpenedFileId,
    )

    /**
     * 用已保存的推荐状态恢复引擎。
     *
     * 传入空值时按“重置推荐学习状态”处理；
     * 传入快照时分别恢复各个 Store，再统一保存一次当前状态。
     */
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

    /**
     * 清空推荐算法相关状态和事件记录。
     *
     * 这里清理的是推荐学习链路的数据，不负责改动文件条目本身的业务数据。
     */
    fun clear() {
        filePatternStore.clear()
        transitionStore.clear()
        weightStore.resetLearning()
        lastOpenedFileId = null
        stateDao.clearState()
        resolvedEventDao.clearEvents()
    }

    /**
     * 记录一次文件打开事件。
     *
     * 这一步会同时驱动三类状态更新：
     * - 更新时间规律样本
     * - 累计前一个文件到当前文件的后继关系
     * - 记录最近一次打开文件，用作下一次推荐上下文
     */
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

    /**
     * 计算候选文件的推荐结果，并按综合分返回前若干项。
     *
     * 综合分由三类信号组成：
     * - 时间规律分：当前时刻与文件典型打开周期的接近程度
     * - 后继关系分：当前上下文文件之后接着打开该文件的概率
     * - 最近打开分：样本不足时的稳定回退信号
     *
     * 排序前会先清理无效条目和重复候选，排序后再做一次轻量多样性选择。
     */
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
            // 候选文件先去重，避免同一文件条目被重复计入推荐结果。
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
                // 样本不足或缺少上下文时，对结构化信号做保守回退，避免偶然行为放大。
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
                    // 综合分接近时，优先选择历史样本更多的文件，降低低样本抖动。
                    .thenByDescending { filePatternStore.get(it.file.id)?.openCount ?: 0 }
                    .thenByDescending { it.file.lastOpenedAtMillis }
                    .thenBy { normalize(it.file.title) }
                    .thenBy { it.file.id },
            )
            .toList()

        return selectWithDiversity(scoredCandidates, limit)
    }

    /**
     * 记录一次推荐点击反馈，并把点击结果作为在线学习样本。
     *
     * 只有被点击文件确实出现在本次展示的推荐列表中，才会触发学习，
     * 这样可以避免把普通文件打开误判为推荐反馈。
     */
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

        // 没有前一个文件上下文时，后继关系无法发挥作用，因此整体置信度更保守。
        return if (hasContext) {
            supportConfidence
        } else {
            (supportConfidence * 0.8) + 0.2
        }
    }

    /**
     * 在综合分排序后做一次轻量多样性筛选。
     *
     * 目标不是推翻综合分，而是在分数接近时，略微压低同类文件连续出现的概率。
     */
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

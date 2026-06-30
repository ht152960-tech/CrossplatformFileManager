package com.example.cross_platformfilemanager.domain.recommendation

import com.example.cross_platformfilemanager.runtime.TaggoRuntimeFile
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class ExtractedAfterOpenFeatures(
    val vector: RecommendationFeatureVector,
    val matchedOrder: Int,
    val pathConfidence: Double,
)

class AfterOpenFeatureExtractor(
    private val fallbackExtractor: HomeRecommendationFeatureExtractor = HomeRecommendationFeatureExtractor(),
    private val pathModel: AfterOpenPathModel = AfterOpenPathModel(),
) {
    fun extractAll(
        files: List<TaggoRuntimeFile>,
        history: RecommendationHistory,
        request: RecommendationRequest,
        config: RecommendationLearningConfig,
    ): Map<String, ExtractedAfterOpenFeatures> {
        val fallback = fallbackExtractor.extractAll(
            files = files,
            history = history,
            nowMs = request.nowMs,
            config = config,
            feedbackMode = RecommendationMode.AFTER_OPEN,
        )
        val pathSignals = pathModel.predict(history.openEvents, request.recentOpenFileIds)
        val filesById = files.associateBy { it.id }
        val contextTags = request.recentOpenFileIds
            .mapNotNull(filesById::get)
            .flatMap { it.tags }
            .map(::normalize)
            .filter { it.isNotBlank() }
            .toSet()
        return files.associate { file ->
            val base = fallback.getValue(file.id).vector
            val path = pathSignals[file.id] ?: AfterOpenPathSignal()
            val candidateTags = file.tags.map(::normalize).filter { it.isNotBlank() }.toSet()
            val contextTagAffinity = if (candidateTags.isEmpty() || contextTags.isEmpty()) {
                0.0
            } else {
                candidateTags.intersect(contextTags).size.toDouble() / candidateTags.size.toDouble()
            }
            file.id to ExtractedAfterOpenFeatures(
                vector = base.copy(
                    periodic = 0.0,
                    sequencePath = path.sequencePath,
                    directSuccessor = path.directSuccessor,
                    tagAffinity = contextTagAffinity,
                ).normalized(),
                matchedOrder = path.matchedOrder,
                pathConfidence = path.confidence,
            )
        }
    }

    private fun normalize(value: String): String = value.trim().lowercase()
}

class AfterOpenRecommendationRanker(
    private val json: Json = Json { encodeDefaults = true },
) {
    fun rank(
        files: List<TaggoRuntimeFile>,
        extracted: Map<String, ExtractedAfterOpenFeatures>,
        policy: RecommendationPolicy,
        request: RecommendationRequest,
        homeFallback: List<RankedHomeRecommendation>,
    ): List<RankedHomeRecommendation> {
        val scored = files.map { file ->
            val features = extracted.getValue(file.id)
            val values = features.vector.values()
            val weights = policy.weights.values()
            val contributions = values.mapValues { (name, value) ->
                val weighted = value * weights.getValue(name)
                if (name == "feedbackPenalty") -weighted else weighted
            }.toMutableMap()
            if (files.size > 1 && file.id == request.triggerFileId) {
                contributions["selfRepeatPenalty"] = -SELF_REPEAT_PENALTY
            }
            val reasons = buildReasons(contributions, features.pathConfidence)
            val snapshot = RecommendationFeatureSnapshot(
                mode = RecommendationMode.AFTER_OPEN.name,
                features = values + ("selfRepeatPenalty" to if (file.id == request.triggerFileId) 1.0 else 0.0),
                weights = weights + ("selfRepeatPenalty" to SELF_REPEAT_PENALTY),
                contributions = contributions,
                confidence = mapOf(
                    "sequencePath" to features.pathConfidence,
                    "directSuccessor" to features.pathConfidence,
                ),
                pathContext = RecommendationPathContext(
                    triggerFileId = request.triggerFileId,
                    recentOpenFileIds = request.recentOpenFileIds,
                    matchedOrder = features.matchedOrder,
                ),
            )
            RankedHomeRecommendation(
                file = file,
                result = RecommendationResult(
                    fileId = file.id,
                    finalScore = contributions.values.sum(),
                    scoreParts = RecommendationScoreParts(
                        recencyScore = features.vector.recency,
                        frequencyScore = features.vector.frequency,
                        tagAffinityScore = features.vector.tagAffinity,
                        manualSearchAffinityScore = features.vector.manualSearchOpen,
                        feedbackScore = features.vector.feedbackPositive - features.vector.feedbackPenalty,
                        weakTypeContextScore = features.vector.weakTypeContext,
                        sequencePathScore = features.vector.sequencePath,
                        successorScore = features.vector.directSuccessor,
                    ),
                    reasons = reasons,
                    featuresJson = json.encodeToString(snapshot),
                    mode = RecommendationMode.AFTER_OPEN,
                ),
            )
        }
        fun hasPathSignal(candidate: RankedHomeRecommendation): Boolean {
            val vector = extracted.getValue(candidate.file.id).vector
            return vector.sequencePath > 0.0 || vector.directSuccessor > 0.0
        }
        val pathComparator = compareByDescending<RankedHomeRecommendation> { it.result.finalScore }
            .thenByDescending { it.file.lastContentOpenedAtMs ?: Long.MIN_VALUE }
            .thenByDescending { it.file.createdAtMs }
            .thenBy { it.file.displayName }
            .thenBy { it.file.id }
        val pathCandidates = scored.filter(::hasPathSignal).sortedWith(pathComparator)
        val scoredById = scored.associateBy { it.file.id }
        val fallbackCandidates = homeFallback
            .mapNotNull { scoredById[it.file.id] }
            .filterNot(::hasPathSignal)
        val ranked = (pathCandidates + fallbackCandidates).toMutableList()
        if (ranked.size > 1 && ranked.first().file.id == request.triggerFileId) {
            val firstOther = ranked.indexOfFirst { it.file.id != request.triggerFileId }
            if (firstOther > 0) {
                val trigger = ranked.removeAt(0)
                ranked.add(firstOther, trigger)
            }
        }
        return ranked.take(request.limit.coerceAtLeast(0).coerceAtMost(files.size))
    }

    private fun buildReasons(
        contributions: Map<String, Double>,
        pathConfidence: Double,
    ): List<RecommendationReason> = buildList {
        addReason(
            name = "sequencePath",
            code = "sequence_path_match",
            message = "符合你最近连续打开后的路径",
            confidence = pathConfidence,
            contributions = contributions,
        )
        addReason(
            name = "directSuccessor",
            code = "direct_successor",
            message = "历史上常在当前文件之后打开",
            confidence = pathConfidence,
            contributions = contributions,
        )
        listOf(
            "recency" to "最近真实打开过",
            "frequency" to "近期真实打开较频繁",
            "feedbackPositive" to "过去曾从推荐中打开",
            "tagAffinity" to "与最近连续打开文件标签相关",
            "manualSearchOpen" to "曾通过精准搜索打开",
            "detailInterest" to "近期查看过文件详情",
            "failedOpenIntent" to "近期尝试打开过",
            "coldStart" to "最近添加的文件",
            "feedbackPenalty" to "此前曾略过",
            "weakTypeContext" to "与近期文件类型相关",
            "selfRepeatPenalty" to "刚刚已打开此文件",
        ).forEach { (name, message) ->
            addReason(name, name, message, null, contributions)
        }
    }

    private fun MutableList<RecommendationReason>.addReason(
        name: String,
        code: String,
        message: String,
        confidence: Double?,
        contributions: Map<String, Double>,
    ) {
        val contribution = contributions[name] ?: return
        if (contribution == 0.0) return
        add(RecommendationReason(code, message, contribution, confidence))
    }

    private companion object {
        const val SELF_REPEAT_PENALTY = 0.05
    }
}

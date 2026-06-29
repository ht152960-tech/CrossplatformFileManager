package com.example.cross_platformfilemanager.domain.recommendation

import com.example.cross_platformfilemanager.runtime.TaggoRuntimeFile
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class RankedHomeRecommendation(
    val file: TaggoRuntimeFile,
    val result: RecommendationResult,
)

class HomeRecommendationRanker(
    private val json: Json = Json { encodeDefaults = true },
) {
    fun rank(
        files: List<TaggoRuntimeFile>,
        extracted: Map<String, ExtractedHomeFeatures>,
        policy: RecommendationPolicy,
        limit: Int,
    ): List<RankedHomeRecommendation> {
        val ranked = files.map { file ->
            val features = extracted.getValue(file.id)
            val values = features.vector.values()
            val weights = policy.weights.values()
            val contributions = values.mapValues { (name, value) ->
                val contribution = value * weights.getValue(name)
                if (name == "feedbackPenalty") -contribution else contribution
            }
            val finalScore = contributions.values.sum()
            val reasons = buildReasons(features, contributions)
            val featureSnapshot = RecommendationFeatureSnapshot(
                mode = RecommendationMode.HOME_INITIAL.name,
                features = values,
                weights = weights,
                contributions = contributions,
                confidence = mapOf("periodic" to features.periodicConfidence),
            )
            RankedHomeRecommendation(
                file = file,
                result = RecommendationResult(
                    fileId = file.id,
                    finalScore = finalScore,
                    scoreParts = features.vector.toScoreParts(),
                    reasons = reasons,
                    featuresJson = json.encodeToString(featureSnapshot),
                    mode = RecommendationMode.HOME_INITIAL,
                ),
            )
        }.sortedWith(
            compareByDescending<RankedHomeRecommendation> { it.result.finalScore }
                .thenByDescending { it.file.lastContentOpenedAtMs ?: Long.MIN_VALUE }
                .thenByDescending { it.file.createdAtMs }
                .thenBy { it.file.displayName }
                .thenBy { it.file.id },
        )
        return ranked.take(limit.coerceAtLeast(0).coerceAtMost(files.size))
    }

    private fun buildReasons(
        features: ExtractedHomeFeatures,
        contributions: Map<String, Double>,
    ): List<RecommendationReason> = buildList {
        addReason("periodic", "接近你的稳定打开周期", features.periodicConfidence, contributions)
        addReason("manualSearchOpen", "曾通过精准搜索打开", null, contributions)
        addReason("recency", "最近真实打开过", null, contributions)
        addReason("frequency", "近期真实打开较频繁", null, contributions)
        addReason("feedbackPositive", "过去曾从推荐中打开", null, contributions)
        addReason("tagAffinity", "与近期打开文件标签相关", null, contributions)
        addReason("detailInterest", "近期查看过文件详情", null, contributions)
        addReason("failedOpenIntent", "近期尝试打开过", null, contributions)
        addReason("coldStart", "最近添加的文件", null, contributions)
        addReason("feedbackPenalty", "此前曾略过", null, contributions)
        addReason("weakTypeContext", "与近期文件类型相关", null, contributions)
    }

    private fun MutableList<RecommendationReason>.addReason(
        code: String,
        message: String,
        confidence: Double?,
        contributions: Map<String, Double>,
    ) {
        val contribution = contributions.getValue(code)
        if (contribution == 0.0) return
        add(RecommendationReason(code, message, contribution, confidence))
    }

    private fun RecommendationFeatureVector.toScoreParts() = RecommendationScoreParts(
        recencyScore = recency,
        frequencyScore = frequency,
        periodicScore = periodic,
        tagAffinityScore = tagAffinity,
        manualSearchAffinityScore = manualSearchOpen,
        feedbackScore = feedbackPositive - feedbackPenalty,
        weakTypeContextScore = weakTypeContext,
    )
}

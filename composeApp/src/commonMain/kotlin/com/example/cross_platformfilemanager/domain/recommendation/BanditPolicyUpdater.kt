package com.example.cross_platformfilemanager.domain.recommendation

import kotlinx.serialization.json.Json

class BanditPolicyUpdater(
    private val policyStore: RecommendationPolicyStore,
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    suspend fun updateFromRecommendationFeedback(
        policy: RecommendationPolicy,
        selectedFeaturesJson: String?,
        skippedBeforeFeaturesJson: List<String>,
    ): RecommendationPolicy? {
        val selected = decode(selectedFeaturesJson) ?: return null
        val skipped = skippedBeforeFeaturesJson.mapNotNull(::decode)
        val values = policy.weights.values().toMutableMap()
        selected.features.forEach { (name, feature) ->
            val current = values[name] ?: return@forEach
            val rate = if (name == "manualSearchOpen" && feature > 0.0) {
                policy.learningConfig.manualSearchLearningRate
            } else {
                policy.learningConfig.positiveLearningRate
            }
            values[name] = current + rate * feature
        }
        if (skipped.isNotEmpty()) {
            values.keys.toList().forEach { name ->
                val mean = skipped.map { it.features[name] ?: 0.0 }.average()
                values[name] = values.getValue(name) - policy.learningConfig.negativeLearningRate * mean
            }
        }
        return policyStore.updateHomePolicy(policy, RecommendationWeights.fromMap(values))
    }

    private fun decode(value: String?): RecommendationFeatureSnapshot? =
        if (value.isNullOrBlank()) {
            null
        } else {
            runCatching { json.decodeFromString<RecommendationFeatureSnapshot>(value) }.getOrNull()
        }
}

package com.example.cross_platformfilemanager.domain.recommendation

import com.example.cross_platformfilemanager.runtime.TaggoRuntimeFile
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class HomeRecommendationComputation(
    val recommendations: List<RankedHomeRecommendation>,
    val policy: RecommendationPolicy,
)

class TaggoRecommendationService(
    private val historyReader: RecommendationHistoryReader,
    private val policyStore: RecommendationPolicyStore,
    private val featureExtractor: HomeRecommendationFeatureExtractor = HomeRecommendationFeatureExtractor(),
    private val ranker: HomeRecommendationRanker = HomeRecommendationRanker(),
    private val policyUpdater: BanditPolicyUpdater = BanditPolicyUpdater(policyStore),
) {
    private val policyUpdateMutex = Mutex()
    suspend fun recommendHome(
        files: List<TaggoRuntimeFile>,
        nowMs: Long,
        limit: Int,
    ): HomeRecommendationComputation {
        val history = historyReader.loadHistory(nowMs)
        val policy = policyStore.loadOrCreateHomePolicy()
        val features = featureExtractor.extractAll(files, history, nowMs, policy.learningConfig)
        return HomeRecommendationComputation(
            recommendations = ranker.rank(files, features, policy, limit),
            policy = policy,
        )
    }

    suspend fun updatePolicyFromFeedback(
        selectedFeaturesJson: String?,
        skippedBeforeFeaturesJson: List<String>,
    ): RecommendationPolicy? = policyUpdateMutex.withLock {
        val current = policyStore.loadOrCreateHomePolicy()
        policyUpdater.updateFromRecommendationFeedback(
            current,
            selectedFeaturesJson,
            skippedBeforeFeaturesJson,
        )
    }

    suspend fun updatePolicyFromManualSearchOpen(
        fileId: String,
        files: List<TaggoRuntimeFile>,
        nowMs: Long,
    ): RecommendationPolicy? = policyUpdateMutex.withLock {
        val history = historyReader.loadHistory(nowMs)
        val policy = policyStore.loadOrCreateHomePolicy()
        val selected = featureExtractor.extractAll(files, history, nowMs, policy.learningConfig)[fileId]
            ?: return null
        val strength = selected.vector.manualSearchOpen
        if (strength <= 0.0) return null
        val values = policy.weights.values().toMutableMap()
        values["manualSearchOpen"] = values.getValue("manualSearchOpen") +
            policy.learningConfig.manualSearchLearningRate * strength
        return policyStore.updateHomePolicy(policy, RecommendationWeights.fromMap(values))
    }
}

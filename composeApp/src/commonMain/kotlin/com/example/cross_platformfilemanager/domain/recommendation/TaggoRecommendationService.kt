package com.example.cross_platformfilemanager.domain.recommendation

import com.example.cross_platformfilemanager.runtime.TaggoRuntimeFile
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class HomeRecommendationComputation(
    val recommendations: List<RankedHomeRecommendation>,
    val policy: RecommendationPolicy,
    val request: RecommendationRequest,
)

class TaggoRecommendationService(
    private val historyReader: RecommendationHistoryReader,
    private val policyStore: RecommendationPolicyStore,
    private val featureExtractor: HomeRecommendationFeatureExtractor = HomeRecommendationFeatureExtractor(),
    private val ranker: HomeRecommendationRanker = HomeRecommendationRanker(),
    private val afterOpenFeatureExtractor: AfterOpenFeatureExtractor =
        AfterOpenFeatureExtractor(featureExtractor),
    private val afterOpenRanker: AfterOpenRecommendationRanker = AfterOpenRecommendationRanker(),
    private val policyUpdater: BanditPolicyUpdater = BanditPolicyUpdater(policyStore),
) {
    private val policyUpdateMutex = Mutex()

    suspend fun recommendHome(
        files: List<TaggoRuntimeFile>,
        nowMs: Long,
        limit: Int,
    ): HomeRecommendationComputation = recommend(
        files = files,
        request = RecommendationRequest(
            mode = RecommendationMode.HOME_INITIAL,
            nowMs = nowMs,
            limit = limit,
        ),
    )

    suspend fun recommend(
        files: List<TaggoRuntimeFile>,
        request: RecommendationRequest,
    ): HomeRecommendationComputation {
        val history = historyReader.loadHistory(request.nowMs)
        return if (request.mode == RecommendationMode.AFTER_OPEN) {
            recommendAfterOpen(files, history, request)
        } else {
            recommendHomeInitial(files, history, request)
        }
    }

    private suspend fun recommendHomeInitial(
        files: List<TaggoRuntimeFile>,
        history: RecommendationHistory,
        request: RecommendationRequest,
    ): HomeRecommendationComputation {
        val policy = policyStore.loadOrCreateHomePolicy()
        val features = featureExtractor.extractAll(
            files,
            history,
            request.nowMs,
            policy.learningConfig,
            RecommendationMode.HOME_INITIAL,
        )
        return HomeRecommendationComputation(
            recommendations = ranker.rank(files, features, policy, request.limit),
            policy = policy,
            request = request,
        )
    }

    private suspend fun recommendAfterOpen(
        files: List<TaggoRuntimeFile>,
        history: RecommendationHistory,
        request: RecommendationRequest,
    ): HomeRecommendationComputation {
        val homePolicy = policyStore.loadOrCreateHomePolicy()
        val afterOpenPolicy = policyStore.loadOrCreateAfterOpenPolicy()
        val homeFeatures = featureExtractor.extractAll(
            files,
            history,
            request.nowMs,
            homePolicy.learningConfig,
            RecommendationMode.HOME_INITIAL,
        )
        val homeFallback = ranker.rank(files, homeFeatures, homePolicy, files.size)
        val afterOpenFeatures = afterOpenFeatureExtractor.extractAll(
            files,
            history,
            request,
            afterOpenPolicy.learningConfig,
        )
        return HomeRecommendationComputation(
            recommendations = afterOpenRanker.rank(
                files = files,
                extracted = afterOpenFeatures,
                policy = afterOpenPolicy,
                request = request,
                homeFallback = homeFallback,
            ),
            policy = afterOpenPolicy,
            request = request,
        )
    }

    suspend fun updatePolicyFromFeedback(
        mode: RecommendationMode,
        selectedFeaturesJson: String?,
        skippedBeforeFeaturesJson: List<String>,
    ): RecommendationPolicy? = policyUpdateMutex.withLock {
        val current = policyStore.loadOrCreatePolicy(mode)
        policyUpdater.updateFromRecommendationFeedback(
            current,
            selectedFeaturesJson,
            skippedBeforeFeaturesJson,
        )
    }

    suspend fun updatePolicyFromFeedback(
        selectedFeaturesJson: String?,
        skippedBeforeFeaturesJson: List<String>,
    ): RecommendationPolicy? = updatePolicyFromFeedback(
        mode = RecommendationMode.HOME_INITIAL,
        selectedFeaturesJson = selectedFeaturesJson,
        skippedBeforeFeaturesJson = skippedBeforeFeaturesJson,
    )

    suspend fun updatePolicyFromManualSearchOpen(
        fileId: String,
        files: List<TaggoRuntimeFile>,
        nowMs: Long,
    ): RecommendationPolicy? = policyUpdateMutex.withLock {
        val history = historyReader.loadHistory(nowMs)
        val policy = policyStore.loadOrCreateHomePolicy()
        val selected = featureExtractor.extractAll(
            files,
            history,
            nowMs,
            policy.learningConfig,
            RecommendationMode.HOME_INITIAL,
        )[fileId] ?: return null
        val strength = selected.vector.manualSearchOpen
        if (strength <= 0.0) return null
        val values = policy.weights.values().toMutableMap()
        values["manualSearchOpen"] = values.getValue("manualSearchOpen") +
            policy.learningConfig.manualSearchLearningRate * strength
        return policyStore.updatePolicy(policy, RecommendationWeights.fromMap(values))
    }
}

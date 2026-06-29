package com.example.cross_platformfilemanager.domain.recommendation

import com.example.cross_platformfilemanager.data.adapter.TaggoClock
import com.example.cross_platformfilemanager.data.model.TaggoRecommendationPolicyState
import com.example.cross_platformfilemanager.data.repository.RecommendationPolicyRepository
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class RecommendationWeights(
    val periodic: Double = 0.20,
    val manualSearchOpen: Double = 0.17,
    val recency: Double = 0.15,
    val frequency: Double = 0.12,
    val feedbackPositive: Double = 0.11,
    val tagAffinity: Double = 0.09,
    val detailInterest: Double = 0.06,
    val failedOpenIntent: Double = 0.04,
    val coldStart: Double = 0.04,
    val feedbackPenalty: Double = 0.015,
    val weakTypeContext: Double = 0.005,
) {
    fun sum(): Double = values().values.sum()

    fun normalized(config: RecommendationLearningConfig): RecommendationWeights {
        val bounded = values().mapValues { (_, value) ->
            value.coerceIn(config.minWeight, config.maxWeight)
        }.toMutableMap()
        repeat(bounded.size * 2) {
            val difference = 1.0 - bounded.values.sum()
            if (kotlin.math.abs(difference) < 1e-9) return@repeat
            val adjustable = bounded.filterValues {
                if (difference > 0.0) it < config.maxWeight else it > config.minWeight
            }.keys
            if (adjustable.isEmpty()) return@repeat
            val share = difference / adjustable.size.toDouble()
            adjustable.forEach { name ->
                bounded[name] = (bounded.getValue(name) + share)
                    .coerceIn(config.minWeight, config.maxWeight)
            }
        }
        return fromMap(bounded)
    }

    fun values(): Map<String, Double> = linkedMapOf(
        "periodic" to periodic,
        "manualSearchOpen" to manualSearchOpen,
        "recency" to recency,
        "frequency" to frequency,
        "feedbackPositive" to feedbackPositive,
        "tagAffinity" to tagAffinity,
        "detailInterest" to detailInterest,
        "failedOpenIntent" to failedOpenIntent,
        "coldStart" to coldStart,
        "feedbackPenalty" to feedbackPenalty,
        "weakTypeContext" to weakTypeContext,
    )

    companion object {
        fun fromMap(values: Map<String, Double>): RecommendationWeights = RecommendationWeights(
            periodic = values.getValue("periodic"),
            manualSearchOpen = values.getValue("manualSearchOpen"),
            recency = values.getValue("recency"),
            frequency = values.getValue("frequency"),
            feedbackPositive = values.getValue("feedbackPositive"),
            tagAffinity = values.getValue("tagAffinity"),
            detailInterest = values.getValue("detailInterest"),
            failedOpenIntent = values.getValue("failedOpenIntent"),
            coldStart = values.getValue("coldStart"),
            feedbackPenalty = values.getValue("feedbackPenalty"),
            weakTypeContext = values.getValue("weakTypeContext"),
        )
    }
}

@Serializable
data class RecommendationLearningConfig(
    val positiveLearningRate: Double = 0.035,
    val negativeLearningRate: Double = 0.012,
    val manualSearchLearningRate: Double = 0.045,
    val minWeight: Double = 0.005,
    val maxWeight: Double = 0.35,
    val normalization: Boolean = true,
    val manualSearchWindowMs: Long = 10L * 60L * 1000L,
)

data class RecommendationPolicy(
    val id: String,
    val policyName: String,
    val mode: RecommendationMode,
    val modelVersion: Long,
    val weights: RecommendationWeights,
    val learningConfig: RecommendationLearningConfig,
    val updateCount: Long,
    val createdAtMs: Long,
    val lastUpdatedAtMs: Long,
)

class RecommendationPolicyStore(
    private val repository: RecommendationPolicyRepository,
    private val clock: TaggoClock,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    },
) {
    suspend fun loadOrCreateHomePolicy(): RecommendationPolicy {
        val stored = repository.loadPolicy(POLICY_NAME, RecommendationMode.HOME_INITIAL.name, MODEL_VERSION)
        if (stored == null) {
            val policy = defaultPolicy(clock.nowMs())
            repository.insertPolicy(policy.toState())
            return policy
        }
        return stored.toPolicyOrDefault()
    }

    suspend fun updateHomePolicy(
        current: RecommendationPolicy,
        weights: RecommendationWeights,
    ): RecommendationPolicy {
        val normalized = if (current.learningConfig.normalization) {
            weights.normalized(current.learningConfig)
        } else {
            weights
        }
        val updated = current.copy(
            weights = normalized,
            updateCount = current.updateCount + 1,
            lastUpdatedAtMs = clock.nowMs(),
        )
        repository.updatePolicy(updated.toState())
        return updated
    }

    fun defaultPolicy(nowMs: Long): RecommendationPolicy = RecommendationPolicy(
        id = "policy_${POLICY_NAME}_${RecommendationMode.HOME_INITIAL.name}_$MODEL_VERSION",
        policyName = POLICY_NAME,
        mode = RecommendationMode.HOME_INITIAL,
        modelVersion = MODEL_VERSION,
        weights = RecommendationWeights(),
        learningConfig = RecommendationLearningConfig(),
        updateCount = 0L,
        createdAtMs = nowMs,
        lastUpdatedAtMs = nowMs,
    )

    private fun TaggoRecommendationPolicyState.toPolicyOrDefault(): RecommendationPolicy =
        try {
            RecommendationPolicy(
                id = id,
                policyName = policyName,
                mode = RecommendationMode.valueOf(recommendationMode),
                modelVersion = modelVersion,
                weights = json.decodeFromString<RecommendationWeights>(weightsJson),
                learningConfig = json.decodeFromString<RecommendationLearningConfig>(learningConfigJson),
                updateCount = updateCount,
                createdAtMs = createdAtMs,
                lastUpdatedAtMs = lastUpdatedAtMs,
            )
        } catch (_: Throwable) {
            defaultPolicy(lastUpdatedAtMs).copy(
                id = id,
                updateCount = updateCount,
                createdAtMs = createdAtMs,
            )
        }

    private fun RecommendationPolicy.toState() = TaggoRecommendationPolicyState(
        id = id,
        policyName = policyName,
        recommendationMode = mode.name,
        modelVersion = modelVersion,
        weightsJson = json.encodeToString(RecommendationWeights.serializer(), weights),
        learningConfigJson = json.encodeToString(RecommendationLearningConfig.serializer(), learningConfig),
        updateCount = updateCount,
        createdAtMs = createdAtMs,
        lastUpdatedAtMs = lastUpdatedAtMs,
    )

    companion object {
        const val POLICY_NAME = "home_dynamic_policy"
        const val MODEL_VERSION = 1L
    }
}

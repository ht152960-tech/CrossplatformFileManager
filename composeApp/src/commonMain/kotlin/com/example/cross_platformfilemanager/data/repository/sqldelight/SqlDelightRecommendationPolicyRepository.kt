package com.example.cross_platformfilemanager.data.repository.sqldelight

import com.example.cross_platformfilemanager.data.db.TaggoDatabase
import com.example.cross_platformfilemanager.data.mapper.toModel
import com.example.cross_platformfilemanager.data.model.TaggoRecommendationPolicyState
import com.example.cross_platformfilemanager.data.repository.RecommendationPolicyRepository

class SqlDelightRecommendationPolicyRepository(
    database: TaggoDatabase,
) : RecommendationPolicyRepository {
    private val queries = database.taggoDatabaseQueries

    override suspend fun loadPolicy(
        policyName: String,
        recommendationMode: String,
        modelVersion: Long,
    ): TaggoRecommendationPolicyState? =
        queries.selectRecommendationPolicy(policyName, recommendationMode, modelVersion)
            .executeAsOneOrNull()
            ?.toModel()

    override suspend fun insertPolicy(state: TaggoRecommendationPolicyState) {
        queries.insertRecommendationPolicy(
            state.id,
            state.policyName,
            state.recommendationMode,
            state.modelVersion,
            state.weightsJson,
            state.learningConfigJson,
            state.updateCount,
            state.createdAtMs,
            state.lastUpdatedAtMs,
        )
    }

    override suspend fun updatePolicy(state: TaggoRecommendationPolicyState) {
        queries.updateRecommendationPolicy(
            state.weightsJson,
            state.learningConfigJson,
            state.updateCount,
            state.lastUpdatedAtMs,
            state.policyName,
            state.recommendationMode,
            state.modelVersion,
        )
    }
}

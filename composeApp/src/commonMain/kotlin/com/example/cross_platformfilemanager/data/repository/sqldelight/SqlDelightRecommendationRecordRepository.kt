package com.example.cross_platformfilemanager.data.repository.sqldelight

import com.example.cross_platformfilemanager.data.db.TaggoDatabase
import com.example.cross_platformfilemanager.data.mapper.toModel
import com.example.cross_platformfilemanager.data.model.TaggoRecommendationCandidateSnapshot
import com.example.cross_platformfilemanager.data.model.TaggoRecommendationContext
import com.example.cross_platformfilemanager.data.model.TaggoRecommendationFeedback
import com.example.cross_platformfilemanager.data.model.TaggoRecommendationSet
import com.example.cross_platformfilemanager.data.repository.RecommendationRecordRepository

class SqlDelightRecommendationRecordRepository(
    private val database: TaggoDatabase,
) : RecommendationRecordRepository {
    private val queries = database.taggoDatabaseQueries
    private val historyQueries = database.recommendationHistoryQueriesQueries

    override suspend fun addRecommendationContext(context: TaggoRecommendationContext) {
        queries.insertRecommendationContext(
            context.id,
            context.createdAtMs,
            context.contextType,
            context.sessionId,
            context.triggerFileId,
            context.searchQuery,
            context.localHour,
            context.dayOfWeek,
            context.latitude,
            context.longitude,
            context.locationPrecision,
        )
    }

    override suspend fun getRecommendationContextById(id: String): TaggoRecommendationContext? =
        queries.selectRecommendationContextById(id).executeAsOneOrNull()?.toModel()

    override suspend fun addRecommendationSet(set: TaggoRecommendationSet) {
        queries.insertRecommendationSet(
            set.id,
            set.contextId,
            set.generatedAtMs,
            set.setType,
            set.modelVersion,
            set.policyName,
            set.policyVersion,
        )
    }

    override suspend fun getRecommendationSetById(id: String): TaggoRecommendationSet? =
        queries.selectRecommendationSetById(id).executeAsOneOrNull()?.toModel()

    override suspend fun addCandidateSnapshot(snapshot: TaggoRecommendationCandidateSnapshot) {
        queries.insertRecommendationCandidateSnapshot(
            snapshot.recommendationSetId,
            snapshot.fileId,
            snapshot.rank,
            snapshot.score,
            snapshot.reasonsJson,
            snapshot.featuresJson,
            snapshot.selectionType,
            snapshot.propensity,
            snapshot.selectedAtMs,
        )
    }

    override suspend fun getCandidateSnapshotsForSet(
        recommendationSetId: String,
    ): List<TaggoRecommendationCandidateSnapshot> =
        queries.selectCandidatesForRecommendationSet(recommendationSetId)
            .executeAsList()
            .map { it.toModel() }

    override suspend fun markCandidateSelected(
        recommendationSetId: String,
        fileId: String,
        selectedAtMs: Long,
    ) {
        queries.markRecommendationCandidateSelected(selectedAtMs, recommendationSetId, fileId)
    }

    override suspend fun addRecommendationFeedback(feedback: TaggoRecommendationFeedback) {
        queries.insertRecommendationFeedback(
            feedback.id,
            feedback.recommendationSetId,
            feedback.fileId,
            feedback.feedbackType,
            feedback.rewardValue,
            feedback.rankAtFeedback,
            feedback.createdAtMs,
            feedback.behaviorEventId,
        )
    }

    override suspend fun getFeedbackForSet(
        recommendationSetId: String,
    ): List<TaggoRecommendationFeedback> =
        queries.selectFeedbackForRecommendationSet(recommendationSetId)
            .executeAsList()
            .map { it.toModel() }

    override suspend fun getFeedbackInRange(
        fromMs: Long,
        toMs: Long,
    ): List<TaggoRecommendationFeedback> =
        historyQueries.selectRecommendationFeedbackInRange(fromMs, toMs)
            .executeAsList()
            .map { it.toModel() }
}

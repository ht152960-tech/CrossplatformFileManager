package com.example.cross_platformfilemanager.runtime

import com.example.cross_platformfilemanager.data.adapter.TaggoClock
import com.example.cross_platformfilemanager.data.adapter.TaggoIdGenerator
import com.example.cross_platformfilemanager.data.model.TaggoRecommendationCandidateSnapshot
import com.example.cross_platformfilemanager.data.model.TaggoRecommendationContext
import com.example.cross_platformfilemanager.data.model.TaggoRecommendationFeedback
import com.example.cross_platformfilemanager.data.model.TaggoRecommendationSet
import com.example.cross_platformfilemanager.data.repository.RecommendationRecordRepository
import com.example.cross_platformfilemanager.domain.recommendation.RecommendationMode
import kotlin.coroutines.cancellation.CancellationException

data class RecommendationSnapshotInput(
    val fileId: String,
    val rank: Int,
    val score: Double?,
    val reasonsJson: String?,
    val featuresJson: String? = null,
)

data class RecordedRecommendationSet(
    val contextId: String,
    val setId: String,
    val generatedAtMs: Long,
)

class TaggoRecommendationRuntime(
    private val recommendationRecordRepository: RecommendationRecordRepository,
    private val idGenerator: TaggoIdGenerator,
    private val clock: TaggoClock,
    private val recommendationModelVersion: Long,
) {
    suspend fun recordRecommendationSet(
        surface: String,
        trigger: String,
        candidates: List<RecommendationSnapshotInput>,
        policyName: String = trigger,
        policyVersion: String? = null,
        mode: RecommendationMode? = null,
        sessionId: String? = null,
        triggerFileId: String? = null,
    ): RecordedRecommendationSet? = safeCall {
        val generatedAtMs = clock.nowMs()
        val contextId = idGenerator.nextRecommendationContextId()
        val setId = idGenerator.nextRecommendationSetId()
        recommendationRecordRepository.addRecommendationContext(
            TaggoRecommendationContext(
                id = contextId,
                createdAtMs = generatedAtMs,
                contextType = mode?.let { "$surface:${it.name}" } ?: surface,
                sessionId = sessionId,
                triggerFileId = triggerFileId,
                searchQuery = null,
                localHour = null,
                dayOfWeek = null,
                latitude = null,
                longitude = null,
                locationPrecision = null,
            ),
        )
        recommendationRecordRepository.addRecommendationSet(
            TaggoRecommendationSet(
                id = setId,
                contextId = contextId,
                generatedAtMs = generatedAtMs,
                setType = mode?.name ?: surface,
                modelVersion = recommendationModelVersion,
                policyName = policyName,
                policyVersion = policyVersion,
            ),
        )
        candidates.forEachIndexed { index, candidate ->
            recommendationRecordRepository.addCandidateSnapshot(
                TaggoRecommendationCandidateSnapshot(
                    recommendationSetId = setId,
                    fileId = candidate.fileId,
                    rank = (index + 1).toLong(),
                    score = candidate.score ?: 0.0,
                    reasonsJson = candidate.reasonsJson,
                    featuresJson = candidate.featuresJson,
                    selectionType = "rule_based",
                    propensity = null,
                    selectedAtMs = null,
                ),
            )
        }
        RecordedRecommendationSet(contextId, setId, generatedAtMs)
    }

    suspend fun recordSelectedFromRecommendation(
        recommendationSetId: String,
        selectedFileId: String,
        selectedRank: Int,
        openedSuccessfully: Boolean,
        candidates: List<RecommendationSnapshotInput>,
    ) {
        safeCall {
            val recordedAtMs = clock.nowMs()
            if (!openedSuccessfully) {
                addFeedback(
                    recommendationSetId = recommendationSetId,
                    fileId = selectedFileId,
                    feedbackType = "open_failed",
                    rewardValue = 0.0,
                    rank = selectedRank,
                    createdAtMs = recordedAtMs,
                )
                return@safeCall
            }

            recommendationRecordRepository.markCandidateSelected(
                recommendationSetId,
                selectedFileId,
                recordedAtMs,
            )
            addFeedback(
                recommendationSetId,
                selectedFileId,
                "selected_file",
                1.0,
                selectedRank,
                recordedAtMs,
            )
            addFeedback(
                recommendationSetId,
                selectedFileId,
                "positive_open",
                1.0,
                selectedRank,
                recordedAtMs,
            )
            recordSkippedBeforeSelected(
                recommendationSetId = recommendationSetId,
                candidates = candidates,
                selectedRank = selectedRank,
                createdAtMs = recordedAtMs,
            )
        }
    }

    suspend fun recordSkippedBeforeSelected(
        recommendationSetId: String,
        candidates: List<RecommendationSnapshotInput>,
        selectedRank: Int,
    ) {
        safeCall {
            recordSkippedBeforeSelected(
                recommendationSetId,
                candidates,
                selectedRank,
                clock.nowMs(),
            )
        }
    }

    private suspend fun recordSkippedBeforeSelected(
        recommendationSetId: String,
        candidates: List<RecommendationSnapshotInput>,
        selectedRank: Int,
        createdAtMs: Long,
    ) {
        candidates
            .filter { it.rank in 1 until selectedRank }
            .forEach { candidate ->
                addFeedback(
                    recommendationSetId,
                    candidate.fileId,
                    "skipped_before_selected_file",
                    null,
                    candidate.rank,
                    createdAtMs,
                )
            }
    }

    private suspend fun addFeedback(
        recommendationSetId: String,
        fileId: String,
        feedbackType: String,
        rewardValue: Double?,
        rank: Int,
        createdAtMs: Long,
    ) {
        recommendationRecordRepository.addRecommendationFeedback(
            TaggoRecommendationFeedback(
                id = idGenerator.nextRecommendationFeedbackId(),
                recommendationSetId = recommendationSetId,
                fileId = fileId,
                feedbackType = feedbackType,
                rewardValue = rewardValue,
                rankAtFeedback = rank.toLong(),
                createdAtMs = createdAtMs,
                behaviorEventId = null,
            ),
        )
    }

    private suspend fun <T> safeCall(block: suspend () -> T): T? =
        try {
            block()
        } catch (error: CancellationException) {
            throw error
        } catch (_: Throwable) {
            null
        }
}

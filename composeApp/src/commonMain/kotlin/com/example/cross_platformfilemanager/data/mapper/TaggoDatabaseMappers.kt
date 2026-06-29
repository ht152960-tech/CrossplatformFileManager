package com.example.cross_platformfilemanager.data.mapper

import com.example.cross_platformfilemanager.data.db.Behavior_event
import com.example.cross_platformfilemanager.data.db.Behavior_session
import com.example.cross_platformfilemanager.data.db.Explicit_need_signal
import com.example.cross_platformfilemanager.data.db.File_entry
import com.example.cross_platformfilemanager.data.db.File_reference
import com.example.cross_platformfilemanager.data.db.Recent_search
import com.example.cross_platformfilemanager.data.db.Recommendation_candidate_snapshot
import com.example.cross_platformfilemanager.data.db.Recommendation_context
import com.example.cross_platformfilemanager.data.db.Recommendation_feedback
import com.example.cross_platformfilemanager.data.db.Recommendation_policy_state
import com.example.cross_platformfilemanager.data.db.Recommendation_set
import com.example.cross_platformfilemanager.data.db.Tag
import com.example.cross_platformfilemanager.data.model.TaggoBehaviorEvent
import com.example.cross_platformfilemanager.data.model.TaggoBehaviorSession
import com.example.cross_platformfilemanager.data.model.TaggoExplicitNeedSignal
import com.example.cross_platformfilemanager.data.model.TaggoFileEntry
import com.example.cross_platformfilemanager.data.model.TaggoFileReference
import com.example.cross_platformfilemanager.data.model.TaggoRecentSearch
import com.example.cross_platformfilemanager.data.model.TaggoRecommendationCandidateSnapshot
import com.example.cross_platformfilemanager.data.model.TaggoRecommendationContext
import com.example.cross_platformfilemanager.data.model.TaggoRecommendationFeedback
import com.example.cross_platformfilemanager.data.model.TaggoRecommendationPolicyState
import com.example.cross_platformfilemanager.data.model.TaggoRecommendationSet
import com.example.cross_platformfilemanager.data.model.TaggoTag

internal fun File_entry.toModel() = TaggoFileEntry(
    id = id,
    displayName = display_name,
    extension = extension,
    mimeType = mime_type,
    taggoFileCategory = taggo_file_category,
    sizeBytes = size_bytes,
    createdAtMs = created_at_ms,
    updatedAtMs = updated_at_ms,
    lastContentOpenedAtMs = last_content_opened_at_ms,
    contentOpenCount = content_open_count,
    thumbnailState = thumbnail_state,
    thumbnailReferenceValue = thumbnail_reference_value,
    deletedAtMs = deleted_at_ms,
)

internal fun File_reference.toModel() = TaggoFileReference(
    id = id,
    fileId = file_id,
    referenceType = reference_type,
    referenceValue = reference_value,
    referenceAvailable = reference_available == 1L,
    platform = platform,
    createdAtMs = created_at_ms,
    updatedAtMs = updated_at_ms,
    lastVerifiedAtMs = last_verified_at_ms,
    isPrimary = is_primary == 1L,
)

internal fun Tag.toModel() = TaggoTag(
    id = id,
    name = name,
    normalizedName = normalized_name,
    createdAtMs = created_at_ms,
    updatedAtMs = updated_at_ms,
    deletedAtMs = deleted_at_ms,
)

internal fun Recent_search.toModel() = TaggoRecentSearch(
    id = id,
    rawQuery = raw_query,
    normalizedQuery = normalized_query,
    createdAtMs = created_at_ms,
    lastUsedAtMs = last_used_at_ms,
    useCount = use_count,
)

internal fun Behavior_session.toModel() = TaggoBehaviorSession(
    id = id,
    startedAtMs = started_at_ms,
    endedAtMs = ended_at_ms,
    platform = platform,
    appVersion = app_version,
    databaseVersion = database_version,
    recommendationModelVersion = recommendation_model_version,
)

internal fun Behavior_event.toModel() = TaggoBehaviorEvent(
    id = id,
    sessionId = session_id,
    occurredAtMs = occurred_at_ms,
    eventType = event_type,
    screenName = screen_name,
    entryPoint = entry_point,
    fileId = file_id,
    fileReferenceId = file_reference_id,
    searchQuery = search_query,
    recommendationSetId = recommendation_set_id,
    recommendationRank = recommendation_rank,
    durationMs = duration_ms,
    extraJson = extra_json,
)

internal fun Explicit_need_signal.toModel() = TaggoExplicitNeedSignal(
    id = id,
    sessionId = session_id,
    behaviorEventId = behavior_event_id,
    signalType = signal_type,
    signalValue = signal_value,
    createdAtMs = created_at_ms,
    consumedByRecommendationSetId = consumed_by_recommendation_set_id,
)

internal fun Recommendation_context.toModel() = TaggoRecommendationContext(
    id = id,
    createdAtMs = created_at_ms,
    contextType = context_type,
    sessionId = session_id,
    triggerFileId = trigger_file_id,
    searchQuery = search_query,
    localHour = local_hour,
    dayOfWeek = day_of_week,
    latitude = latitude,
    longitude = longitude,
    locationPrecision = location_precision,
)

internal fun Recommendation_set.toModel() = TaggoRecommendationSet(
    id = id,
    contextId = context_id,
    generatedAtMs = generated_at_ms,
    setType = set_type,
    modelVersion = model_version,
    policyName = policy_name,
    policyVersion = policy_version,
)

internal fun Recommendation_candidate_snapshot.toModel() = TaggoRecommendationCandidateSnapshot(
    recommendationSetId = recommendation_set_id,
    fileId = file_id,
    rank = rank,
    score = score,
    reasonsJson = reasons_json,
    featuresJson = features_json,
    selectionType = selection_type,
    propensity = propensity,
    selectedAtMs = selected_at_ms,
)

internal fun Recommendation_feedback.toModel() = TaggoRecommendationFeedback(
    id = id,
    recommendationSetId = recommendation_set_id,
    fileId = file_id,
    feedbackType = feedback_type,
    rewardValue = reward_value,
    rankAtFeedback = rank_at_feedback,
    createdAtMs = created_at_ms,
    behaviorEventId = behavior_event_id,
)

internal fun Boolean.toDatabaseLong(): Long = if (this) 1L else 0L
internal fun Recommendation_policy_state.toModel() = TaggoRecommendationPolicyState(
    id = id,
    policyName = policy_name,
    recommendationMode = recommendation_mode,
    modelVersion = model_version,
    weightsJson = weights_json,
    learningConfigJson = learning_config_json,
    updateCount = update_count,
    createdAtMs = created_at_ms,
    lastUpdatedAtMs = last_updated_at_ms,
)

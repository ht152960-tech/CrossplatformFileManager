package com.example.cross_platformfilemanager.data.repository

import com.example.cross_platformfilemanager.data.model.TaggoBehaviorEvent
import com.example.cross_platformfilemanager.data.model.TaggoBehaviorSession
import com.example.cross_platformfilemanager.data.model.TaggoExplicitNeedSignal
import com.example.cross_platformfilemanager.data.model.TaggoFileEntry
import com.example.cross_platformfilemanager.data.model.TaggoFileReference
import com.example.cross_platformfilemanager.data.model.TaggoRecentSearch
import com.example.cross_platformfilemanager.data.model.TaggoRecommendationCandidateSnapshot
import com.example.cross_platformfilemanager.data.model.TaggoRecommendationContext
import com.example.cross_platformfilemanager.data.model.TaggoRecommendationFeedback
import com.example.cross_platformfilemanager.data.model.TaggoRecommendationSet
import com.example.cross_platformfilemanager.data.model.TaggoTag

interface FileEntryRepository {
    suspend fun getActiveFileEntries(): List<TaggoFileEntry>

    suspend fun getFileEntryById(id: String): TaggoFileEntry?

    suspend fun addFileEntry(entry: TaggoFileEntry, primaryReference: TaggoFileReference)

    suspend fun updateFileEntry(entry: TaggoFileEntry)

    suspend fun softDeleteFileEntry(id: String, deletedAtMs: Long, updatedAtMs: Long)

    suspend fun getReferencesForFile(fileId: String): List<TaggoFileReference>

    suspend fun getPrimaryReferenceForFile(fileId: String): TaggoFileReference?

    suspend fun addFileReference(reference: TaggoFileReference)

    suspend fun updateReferenceAvailability(
        referenceId: String,
        referenceAvailable: Boolean,
        lastVerifiedAtMs: Long?,
        updatedAtMs: Long,
    )
}

interface TagRepository {
    suspend fun getActiveTags(): List<TaggoTag>

    suspend fun findTagByNormalizedName(normalizedName: String): TaggoTag?

    suspend fun addTag(tag: TaggoTag)

    suspend fun softDeleteTag(id: String, deletedAtMs: Long, updatedAtMs: Long)

    suspend fun getTagsForFile(fileId: String): List<TaggoTag>

    suspend fun getFilesForTag(tagId: String): List<TaggoFileEntry>

    suspend fun attachTagToFile(fileId: String, tagId: String, createdAtMs: Long)

    suspend fun detachTagFromFile(fileId: String, tagId: String)

    suspend fun detachAllTagsFromFile(fileId: String)
}

interface SearchHistoryRepository {
    suspend fun getRecentSearches(): List<TaggoRecentSearch>

    suspend fun upsertRecentSearch(search: TaggoRecentSearch)

    suspend fun deleteRecentSearch(id: String)
}

interface BehaviorRepository {
    suspend fun startSession(session: TaggoBehaviorSession)

    suspend fun endSession(id: String, endedAtMs: Long)

    suspend fun getSessionById(id: String): TaggoBehaviorSession?

    suspend fun recordEvent(event: TaggoBehaviorEvent)

    suspend fun getEventsForSession(sessionId: String): List<TaggoBehaviorEvent>

    suspend fun getEventsByTypeInRange(
        eventType: String,
        fromMs: Long,
        toMs: Long,
    ): List<TaggoBehaviorEvent> = emptyList()

    suspend fun recordExplicitNeedSignal(signal: TaggoExplicitNeedSignal)

    suspend fun getExplicitNeedSignalsForSession(sessionId: String): List<TaggoExplicitNeedSignal>
}

interface RecommendationRecordRepository {
    suspend fun addRecommendationContext(context: TaggoRecommendationContext)

    suspend fun getRecommendationContextById(id: String): TaggoRecommendationContext?

    suspend fun addRecommendationSet(set: TaggoRecommendationSet)

    suspend fun getRecommendationSetById(id: String): TaggoRecommendationSet?

    suspend fun addCandidateSnapshot(snapshot: TaggoRecommendationCandidateSnapshot)

    suspend fun getCandidateSnapshotsForSet(recommendationSetId: String): List<TaggoRecommendationCandidateSnapshot>

    suspend fun markCandidateSelected(recommendationSetId: String, fileId: String, selectedAtMs: Long)

    suspend fun addRecommendationFeedback(feedback: TaggoRecommendationFeedback)

    suspend fun getFeedbackForSet(recommendationSetId: String): List<TaggoRecommendationFeedback>

    suspend fun getFeedbackInRange(
        fromMs: Long,
        toMs: Long,
    ): List<TaggoRecommendationFeedback> = emptyList()
}

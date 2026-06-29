package com.example.cross_platformfilemanager.runtime

import androidx.compose.runtime.mutableStateListOf
import com.example.cross_platformfilemanager.data.adapter.DefaultTaggoIdGenerator
import com.example.cross_platformfilemanager.data.adapter.SystemTaggoClock
import com.example.cross_platformfilemanager.data.adapter.TaggoClock
import com.example.cross_platformfilemanager.data.adapter.TaggoFileImportInput
import com.example.cross_platformfilemanager.data.adapter.TaggoIdGenerator
import com.example.cross_platformfilemanager.data.adapter.normalizeTagName
import com.example.cross_platformfilemanager.data.db.TaggoDatabaseRepositories
import com.example.cross_platformfilemanager.data.model.TaggoRecentSearch
import com.example.cross_platformfilemanager.data.model.TaggoTag
import com.example.cross_platformfilemanager.data.service.TaggoFileImportService
import kotlin.coroutines.cancellation.CancellationException

class TaggoFileRuntimeStore(
    private val repositories: TaggoDatabaseRepositories? = null,
    private val importService: TaggoFileImportService? = null,
    private val idGenerator: TaggoIdGenerator = DefaultTaggoIdGenerator,
    private val clock: TaggoClock = SystemTaggoClock,
    private val behaviorRuntime: TaggoBehaviorRuntime? = null,
) {
    val files = mutableStateListOf<TaggoRuntimeFile>()
    val recentSearches = mutableStateListOf<String>()

    suspend fun load() {
        val databaseRepositories = repositories ?: return
        files.replaceWith(
            databaseRepositories.fileEntries.getActiveFileEntries().mapNotNull { entry ->
                if (entry.deletedAtMs != null) {
                    null
                } else {
                    val primaryReference =
                        databaseRepositories.fileEntries.getPrimaryReferenceForFile(entry.id)
                            ?: return@mapNotNull null
                    val tags = databaseRepositories.tags.getTagsForFile(entry.id)
                    TaggoRuntimeFile(
                        id = entry.id,
                        displayName = entry.displayName,
                        extension = entry.extension,
                        mimeType = entry.mimeType,
                        taggoFileCategory = entry.taggoFileCategory,
                        sizeBytes = entry.sizeBytes,
                        primaryReferenceId = primaryReference.id,
                        referenceType = primaryReference.referenceType,
                        referenceValue = primaryReference.referenceValue,
                        referenceAvailable = primaryReference.referenceAvailable,
                        platform = primaryReference.platform,
                        tags = tags.map { it.name },
                        createdAtMs = entry.createdAtMs,
                        updatedAtMs = entry.updatedAtMs,
                        lastContentOpenedAtMs = entry.lastContentOpenedAtMs,
                        contentOpenCount = entry.contentOpenCount,
                        thumbnailState = entry.thumbnailState,
                        thumbnailReferenceValue = entry.thumbnailReferenceValue,
                    )
                }
            },
        )
        recentSearches.replaceWith(
            databaseRepositories.searchHistory.getRecentSearches().map { it.rawQuery },
        )
    }

    suspend fun addFile(input: TaggoFileImportInput): TaggoRuntimeFile {
        val service = requireNotNull(importService) {
            "A database-backed runtime store is required to add files"
        }
        val mapping = service.importFile(input)
        load()
        val saved = requireNotNull(files.firstOrNull { it.id == mapping.fileEntry.id })
        recordBehaviorEvent {
            recordFileAdded(saved.id)
        }
        return saved
    }

    suspend fun softDeleteFile(fileId: String) {
        val databaseRepositories = repositories ?: return
        val timestamp = clock.nowMs()
        databaseRepositories.fileEntries.softDeleteFileEntry(fileId, timestamp, timestamp)
        load()
        recordBehaviorEvent {
            recordFileDeleted(fileId)
        }
    }

    suspend fun updateFile(file: TaggoRuntimeFile) {
        val databaseRepositories = repositories ?: return
        val current = databaseRepositories.fileEntries.getFileEntryById(file.id) ?: return
        databaseRepositories.fileEntries.updateFileEntry(
            current.copy(
                displayName = file.displayName,
                extension = file.extension,
                mimeType = file.mimeType,
                taggoFileCategory = file.taggoFileCategory,
                sizeBytes = file.sizeBytes,
                updatedAtMs = clock.nowMs(),
                lastContentOpenedAtMs = file.lastContentOpenedAtMs,
                contentOpenCount = file.contentOpenCount,
                thumbnailState = file.thumbnailState,
                thumbnailReferenceValue = file.thumbnailReferenceValue,
            ),
        )
        load()
    }

    suspend fun recordContentOpen(fileId: String, openedAtMs: Long = clock.nowMs()) {
        val databaseRepositories = repositories ?: return
        val current = databaseRepositories.fileEntries.getFileEntryById(fileId) ?: return
        databaseRepositories.fileEntries.updateFileEntry(
            current.copy(
                updatedAtMs = openedAtMs,
                lastContentOpenedAtMs = openedAtMs,
                contentOpenCount = current.contentOpenCount + 1,
            ),
        )
        load()
    }

    suspend fun addTag(fileId: String, rawName: String): Boolean {
        val databaseRepositories = repositories ?: return false
        val name = rawName.trim()
        val normalizedName = normalizeTagName(name)
        if (normalizedName.isBlank()) return false
        val timestamp = clock.nowMs()
        val tag = databaseRepositories.tags.findTagByNormalizedName(normalizedName)
            ?: TaggoTag(
                id = idGenerator.nextTagId(normalizedName),
                name = name,
                normalizedName = normalizedName,
                createdAtMs = timestamp,
                updatedAtMs = timestamp,
                deletedAtMs = null,
            ).also { databaseRepositories.tags.addTag(it) }
        databaseRepositories.tags.attachTagToFile(fileId, tag.id, timestamp)
        load()
        recordBehaviorEvent {
            recordTagAdded(fileId, tag.name)
        }
        return true
    }

    suspend fun removeTag(fileId: String, rawName: String): Boolean {
        val databaseRepositories = repositories ?: return false
        val tag = databaseRepositories.tags.findTagByNormalizedName(normalizeTagName(rawName))
            ?: return false
        databaseRepositories.tags.detachTagFromFile(fileId, tag.id)
        load()
        recordBehaviorEvent {
            recordTagRemoved(fileId, tag.name)
        }
        return true
    }

    suspend fun removeTagEverywhere(rawName: String): Int {
        val databaseRepositories = repositories ?: return 0
        val tag = databaseRepositories.tags.findTagByNormalizedName(normalizeTagName(rawName))
            ?: return 0
        val linkedFiles = databaseRepositories.tags.getFilesForTag(tag.id)
        linkedFiles.forEach { file ->
            databaseRepositories.tags.detachTagFromFile(file.id, tag.id)
        }
        load()
        return linkedFiles.size
    }

    suspend fun recordSearch(rawQuery: String) {
        val databaseRepositories = repositories ?: return
        val query = rawQuery.trim()
        if (query.isBlank()) return
        val normalizedQuery = query.lowercase()
        val existing = databaseRepositories.searchHistory.getRecentSearches()
            .firstOrNull { it.normalizedQuery == normalizedQuery }
        val timestamp = clock.nowMs()
        databaseRepositories.searchHistory.upsertRecentSearch(
            TaggoRecentSearch(
                id = existing?.id ?: idGenerator.nextRecentSearchId(normalizedQuery),
                rawQuery = query,
                normalizedQuery = normalizedQuery,
                createdAtMs = existing?.createdAtMs ?: timestamp,
                lastUsedAtMs = timestamp,
                useCount = (existing?.useCount ?: 0) + 1,
            ),
        )
        load()
        recordBehaviorEvent {
            recordSearchSubmit(query, screenName = "search")
        }
    }

    fun getFile(fileId: String): TaggoRuntimeFile? = files.firstOrNull { it.id == fileId }

    private suspend fun recordBehaviorEvent(block: suspend TaggoBehaviorRuntime.() -> Unit) {
        val runtime = behaviorRuntime ?: return
        try {
            runtime.block()
        } catch (error: CancellationException) {
            throw error
        } catch (_: Throwable) {
            // 行为日志写入失败不能影响主业务流程。
        }
    }

    private fun <T> MutableList<T>.replaceWith(values: List<T>) {
        clear()
        addAll(values)
    }
}

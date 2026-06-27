package com.example.cross_platformfilemanager.data.repository.sqldelight

import com.example.cross_platformfilemanager.data.db.TaggoDatabase
import com.example.cross_platformfilemanager.data.mapper.toDatabaseLong
import com.example.cross_platformfilemanager.data.mapper.toModel
import com.example.cross_platformfilemanager.data.model.TaggoFileEntry
import com.example.cross_platformfilemanager.data.model.TaggoFileReference
import com.example.cross_platformfilemanager.data.repository.FileEntryRepository

class SqlDelightFileEntryRepository(
    private val database: TaggoDatabase,
) : FileEntryRepository {
    private val queries = database.taggoDatabaseQueries

    override suspend fun getActiveFileEntries(): List<TaggoFileEntry> =
        queries.selectActiveFileEntries().executeAsList().map { it.toModel() }

    override suspend fun getFileEntryById(id: String): TaggoFileEntry? =
        queries.selectFileEntryById(id).executeAsOneOrNull()?.toModel()

    override suspend fun addFileEntry(
        entry: TaggoFileEntry,
        primaryReference: TaggoFileReference,
    ) {
        database.transaction {
            insertFileEntry(entry)
            insertFileReference(primaryReference)
        }
    }

    override suspend fun updateFileEntry(entry: TaggoFileEntry) {
        queries.updateFileEntry(
            display_name = entry.displayName,
            extension = entry.extension,
            mime_type = entry.mimeType,
            taggo_file_category = entry.taggoFileCategory,
            size_bytes = entry.sizeBytes,
            updated_at_ms = entry.updatedAtMs,
            last_content_opened_at_ms = entry.lastContentOpenedAtMs,
            content_open_count = entry.contentOpenCount,
            thumbnail_state = entry.thumbnailState,
            thumbnail_reference_value = entry.thumbnailReferenceValue,
            deleted_at_ms = entry.deletedAtMs,
            id = entry.id,
        )
    }

    override suspend fun softDeleteFileEntry(id: String, deletedAtMs: Long, updatedAtMs: Long) {
        queries.softDeleteFileEntry(deletedAtMs, updatedAtMs, id)
    }

    override suspend fun getReferencesForFile(fileId: String): List<TaggoFileReference> =
        queries.selectReferencesByFileId(fileId).executeAsList().map { it.toModel() }

    override suspend fun getPrimaryReferenceForFile(fileId: String): TaggoFileReference? =
        queries.selectPrimaryReferenceByFileId(fileId).executeAsOneOrNull()?.toModel()

    override suspend fun addFileReference(reference: TaggoFileReference) {
        insertFileReference(reference)
    }

    override suspend fun updateReferenceAvailability(
        referenceId: String,
        referenceAvailable: Boolean,
        lastVerifiedAtMs: Long?,
        updatedAtMs: Long,
    ) {
        queries.updateFileReferenceAvailability(
            referenceAvailable.toDatabaseLong(),
            lastVerifiedAtMs,
            updatedAtMs,
            referenceId,
        )
    }

    private fun insertFileEntry(entry: TaggoFileEntry) {
        queries.insertFileEntry(
            id = entry.id,
            display_name = entry.displayName,
            extension = entry.extension,
            mime_type = entry.mimeType,
            taggo_file_category = entry.taggoFileCategory,
            size_bytes = entry.sizeBytes,
            created_at_ms = entry.createdAtMs,
            updated_at_ms = entry.updatedAtMs,
            last_content_opened_at_ms = entry.lastContentOpenedAtMs,
            content_open_count = entry.contentOpenCount,
            thumbnail_state = entry.thumbnailState,
            thumbnail_reference_value = entry.thumbnailReferenceValue,
            deleted_at_ms = entry.deletedAtMs,
        )
    }

    private fun insertFileReference(reference: TaggoFileReference) {
        queries.insertFileReference(
            id = reference.id,
            file_id = reference.fileId,
            reference_type = reference.referenceType,
            reference_value = reference.referenceValue,
            reference_available = reference.referenceAvailable.toDatabaseLong(),
            platform = reference.platform,
            created_at_ms = reference.createdAtMs,
            updated_at_ms = reference.updatedAtMs,
            last_verified_at_ms = reference.lastVerifiedAtMs,
            is_primary = reference.isPrimary.toDatabaseLong(),
        )
    }
}

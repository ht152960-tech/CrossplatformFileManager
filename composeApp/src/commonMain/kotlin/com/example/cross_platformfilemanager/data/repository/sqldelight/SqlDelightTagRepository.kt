package com.example.cross_platformfilemanager.data.repository.sqldelight

import com.example.cross_platformfilemanager.data.db.TaggoDatabase
import com.example.cross_platformfilemanager.data.mapper.toModel
import com.example.cross_platformfilemanager.data.model.TaggoFileEntry
import com.example.cross_platformfilemanager.data.model.TaggoTag
import com.example.cross_platformfilemanager.data.repository.TagRepository

class SqlDelightTagRepository(
    private val database: TaggoDatabase,
) : TagRepository {
    private val queries = database.taggoDatabaseQueries

    override suspend fun getActiveTags(): List<TaggoTag> =
        queries.selectActiveTags().executeAsList().map { it.toModel() }

    override suspend fun findTagByNormalizedName(normalizedName: String): TaggoTag? =
        queries.selectTagByNormalizedName(normalizedName).executeAsOneOrNull()?.toModel()

    override suspend fun addTag(tag: TaggoTag) {
        queries.insertTag(
            tag.id,
            tag.name,
            tag.normalizedName,
            tag.createdAtMs,
            tag.updatedAtMs,
            tag.deletedAtMs,
        )
    }

    override suspend fun softDeleteTag(id: String, deletedAtMs: Long, updatedAtMs: Long) {
        queries.softDeleteTag(deletedAtMs, updatedAtMs, id)
    }

    override suspend fun getTagsForFile(fileId: String): List<TaggoTag> =
        queries.selectTagsForFile(fileId).executeAsList().map { it.toModel() }

    override suspend fun getFilesForTag(tagId: String): List<TaggoFileEntry> =
        queries.selectFilesForTag(tagId).executeAsList().map { it.toModel() }

    override suspend fun attachTagToFile(fileId: String, tagId: String, createdAtMs: Long) {
        queries.insertFileTag(fileId, tagId, createdAtMs)
    }

    override suspend fun detachTagFromFile(fileId: String, tagId: String) {
        queries.deleteFileTag(fileId, tagId)
    }

    override suspend fun detachAllTagsFromFile(fileId: String) {
        queries.deleteFileTagsForFile(fileId)
    }
}

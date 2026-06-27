package com.example.cross_platformfilemanager.data.service

import com.example.cross_platformfilemanager.data.adapter.TaggoClock
import com.example.cross_platformfilemanager.data.adapter.TaggoFileImportInput
import com.example.cross_platformfilemanager.data.adapter.TaggoFileImportMapping
import com.example.cross_platformfilemanager.data.adapter.TaggoIdGenerator
import com.example.cross_platformfilemanager.data.adapter.createTaggoFileImportMapping
import com.example.cross_platformfilemanager.data.repository.FileEntryRepository
import com.example.cross_platformfilemanager.data.repository.TagRepository

class TaggoFileImportService(
    private val fileEntries: FileEntryRepository,
    private val tags: TagRepository,
    private val idGenerator: TaggoIdGenerator,
    private val clock: TaggoClock,
    private val platform: String?,
) {
    suspend fun importFile(input: TaggoFileImportInput): TaggoFileImportMapping {
        val mapping = createTaggoFileImportMapping(
            input = input,
            idGenerator = idGenerator,
            clock = clock,
            platform = platform,
        )
        fileEntries.addFileEntry(mapping.fileEntry, mapping.primaryReference)

        mapping.tags.forEach { mappedTag ->
            val storedTag = tags.findTagByNormalizedName(mappedTag.normalizedName)
                ?: mappedTag.also { tags.addTag(it) }
            tags.attachTagToFile(
                fileId = mapping.fileEntry.id,
                tagId = storedTag.id,
                createdAtMs = mapping.fileEntry.createdAtMs,
            )
        }

        return mapping
    }
}

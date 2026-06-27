package com.example.cross_platformfilemanager.data.adapter

import com.example.cross_platformfilemanager.data.model.TaggoFileEntry
import com.example.cross_platformfilemanager.data.model.TaggoFileReference
import com.example.cross_platformfilemanager.data.model.TaggoTag

data class TaggoFileImportInput(
    val oldId: String?,
    val displayName: String,
    val extension: String?,
    val mimeType: String?,
    val referenceValue: String,
    val sizeBytes: Long?,
    val tags: List<String>,
    val thumbnailState: String = "none",
    val thumbnailReferenceValue: String? = null,
)

data class TaggoFileImportMapping(
    val fileEntry: TaggoFileEntry,
    val primaryReference: TaggoFileReference,
    val tags: List<TaggoTag>,
)

fun createTaggoFileImportMapping(
    input: TaggoFileImportInput,
    idGenerator: TaggoIdGenerator,
    clock: TaggoClock,
    platform: String?,
): TaggoFileImportMapping {
    val timestamp = clock.nowMs()
    val fileEntryId = input.oldId?.takeIf { it.isNotBlank() }
        ?: idGenerator.nextFileEntryId()

    val fileEntry = TaggoFileEntry(
        id = fileEntryId,
        displayName = input.displayName,
        extension = input.extension,
        mimeType = input.mimeType,
        taggoFileCategory = inferTaggoFileCategory(input.extension, input.mimeType),
        sizeBytes = input.sizeBytes,
        createdAtMs = timestamp,
        updatedAtMs = timestamp,
        lastContentOpenedAtMs = null,
        contentOpenCount = 0,
        thumbnailState = input.thumbnailState,
        thumbnailReferenceValue = input.thumbnailReferenceValue,
        deletedAtMs = null,
    )
    val primaryReference = TaggoFileReference(
        id = idGenerator.nextFileReferenceId(),
        fileId = fileEntryId,
        referenceType = inferTaggoReferenceType(input.referenceValue),
        referenceValue = input.referenceValue,
        referenceAvailable = true,
        platform = platform,
        createdAtMs = timestamp,
        updatedAtMs = timestamp,
        lastVerifiedAtMs = null,
        isPrimary = true,
    )
    val seenNormalizedNames = mutableSetOf<String>()
    val tags = input.tags.mapNotNull { rawName ->
        val name = rawName.trim()
        val normalizedName = normalizeTagName(name)
        if (normalizedName.isBlank() || !seenNormalizedNames.add(normalizedName)) {
            null
        } else {
            TaggoTag(
                id = idGenerator.nextTagId(normalizedName),
                name = name,
                normalizedName = normalizedName,
                createdAtMs = timestamp,
                updatedAtMs = timestamp,
                deletedAtMs = null,
            )
        }
    }

    return TaggoFileImportMapping(
        fileEntry = fileEntry,
        primaryReference = primaryReference,
        tags = tags,
    )
}

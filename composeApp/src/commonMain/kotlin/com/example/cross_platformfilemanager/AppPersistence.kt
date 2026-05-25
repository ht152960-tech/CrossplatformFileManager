package com.example.cross_platformfilemanager

interface AppSnapshotStore {
    suspend fun load(): AppSnapshot?
    suspend fun save(snapshot: AppSnapshot)
}

expect fun createAppSnapshotStore(): AppSnapshotStore?

data class BrowserReferenceDraft(
    val title: String,
    val source: String,
    val fileType: String,
    val fileSizeBytes: Long? = null,
    val coverArtSource: String? = null,
    val notes: String,
)

interface BrowserReferencePicker {
    suspend fun pickReference(): BrowserReferenceDraft?
}

expect fun createBrowserReferencePicker(): BrowserReferencePicker?

interface BrowserReferenceResolver {
    suspend fun resolveReference(reference: FileReference): BrowserReferenceDraft?
}

expect fun createBrowserReferenceResolver(): BrowserReferenceResolver?

fun BrowserReferenceDraft.normalized(): BrowserReferenceDraft = copy(
    title = title.trim(),
    source = source.trim(),
    fileType = fileType.trim(),
    fileSizeBytes = fileSizeBytes,
    coverArtSource = coverArtSource?.trim()?.takeIf { it.isNotBlank() },
    notes = notes.trim(),
)

fun BrowserReferenceDraft.toReference(
    id: String,
    sourceKind: FileSourceKind,
    createdAtMillis: Long,
    lastOpenedAtMillis: Long = createdAtMillis,
    tags: List<String> = emptyList(),
    isFavorite: Boolean = false,
): FileReference {
    val normalized = normalized()
    val reference = FileReference(
        id = id,
        title = normalized.title.ifBlank { "Untitled file" },
        source = normalized.source.ifBlank { "browser-handle:unknown" },
        sourceKind = sourceKind,
        fileType = normalized.fileType.ifBlank { "FILE" },
        fileSizeBytes = normalized.fileSizeBytes,
        coverArtSource = normalized.coverArtSource,
        tags = tags.map { it.trim() }.filter { it.isNotBlank() }.distinct(),
        notes = normalized.notes,
        createdAtMillis = createdAtMillis,
        lastOpenedAtMillis = lastOpenedAtMillis,
        isFavorite = isFavorite,
    )
    return reference.copy(thumbnailStatus = reference.initialThumbnailStatus())
}

fun FileReference.toBrowserReferenceDraft(): BrowserReferenceDraft = BrowserReferenceDraft(
    title = title,
    source = source,
    fileType = fileType,
    fileSizeBytes = fileSizeBytes,
    coverArtSource = coverArtSource,
    notes = notes,
)

package com.example.cross_platformfilemanager

interface AppSnapshotStore {
    suspend fun load(): AppSnapshot?
    suspend fun save(snapshot: AppSnapshot)
}

expect fun createAppSnapshotStore(): AppSnapshotStore?

interface LocalDataController {
    suspend fun exportSnapshot(): String?
    suspend fun importSnapshot(): String?
    suspend fun clearAllData()
}

expect fun createLocalDataController(): LocalDataController?

data class BrowserReferenceDraft(
    val title: String,
    val source: String,
    val fileType: String,
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
    notes = notes.trim(),
)

fun BrowserReferenceDraft.toReference(
    id: String,
    sourceKind: FileSourceKind,
    createdAtMillis: Long,
    lastOpenedAtMillis: Long = createdAtMillis,
    tags: List<String> = emptyList(),
    isFavorite: Boolean = false,
): FileReference = FileReference(
    id = id,
    title = title.trim().ifBlank { "Untitled file" },
    source = source.trim().ifBlank { "browser-handle:unknown" },
    sourceKind = sourceKind,
    fileType = fileType.trim().ifBlank { "FILE" },
    tags = tags.map { it.trim() }.filter { it.isNotBlank() }.distinct(),
    notes = notes.trim(),
    createdAtMillis = createdAtMillis,
    lastOpenedAtMillis = lastOpenedAtMillis,
    isFavorite = isFavorite,
)

fun FileReference.toBrowserReferenceDraft(): BrowserReferenceDraft = BrowserReferenceDraft(
    title = title,
    source = source,
    fileType = fileType,
    notes = notes,
)

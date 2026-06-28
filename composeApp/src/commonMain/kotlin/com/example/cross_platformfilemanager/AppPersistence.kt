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
)

fun FileReference.toBrowserReferenceDraft(): BrowserReferenceDraft = BrowserReferenceDraft(
    title = displayName,
    source = referenceValue,
    fileType = taggoFileCategory,
    fileSizeBytes = sizeBytes,
)

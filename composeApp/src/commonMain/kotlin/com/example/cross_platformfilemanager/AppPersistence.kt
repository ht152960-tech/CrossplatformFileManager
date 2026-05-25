package com.example.cross_platformfilemanager

/**
 * 应用快照存储接口。
 *
 * 它抽象的是整个工作区快照的读取和保存能力，
 * 让上层状态管理不依赖具体平台的文件、浏览器存储或其他持久化细节。
 */
interface AppSnapshotStore {
    suspend fun load(): AppSnapshot?
    suspend fun save(snapshot: AppSnapshot)
}

expect fun createAppSnapshotStore(): AppSnapshotStore?

/**
 * 浏览器或外部选择器返回的文件草稿。
 *
 * 这个对象还不是正式入库的文件条目，
 * 需要经过规范化并补全业务字段后，才能转换成 [FileReference]。
 */
data class BrowserReferenceDraft(
    val title: String,
    val source: String,
    val fileType: String,
    val fileSizeBytes: Long? = null,
    val coverArtSource: String? = null,
    val notes: String,
)

/**
 * 文件选择器抽象接口。
 *
 * 各平台可以用自己的文件选择能力返回一个文件草稿，
 * 上层只关心是否成功拿到草稿，不关心具体选择过程。
 */
interface BrowserReferencePicker {
    suspend fun pickReference(): BrowserReferenceDraft?
}

expect fun createBrowserReferencePicker(): BrowserReferencePicker?

/**
 * 浏览器引用解析器抽象接口。
 *
 * 当某些平台只能拿到句柄或外部引用时，通过这里把引用解析成可入库的文件草稿。
 */
interface BrowserReferenceResolver {
    suspend fun resolveReference(reference: FileReference): BrowserReferenceDraft?
}

expect fun createBrowserReferenceResolver(): BrowserReferenceResolver?

/**
 * 规范化浏览器文件草稿。
 *
 * 这个步骤只清理空白和空字符串，不做业务补默认值，
 * 便于后续转换为正式条目时集中决定兜底策略。
 */
fun BrowserReferenceDraft.normalized(): BrowserReferenceDraft = copy(
    title = title.trim(),
    source = source.trim(),
    fileType = fileType.trim(),
    fileSizeBytes = fileSizeBytes,
    coverArtSource = coverArtSource?.trim()?.takeIf { it.isNotBlank() },
    notes = notes.trim(),
)

/**
 * 把文件草稿转换成正式文件条目。
 *
 * 这里会补齐默认标题、默认来源、默认类型，并把标签做基础清洗，
 * 让后续仓储层接收到的是可直接使用的业务对象。
 */
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

/**
 * 把正式文件条目还原成可编辑的文件草稿。
 *
 * 这个转换主要服务“替换文件”或“继续编辑当前条目”的流程。
 */
fun FileReference.toBrowserReferenceDraft(): BrowserReferenceDraft = BrowserReferenceDraft(
    title = title,
    source = source,
    fileType = fileType,
    fileSizeBytes = fileSizeBytes,
    coverArtSource = coverArtSource,
    notes = notes,
)

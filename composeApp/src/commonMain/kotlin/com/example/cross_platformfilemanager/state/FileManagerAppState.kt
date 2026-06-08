package com.example.cross_platformfilemanager

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * 应用状态层，负责把文件仓储、推荐引擎、快照恢复和界面可读状态组织在一起。
 *
 * 推荐相关的职责主要集中在：
 * - 组装推荐候选文件
 * - 暴露推荐结果与打分明细
 * - 在打开文件时同步写入推荐引擎的打开事件
 * - 在导入导出快照时保留推荐日志和推荐状态
 */
class FileManagerAppState(
    private val repository: InMemoryFileRepository = InMemoryFileRepository(),
    private val recommendationEngine: RecommendationEngine = RecommendationEngine(),
    private val browserReferenceResolver: BrowserReferenceResolver? = null,
    private val thumbnailGenerator: ThumbnailGenerator? = null,
) : RecommendationReadOnlyState {
    val searchTags = androidx.compose.runtime.mutableStateListOf<SearchTag>()
    val startupDefaultLocale = AppLocale.ZhCn
    private val defaultDraftTitle = ""
    private val defaultDraftSource = ""
    private val defaultDraftType = ""
    private val defaultDraftFileSizeBytes: Long? = null
    private val defaultDraftCoverArtSource = ""
    private val defaultDraftTags = ""
    private val defaultDraftNotes = ""

    var preferredLocale by mutableStateOf(startupDefaultLocale)
    var locale by mutableStateOf(startupDefaultLocale)
    var query by mutableStateOf("")
    var selectedTag by mutableStateOf<String?>(null)
    var selectedFileType by mutableStateOf<String?>(null)
    var favoritesOnly by mutableStateOf(false)

    var draftTitle by mutableStateOf(defaultDraftTitle)
    var draftSource by mutableStateOf(defaultDraftSource)
    var draftType by mutableStateOf(defaultDraftType)
    var draftFileSizeBytes by mutableStateOf(defaultDraftFileSizeBytes)
    var draftCoverArtSource by mutableStateOf(defaultDraftCoverArtSource)
    var draftTags by mutableStateOf(defaultDraftTags)
    var draftNotes by mutableStateOf(defaultDraftNotes)

    var activeReferenceId by mutableStateOf(repository.references.firstOrNull()?.id)
    var snapshotVersion by mutableStateOf(0)

    private val strings: UiStrings
        get() = AppStrings.forLocale(locale)

    val subtitle: String
        get() = strings.subtitle

    val appName: String
        get() = strings.appName

    val allFilesTab: String
        get() = strings.allFilesTab

    val detailPanelTitle: String
        get() = strings.detailPanelTitle

    val recentlyAdded: String
        get() = strings.recentlyAdded

    val recommendationTitle: String
        get() = strings.recommendationTitle

    val recommendationSubtitle: String
        get() = strings.recommendationSubtitle

    val noRecommendations: String
        get() = strings.noRecommendations

    val searchTitle: String
        get() = strings.searchTitle

    val searchPlaceholder: String
        get() = strings.searchPlaceholder

    val referenceTitle: String
        get() = strings.referenceTitle

    val referenceLocation: String
        get() = strings.referenceLocation

    val fileType: String
        get() = strings.fileType

    val tagsCommaSeparated: String
        get() = strings.tagsCommaSeparated

    val tagEditorTitle: String
        get() = strings.tagEditorTitle

    val tagLibraryTitle: String
        get() = strings.tagLibraryTitle

    val tagLibrarySubtitle: String
        get() = strings.tagLibrarySubtitle

    val tagLibraryEmpty: String
        get() = strings.tagLibraryEmpty

    val addTag: String
        get() = strings.addTag

    val removeTag: String
        get() = strings.removeTag

    val notes: String
        get() = strings.notes

    val createdAtLabel: String
        get() = strings.createdAtLabel

    val lastOpenedAtLabel: String
        get() = strings.lastOpenedAtLabel

    val save: String
        get() = strings.save

    val open: String
        get() = strings.open

    val delete: String
        get() = strings.delete

    val cancel: String
        get() = strings.cancel

    val addReference: String
        get() = strings.addReference

    val emptyResultsTitle: String
        get() = strings.emptyResultsTitle

    val emptyResultsBody: String
        get() = strings.emptyResultsBody

    /**
     * 当前搜索条件下的建议列表。
     *
     * 这里调用的是推荐引擎的建议入口，主要服务搜索联想和标签建议。
     */
    val recommendations: List<Suggestion>
        get() = recommendationEngine.suggest(
            query = query,
            references = repository.references,
            recentSearches = repository.recentSearches,
            selectedTag = selectedTag,
        )

    val searchResults: List<SearchResult>
        get() = repository.search(searchTags.toList())

    val topTags: List<String>
        get() = repository.topTags()

    val allTags: List<String>
        get() = repository.allTags()

    val recentReferences: List<FileReference>
        get() = repository.recentReferences()

    val allReferences: List<FileReference>
        get() = repository.references

    /**
     * 面向界面展示的推荐文件列表。
     *
     * 这里返回的是去掉打分明细后的文件条目，用于常规列表展示。
     */
    override val recommendedReferences: List<FileReference>
        get() = recommendationEngine.recommend(
            references = recommendationCandidates(),
            previousFileId = activeReferenceId,
            nowMillis = nowMillis(),
            limit = 10,
        ).map { it.file }

    /**
     * 保留打分明细的推荐结果。
     *
     * 这个视图更适合调试、解释推荐结果或后续扩展推荐说明能力。
     */
    override val scoredRecommendedReferences: List<ScoredRecommendation>
        get() = recommendationEngine.recommend(
            references = recommendationCandidates(),
            previousFileId = activeReferenceId,
            nowMillis = nowMillis(),
            limit = 10,
        )

    val recentAddedReferences: List<FileReference>
        get() {
            val now = nowMillis()
            return repository.references
                .asSequence()
                .filter { shouldShowInNewUploadList(it, now) }
                .sortedByDescending { it.createdAtMillis }
                .take(5)
                .toList()
        }

    val activeReference: FileReference?
        get() = repository.references.firstOrNull { it.id == activeReferenceId }

    /**
     * 导出当前工作区快照。
     *
     * 除了普通界面状态，这里还会一起导出推荐日志和推荐引擎状态，
     * 这样恢复后推荐算法可以延续先前的学习结果。
     */
    fun exportSnapshot(): AppSnapshot = AppSnapshot(
        locale = preferredLocale,
        query = query,
        searchTags = searchTags.toList(),
        selectedTag = selectedTag,
        selectedFileType = selectedFileType,
        favoritesOnly = favoritesOnly,
        activeReferenceId = activeReferenceId,
        references = repository.references.toList(),
        recentSearches = repository.recentSearches.toList(),
        recommendationLogs = repository.recommendationLogs.toList(),
        recommendationState = recommendationEngine.exportSnapshot(),
    )

    /**
     * 用完整快照恢复当前工作区。
     *
     * 恢复推荐状态后，还会重新校正活动文件，避免快照里的活动文件已经不存在时留下悬空引用。
     */
    fun restoreSnapshot(snapshot: AppSnapshot) {
        preferredLocale = snapshot.locale
        locale = preferredLocale
        query = snapshot.query
        searchTags.clear()
        searchTags.addAll(snapshot.searchTags)
        selectedTag = snapshot.selectedTag
        selectedFileType = snapshot.selectedFileType
        favoritesOnly = snapshot.favoritesOnly
        repository.replaceReferences(snapshot.references)
        repository.replaceRecentSearches(snapshot.recentSearches)
        repository.replaceRecommendationLogs(snapshot.recommendationLogs)
        recommendationEngine.restoreSnapshot(snapshot.recommendationState)
        // 恢复后如果原来的 activeReferenceId 已经不存在，就回退到第一个可用文件，避免悬空引用。
        activeReferenceId = repository.findReferenceById(snapshot.activeReferenceId ?: "")?.id
            ?: repository.references.firstOrNull()?.id
        snapshotVersion++
    }

    /**
     * 把外部快照内容合并进当前工作区。
     *
     * 这个过程会合并文件条目、搜索标签、最近搜索、推荐日志和推荐状态，
     * 适合导入附加数据而不是完全覆盖当前状态。
     */
    fun mergeSnapshot(snapshot: AppSnapshot) {
        snapshot.references.forEach { reference ->
            repository.upsertReference(reference)
        }
        snapshot.searchTags.forEach { tag ->
            addSearchTag(tag)
        }
        repository.replaceRecentSearches(
            (repository.recentSearches + snapshot.recentSearches)
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinct()
                .take(12),
        )
        repository.replaceRecommendationLogs(
            (repository.recommendationLogs + snapshot.recommendationLogs)
                .distinctBy { it.id }
                .sortedBy { it.generatedAtMillis }
                .takeLast(30),
        )
        if (snapshot.recommendationState != null) {
            recommendationEngine.restoreSnapshot(snapshot.recommendationState)
        }
        // 合并快照后，同样要保证 activeReferenceId 指向一个真实存在的文件。
        activeReferenceId = repository.findReferenceById(activeReferenceId ?: "")
            ?.id
            ?: repository.findReferenceById(snapshot.activeReferenceId ?: "")
            ?.id
            ?: repository.references.firstOrNull()?.id
        snapshotVersion++
    }

    fun resetWorkspace() {
        resetWorkspaceFields()
        activeReferenceId = repository.references.firstOrNull()?.id
        snapshotVersion++
    }

    fun toggleLocale() {
        val nextLocale = when (locale) {
            AppLocale.ZhCn -> AppLocale.EnUs
            AppLocale.EnUs -> AppLocale.ZhCn
        }
        locale = nextLocale
        preferredLocale = nextLocale
        snapshotVersion++
    }

    /**
     * 清空当前工作区中的本地数据和推荐学习状态。
     */
    fun clearLocalData() {
        repository.clearAllData()
        recommendationEngine.clear()
        resetWorkspaceFields()
        activeReferenceId = null
        snapshotVersion++
    }

    private fun resetWorkspaceFields() {
        preferredLocale = startupDefaultLocale
        locale = startupDefaultLocale
        query = ""
        searchTags.clear()
        selectedTag = null
        selectedFileType = null
        favoritesOnly = false
        draftTitle = defaultDraftTitle
        draftSource = defaultDraftSource
        draftType = defaultDraftType
        draftFileSizeBytes = defaultDraftFileSizeBytes
        draftCoverArtSource = defaultDraftCoverArtSource
        draftTags = defaultDraftTags
        draftNotes = defaultDraftNotes
    }

    fun addDraftReference(): FileReference {
        val createdAtMillis = nowMillis()
        val id = "ref-$createdAtMillis"
        val source = draftSource.trim().ifBlank { "/local/path/$id" }
        val tags = draftTags
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

        val saved = repository.upsertReference(
            BrowserReferenceDraft(
                title = draftTitle,
                source = source,
                fileType = draftType,
                fileSizeBytes = draftFileSizeBytes ?: guessFileSizeFromNotes(draftNotes),
                coverArtSource = draftCoverArtSource.trim().ifBlank { null },
                notes = draftNotes,
            ).toReference(
                id = id,
                sourceKind = guessSourceKind(source),
                tags = tags,
                createdAtMillis = createdAtMillis,
                lastOpenedAtMillis = createdAtMillis,
            )
        )

        activeReferenceId = saved.id
        snapshotVersion++
        return saved
    }

    fun replaceReference(referenceId: String, draft: BrowserReferenceDraft): FileReference? {
        val current = repository.findReferenceById(referenceId) ?: return null
        val normalized = draft.normalized()
        val updatedBase = normalized.toReference(
            id = current.id,
            sourceKind = guessSourceKind(normalized.source.ifBlank { current.source }),
            createdAtMillis = current.createdAtMillis,
            lastOpenedAtMillis = current.lastOpenedAtMillis,
            tags = current.tags,
            isFavorite = current.isFavorite,
        )
        val updated = updatedBase.copy(
            title = normalized.title.ifBlank { current.title },
            source = normalized.source.ifBlank { current.source },
            fileType = normalized.fileType.ifBlank { current.fileType },
            fileSizeBytes = normalized.fileSizeBytes ?: guessFileSizeFromNotes(normalized.notes) ?: current.fileSizeBytes,
            coverArtSource = normalized.coverArtSource ?: current.coverArtSource,
            notes = normalized.notes.ifBlank { current.notes },
            thumbnailPath = null,
            thumbnailStatus = updatedBase.initialThumbnailStatus(),
        )

        current.thumbnailPath?.let { path -> thumbnailGenerator?.deleteThumbnail(path) }
        val replaced = repository.replaceReference(referenceId, updated) ?: return null
        activeReferenceId = replaced.id
        snapshotVersion++
        return replaced
    }

    fun applyBrowserDraft(draft: BrowserReferenceDraft) {
        val normalized = draft.normalized()
        draftTitle = normalized.title.ifBlank { "Untitled file" }
        draftSource = normalized.source.ifBlank { "browser-handle:unknown" }
        draftType = normalized.fileType.ifBlank { "FILE" }
        draftFileSizeBytes = normalized.fileSizeBytes ?: guessFileSizeFromNotes(normalized.notes)
        draftCoverArtSource = normalized.coverArtSource.orEmpty()
        draftNotes = normalized.notes.ifBlank { "Selected from browser file picker." }
        snapshotVersion++
    }

    fun submitSearch(rawInput: String): Boolean {
        val trimmed = rawInput.trim()
        if (trimmed.isBlank()) return false

        val tokens = tokenizeSubmittedSearch(trimmed)
        if (tokens.isEmpty()) return false

        query = trimmed
        tokens.forEach { token ->
            addSearchTag(SearchTag(value = token, source = SearchTagSource.Input))
        }
        repository.recordSearch(trimmed)
        snapshotVersion++
        return true
    }

    fun resetSearchSession() {
        query = ""
        searchTags.clear()
        snapshotVersion++
    }

    fun addSearchTag(tag: SearchTag): Boolean {
        val normalizedValue = normalizeSearchTagToken(tag.value)
        if (normalizedValue.isBlank()) return false
        if (searchTags.any { normalizeSearchTagToken(it.value) == normalizedValue }) return false
        searchTags.add(SearchTag(value = normalizedValue, source = tag.source))
        snapshotVersion++
        return true
    }

    fun removeSearchTag(tagValue: String) {
        val normalizedValue = normalizeSearchTagToken(tagValue)
        if (normalizedValue.isBlank()) return
        val removed = searchTags.removeAll { normalizeSearchTagToken(it.value) == normalizedValue }
        if (!removed) return
        if (searchTags.isEmpty()) {
            query = ""
        }
        snapshotVersion++
    }

    /**
     * 打开一个文件条目，并把这次行为写入推荐学习链路。
     *
     * 仓储层更新时间戳后，推荐引擎会收到同一时刻的打开事件，
     * 以便同步更新时间规律和后继关系。
     */
    fun openReference(referenceId: String) {
        val target = repository.findReferenceById(referenceId) ?: return
        val previousReferenceId = activeReferenceId
        val openedAtMillis = nowMillis()
        repository.open(referenceId)
        recommendationEngine.recordFileOpen(
            fileId = referenceId,
            openedAtMillis = openedAtMillis,
            previousFileId = previousReferenceId,
        )
        activeReferenceId = target.id
        snapshotVersion++
    }

    suspend fun refreshReference(referenceId: String) {
        val current = repository.references.firstOrNull { it.id == referenceId } ?: return
        if (!current.source.startsWith("browser-", ignoreCase = true)) {
            openReference(referenceId)
            return
        }

        val resolved = browserReferenceResolver?.resolveReference(current) ?: run {
            openReference(referenceId)
            return
        }

        repository.updateReference(referenceId) { existing ->
            val resolvedSize = resolved.fileSizeBytes
                ?: guessFileSizeFromNotes(resolved.notes)
                ?: existing.fileSizeBytes
            val openedAtMillis = nowMillis()
            existing.copy(
                title = resolved.title.trim().ifBlank { existing.title },
                source = resolved.source.trim().ifBlank { existing.source },
                sourceKind = FileSourceKind.BrowserHandle,
                fileType = resolved.fileType.trim().ifBlank { existing.fileType },
                fileSizeBytes = resolvedSize,
                notes = resolved.notes.trim().ifBlank { existing.notes },
                lastOpenedAtMillis = openedAtMillis,
            )
        }
        // 这里已经在 openReference() 里记录过一次“打开”了，刷新元数据时不再重复写推荐学习信号。
        activeReferenceId = referenceId
        snapshotVersion++
    }

    suspend fun refreshBrowserReferences(): Int {
        val browserReferences = repository.references
            .filter { it.source.startsWith("browser-", ignoreCase = true) }

        var refreshed = 0
        browserReferences.forEach { reference ->
            val resolved = browserReferenceResolver?.resolveReference(reference) ?: return@forEach
            repository.updateReference(reference.id) { existing ->
                val resolvedSize = resolved.fileSizeBytes
                    ?: guessFileSizeFromNotes(resolved.notes)
                    ?: existing.fileSizeBytes
                existing.copy(
                    title = resolved.title.trim().ifBlank { existing.title },
                    source = resolved.source.trim().ifBlank { existing.source },
                    sourceKind = FileSourceKind.BrowserHandle,
                    fileType = resolved.fileType.trim().ifBlank { existing.fileType },
                    fileSizeBytes = resolvedSize,
                    notes = resolved.notes.trim().ifBlank { existing.notes },
                    lastOpenedAtMillis = nowMillis(),
                )
            }
            refreshed++
        }

        if (refreshed > 0) {
            snapshotVersion++
        }
        return refreshed
    }

    fun toggleFavorite(referenceId: String) {
        repository.toggleFavorite(referenceId)
        snapshotVersion++
    }

    fun deleteReference(referenceId: String) {
        val current = repository.findReferenceById(referenceId) ?: return
        current.thumbnailPath?.let { path -> thumbnailGenerator?.deleteThumbnail(path) }
        if (!repository.deleteReference(referenceId)) {
            return
        }
        if (activeReferenceId == referenceId) {
            activeReferenceId = repository.references.firstOrNull()?.id
        }
        snapshotVersion++
    }

    fun updateReferenceTags(referenceId: String, tagsText: String) {
        val tags = tagsText
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

        repository.updateReferenceTags(referenceId, tags)
        snapshotVersion++
    }

    fun deleteTagEverywhere(tag: String): Int {
        val removedFromReferences = repository.removeTagEverywhere(tag)
        if (selectedTag?.let(::normalize) == normalize(tag)) {
            selectedTag = null
        }
        if (normalize(query) == normalize(tag)) {
            query = ""
        }
        if (removedFromReferences > 0) {
            snapshotVersion++
        }
        return removedFromReferences
    }

    fun updateReferenceNotes(referenceId: String, notes: String) {
        repository.updateReference(referenceId) { existing ->
            existing.copy(notes = notes.trim())
        }
        snapshotVersion++
    }

    fun updateReferenceTitle(referenceId: String, title: String) {
        repository.updateReference(referenceId) { existing ->
            existing.copy(title = title.trim().ifBlank { existing.title })
        }
        snapshotVersion++
    }

    fun updateReferenceFileType(referenceId: String, fileType: String) {
        repository.updateReference(referenceId) { existing ->
            existing.copy(fileType = fileType.trim().ifBlank { existing.fileType })
        }
        snapshotVersion++
    }

    suspend fun generateThumbnailForReference(referenceId: String) {
        val current = repository.findReferenceById(referenceId) ?: return
        println("Thumbnail request: fileId=${current.id}, sourceKind=${current.sourceKind}, fileType=${current.fileType}, source=${current.source}")
        if (!current.needsThumbnailGeneration()) {
            println("Thumbnail skipped as unsupported: fileId=${current.id}, fileType=${current.fileType}")
            repository.updateReference(referenceId) { existing ->
                existing.copy(thumbnailPath = null, thumbnailStatus = ThumbnailStatus.UNSUPPORTED)
            }
            snapshotVersion++
            return
        }
        if (current.thumbnailStatus == ThumbnailStatus.GENERATING) {
            return
        }
        if (current.thumbnailStatus == ThumbnailStatus.READY && !current.thumbnailPath.isNullOrBlank()) {
            return
        }

        repository.updateReference(referenceId) { existing ->
            existing.copy(thumbnailStatus = ThumbnailStatus.GENERATING)
        }
        snapshotVersion++

        println("Thumbnail generation start: fileId=${current.id}, fileType=${current.fileType}, mimeType=${current.fileType}")
        val result = thumbnailGenerator?.generateThumbnail(repository.findReferenceById(referenceId) ?: current)
            ?: ThumbnailResult.Unsupported("thumbnail generator unavailable")

        repository.updateReference(referenceId) { existing ->
            when (result) {
                is ThumbnailResult.Ready -> existing.copy(
                    thumbnailPath = result.thumbnailPath,
                    thumbnailStatus = ThumbnailStatus.READY,
                )

                is ThumbnailResult.Failed -> {
                    println("Thumbnail generation failed for ${existing.id}: ${result.reason}")
                    existing.copy(
                        thumbnailPath = null,
                        thumbnailStatus = ThumbnailStatus.FAILED,
                    )
                }

                is ThumbnailResult.Unsupported -> {
                    println("Thumbnail generation unsupported for ${existing.id}: ${result.reason}")
                    existing.copy(
                        thumbnailPath = null,
                        thumbnailStatus = ThumbnailStatus.UNSUPPORTED,
                    )
                }
            }
        }
        when (result) {
            is ThumbnailResult.Ready -> println(
                "Thumbnail saved: fileId=${current.id}, saveMode=thumbnailPathDataUrl, key=inline-data-url, writtenBack=true, length=${result.thumbnailPath.length}"
            )
            is ThumbnailResult.Failed -> println("Thumbnail save skipped after failure: fileId=${current.id}")
            is ThumbnailResult.Unsupported -> println("Thumbnail save skipped as unsupported: fileId=${current.id}")
        }
        snapshotVersion++
    }

    private fun guessSourceKind(source: String): FileSourceKind = when {
        source.startsWith("http://", ignoreCase = true) -> FileSourceKind.Url
        source.startsWith("https://", ignoreCase = true) -> FileSourceKind.Url
        source.startsWith("browser-handle:", ignoreCase = true) -> FileSourceKind.BrowserHandle
        source.contains("://") -> FileSourceKind.RemoteReference
        else -> FileSourceKind.ManualPath
    }

    /**
     * 生成推荐候选文件列表。
     *
     * 正式规则要求文件上传满 24 小时后才能成为推荐候选。
     * 本地 UI 验收可以通过集中式调试开关仅绕过这段等待时间，
     * 推荐排序、理由、打开记录和其他候选规则仍由推荐引擎正常处理。
     */
    private fun recommendationCandidates(): List<FileReference> {
        val now = nowMillis()
        return repository.references.filter { reference ->
            isEligibleForRecommendation(reference, now)
        }
    }
}

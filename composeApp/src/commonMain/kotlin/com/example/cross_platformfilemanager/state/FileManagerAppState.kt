package com.example.cross_platformfilemanager

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

//应用状态层，负责文件管理器的状态组织。
class FileManagerAppState(
    private val repository: InMemoryFileRepository = InMemoryFileRepository(),
    private val recommendationEngine: RecommendationEngine = RecommendationEngine(),
    private val browserReferenceResolver: BrowserReferenceResolver? = null,
) : RecommendationReadOnlyState {
    private val defaultDraftTitle = ""
    private val defaultDraftSource = ""
    private val defaultDraftType = ""
    private val defaultDraftFileSizeBytes: Long? = null
    private val defaultDraftCoverArtSource = ""
    private val defaultDraftTags = ""
    private val defaultDraftNotes = ""

    var locale by mutableStateOf(AppLocale.ZhCn)
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

    val allResults: String
        get() = strings.allResults

    val emptyResultsTitle: String
        get() = strings.emptyResultsTitle

    val emptyResultsBody: String
        get() = strings.emptyResultsBody

    val recommendations: List<Suggestion>
        get() = recommendationEngine.suggest(
            query = query,
            references = repository.references,
            recentSearches = repository.recentSearches,
            selectedTag = selectedTag,
        )

    val searchResults: List<SearchResult>
        get() {
            if (query.isBlank() && selectedTag == null && selectedFileType == null && !favoritesOnly) {
                return repository.search("", null, null, false)
            }
            return repository.search(query, selectedTag, selectedFileType, favoritesOnly)
        }

    val querySuggestions: List<Suggestion>
        get() = recommendationEngine.suggest(
            query = query,
            references = repository.references,
            recentSearches = repository.recentSearches,
            selectedTag = selectedTag,
        ).filter { it.kind != SuggestionKind.File }

    val fileSuggestions: List<Suggestion>
        get() = recommendationEngine.suggest(
            query = query,
            references = repository.references,
            recentSearches = repository.recentSearches,
            selectedTag = selectedTag,
        ).filter { it.kind == SuggestionKind.File }

    val topTags: List<String>
        get() = repository.topTags()

    val allTags: List<String>
        get() = repository.allTags()

    val tagSummaries: List<TagSummary>
        get() = repository.tagSummaries()

    val fileTypeSummaries: List<FileTypeSummary>
        get() = repository.fileTypeSummaries()

    val recentReferences: List<FileReference>
        get() = repository.recentReferences()

    val allReferences: List<FileReference>
        get() = repository.references

    override val recommendedReferences: List<FileReference>
        get() = recommendationEngine.recommend(
            references = repository.references,
            previousFileId = activeReferenceId,
            nowMillis = nowMillis(),
            limit = 10,
        ).map { it.file }

    override val scoredRecommendedReferences: List<ScoredRecommendation>
        get() = recommendationEngine.recommend(
            references = repository.references,
            previousFileId = activeReferenceId,
            nowMillis = nowMillis(),
            limit = 10,
        )

    val recentAddedReferences: List<FileReference>
        get() = repository.references
            .sortedByDescending { it.createdAtMillis }
            .take(5)

    val activeReference: FileReference?
        get() = repository.references.firstOrNull { it.id == activeReferenceId }

    fun dashboardStats(): DashboardStats = repository.stats()

    fun exportSnapshot(): AppSnapshot = AppSnapshot(
        locale = locale,
        query = query,
        selectedTag = selectedTag,
        selectedFileType = selectedFileType,
        favoritesOnly = favoritesOnly,
        activeReferenceId = activeReferenceId,
        references = repository.references.toList(),
        recentSearches = repository.recentSearches.toList(),
        recommendationLogs = repository.recommendationLogs.toList(),
        recommendationState = recommendationEngine.exportSnapshot(),
    )

    fun restoreSnapshot(snapshot: AppSnapshot) {
        locale = snapshot.locale
        query = snapshot.query
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

    fun mergeSnapshot(snapshot: AppSnapshot) {
        snapshot.references.forEach { reference ->
            repository.upsertReference(reference)
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
        locale = when (locale) {
            AppLocale.ZhCn -> AppLocale.EnUs
            AppLocale.EnUs -> AppLocale.ZhCn
        }
        snapshotVersion++
    }

    fun clearLocalData() {
        repository.clearAllData()
        recommendationEngine.clear()
        resetWorkspaceFields()
        activeReferenceId = null
        snapshotVersion++
    }

    private fun resetWorkspaceFields() {
        locale = AppLocale.ZhCn
        query = ""
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

        val previousReferenceId = activeReferenceId
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
        recommendationEngine.recordFileOpen(
            fileId = saved.id,
            openedAtMillis = saved.lastOpenedAtMillis,
            previousFileId = previousReferenceId,
        )
        query = saved.title
        selectedTag = saved.tags.firstOrNull()
        commitSearch()
        snapshotVersion++
        return saved
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

    fun commitSearch() {
        repository.recordSearch(query)
        repository.recordRecommendation(query, selectedTag, recommendations)
        snapshotVersion++
    }

    fun toggleTagFilter(tag: String) {
        val normalizedTag = tag.trim()
        val isDeselecting = selectedTag == normalizedTag
        selectedTag = if (isDeselecting) null else normalizedTag
        query = if (isDeselecting) "" else normalizedTag
        repository.recordSearch(query)
        repository.recordRecommendation(query, selectedTag, recommendations)
        snapshotVersion++ 
    }

    fun toggleFileTypeFilter(fileType: String?) {
        val normalized = fileType?.trim()?.takeIf { it.isNotBlank() }
        selectedFileType = if (normalized == null) {
            null
        } else if (selectedFileType == normalized) {
            null
        } else {
            normalized
        }
        snapshotVersion++
    }

    fun toggleFavoritesOnly() {
        favoritesOnly = !favoritesOnly
        snapshotVersion++
    }

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
        // 这里已经在 openReference() 里记过一次“打开”了，刷新元数据只更新详情，不再重复写入推荐学习信号。
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

    private fun guessSourceKind(source: String): FileSourceKind = when {
        source.startsWith("http://", ignoreCase = true) -> FileSourceKind.Url
        source.startsWith("https://", ignoreCase = true) -> FileSourceKind.Url
        source.startsWith("browser-handle:", ignoreCase = true) -> FileSourceKind.BrowserHandle
        source.contains("://") -> FileSourceKind.RemoteReference
        else -> FileSourceKind.ManualPath
    }
}

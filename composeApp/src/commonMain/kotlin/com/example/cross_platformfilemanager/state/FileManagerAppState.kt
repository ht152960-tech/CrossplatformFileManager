package com.example.cross_platformfilemanager

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

//应用状态层，负责文件管理器的状态组织。
class FileManagerAppState(
    private val repository: InMemoryFileRepository = InMemoryFileRepository(),
    private val recommendationEngine: RecommendationEngine = RecommendationEngine(),
    private val browserReferenceResolver: BrowserReferenceResolver? = null,
) {
    private val defaultDraftTitle = "New reference"
    private val defaultDraftSource = "/local/path/example.pdf"
    private val defaultDraftType = "PDF"
    private val defaultDraftTags = "tag1, tag2"
    private val defaultDraftNotes = "Store only the reference and custom tags."

    var locale by mutableStateOf(AppLocale.ZhCn)
    var query by mutableStateOf("")
    var selectedTag by mutableStateOf<String?>(null)
    var selectedFileType by mutableStateOf<String?>(null)
    var favoritesOnly by mutableStateOf(false)

    var draftTitle by mutableStateOf(defaultDraftTitle)
    var draftSource by mutableStateOf(defaultDraftSource)
    var draftType by mutableStateOf(defaultDraftType)
    var draftTags by mutableStateOf(defaultDraftTags)
    var draftNotes by mutableStateOf(defaultDraftNotes)

    var activeReferenceId by mutableStateOf(repository.references.firstOrNull()?.id)
    var snapshotVersion by mutableStateOf(0)

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

    val tagSummaries: List<TagSummary>
        get() = repository.tagSummaries()

    val fileTypeSummaries: List<FileTypeSummary>
        get() = repository.fileTypeSummaries()

    val recentReferences: List<FileReference>
        get() = repository.recentReferences()

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
    )

    fun restoreSnapshot(snapshot: AppSnapshot) {
        locale = snapshot.locale
        query = snapshot.query
        selectedTag = snapshot.selectedTag
        selectedFileType = snapshot.selectedFileType
        favoritesOnly = snapshot.favoritesOnly
        activeReferenceId = snapshot.activeReferenceId
        repository.replaceReferences(snapshot.references)
        repository.replaceRecentSearches(snapshot.recentSearches)
        repository.replaceRecommendationLogs(snapshot.recommendationLogs)
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
        activeReferenceId = activeReferenceId ?: snapshot.activeReferenceId ?: repository.references.firstOrNull()?.id
        snapshotVersion++
    }

    fun resetWorkspace() {
        resetWorkspaceFields()
        activeReferenceId = repository.references.firstOrNull()?.id
        snapshotVersion++
    }

    fun clearLocalData() {
        repository.clearAllData()
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
        draftTags = defaultDraftTags
        draftNotes = defaultDraftNotes
    }

    fun addDraftReference() {
        val id = "ref-${nowMillis()}"
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
                notes = draftNotes,
            ).toReference(
                id = id,
                sourceKind = guessSourceKind(source),
                tags = tags,
                createdAtMillis = nowMillis(),
                lastOpenedAtMillis = nowMillis(),
            )
        )

        activeReferenceId = saved.id
        query = saved.title
        selectedTag = saved.tags.firstOrNull()
        commitSearch()
        snapshotVersion++
    }

    fun applyBrowserDraft(draft: BrowserReferenceDraft) {
        val normalized = draft.normalized()
        draftTitle = normalized.title.ifBlank { "Untitled file" }
        draftSource = normalized.source.ifBlank { "browser-handle:unknown" }
        draftType = normalized.fileType.ifBlank { "FILE" }
        draftNotes = normalized.notes.ifBlank { "Selected from browser file picker." }
        snapshotVersion++
    }

    fun commitSearch() {
        repository.recordSearch(query)
        repository.recordRecommendation(query, selectedTag, recommendations)
        snapshotVersion++
    }

    fun toggleTagFilter(tag: String) {
        selectedTag = if (selectedTag == tag) null else tag
        if (query.isBlank()) {
            query = tag
        }
        repository.recordSearch(query.ifBlank { tag })
        repository.recordRecommendation(query.ifBlank { tag }, selectedTag, recommendations)
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
        repository.open(referenceId)
        activeReferenceId = referenceId
        snapshotVersion++
    }

    suspend fun refreshReference(referenceId: String) {
        val current = repository.references.firstOrNull { it.id == referenceId } ?: return
        if (current.sourceKind != FileSourceKind.BrowserHandle) {
            openReference(referenceId)
            return
        }

        val resolved = browserReferenceResolver?.resolveReference(current) ?: run {
            openReference(referenceId)
            return
        }

        repository.updateReference(referenceId) { existing ->
            existing.copy(
                title = resolved.title.trim().ifBlank { existing.title },
                source = resolved.source.trim().ifBlank { existing.source },
                sourceKind = FileSourceKind.BrowserHandle,
                fileType = resolved.fileType.trim().ifBlank { existing.fileType },
                notes = resolved.notes.trim().ifBlank { existing.notes },
                lastOpenedAtMillis = nowMillis(),
            )
        }
        activeReferenceId = referenceId
        snapshotVersion++
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

    private fun guessSourceKind(source: String): FileSourceKind = when {
        source.startsWith("http://", ignoreCase = true) -> FileSourceKind.Url
        source.startsWith("https://", ignoreCase = true) -> FileSourceKind.Url
        source.startsWith("browser-handle:", ignoreCase = true) -> FileSourceKind.BrowserHandle
        source.contains("://") -> FileSourceKind.RemoteReference
        else -> FileSourceKind.ManualPath
    }
}

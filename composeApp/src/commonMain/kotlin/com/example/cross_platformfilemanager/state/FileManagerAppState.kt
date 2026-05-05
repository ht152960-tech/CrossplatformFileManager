package com.example.cross_platformfilemanager

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class FileManagerAppState(
    private val repository: InMemoryFileRepository = InMemoryFileRepository(),
    private val recommendationEngine: RecommendationEngine = RecommendationEngine(),
) {
    var locale by mutableStateOf(AppLocale.ZhCn)
    var query by mutableStateOf("")
    var selectedTag by mutableStateOf<String?>(null)

    var draftTitle by mutableStateOf("New reference")
    var draftSource by mutableStateOf("/local/path/example.pdf")
    var draftType by mutableStateOf("PDF")
    var draftTags by mutableStateOf("tag1, tag2")
    var draftNotes by mutableStateOf("Store only the reference and custom tags.")

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
            if (query.isBlank() && selectedTag == null) {
                return repository.search("", null)
            }
            return repository.search(query, selectedTag)
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

    val recentReferences: List<FileReference>
        get() = repository.recentReferences()

    val activeReference: FileReference?
        get() = repository.references.firstOrNull { it.id == activeReferenceId }

    fun dashboardStats(): DashboardStats = repository.stats()

    fun exportSnapshot(): AppSnapshot = AppSnapshot(
        locale = locale,
        query = query,
        selectedTag = selectedTag,
        activeReferenceId = activeReferenceId,
        references = repository.references.toList(),
        recentSearches = repository.recentSearches.toList(),
        recommendationLogs = repository.recommendationLogs.toList(),
    )

    fun restoreSnapshot(snapshot: AppSnapshot) {
        locale = snapshot.locale
        query = snapshot.query
        selectedTag = snapshot.selectedTag
        activeReferenceId = snapshot.activeReferenceId
        repository.replaceReferences(snapshot.references)
        repository.replaceRecentSearches(snapshot.recentSearches)
        repository.replaceRecommendationLogs(snapshot.recommendationLogs)
        snapshotVersion++
    }

    fun addDraftReference() {
        val id = "ref-${nowMillis()}"
        val normalizedTags = draftTags
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

        val title = draftTitle.trim().ifBlank { "Untitled reference" }
        val source = draftSource.trim().ifBlank { "/local/path/$id" }
        val type = draftType.trim().ifBlank { "FILE" }
        val notes = draftNotes.trim()

        repository.addReference(
            FileReference(
                id = id,
                title = title,
                source = source,
                sourceKind = guessSourceKind(source),
                fileType = type,
                tags = normalizedTags,
                notes = notes,
                createdAtMillis = nowMillis(),
                lastOpenedAtMillis = nowMillis(),
            )
        )

        activeReferenceId = id
        query = title
        selectedTag = normalizedTags.firstOrNull()
        commitSearch()
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

    fun openReference(referenceId: String) {
        repository.open(referenceId)
        activeReferenceId = referenceId
        snapshotVersion++
    }

    fun toggleFavorite(referenceId: String) {
        repository.toggleFavorite(referenceId)
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


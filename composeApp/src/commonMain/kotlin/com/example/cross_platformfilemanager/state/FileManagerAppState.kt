package com.example.cross_platformfilemanager

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.cross_platformfilemanager.data.adapter.TaggoFileImportInput
import com.example.cross_platformfilemanager.domain.recommendation.RecommendationMode
import com.example.cross_platformfilemanager.domain.recommendation.RecommendationRequest
import com.example.cross_platformfilemanager.domain.recommendation.RecommendationRequestContext
import com.example.cross_platformfilemanager.domain.recommendation.RankedHomeRecommendation
import com.example.cross_platformfilemanager.domain.recommendation.TaggoRecommendationService
import com.example.cross_platformfilemanager.runtime.TaggoBehaviorRuntime
import com.example.cross_platformfilemanager.runtime.TaggoFileRuntimeStore
import com.example.cross_platformfilemanager.runtime.RecommendationSnapshotInput
import com.example.cross_platformfilemanager.runtime.TaggoRecommendationRuntime
import com.example.cross_platformfilemanager.runtime.TaggoRuntimeFile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class HomeRecommendationFeedbackBinding(
    val recommendationSetId: String,
    val mode: RecommendationMode,
    val selected: RecommendationSnapshotInput,
    val candidates: List<RecommendationSnapshotInput>,
)

private data class HomeRecommendationBatch(
    val ranked: List<RankedHomeRecommendation> = emptyList(),
    val recommendationSetId: String? = null,
    val candidates: List<RecommendationSnapshotInput> = emptyList(),
    val request: RecommendationRequest? = null,
)
class FileManagerAppState(
    val runtimeStore: TaggoFileRuntimeStore = TaggoFileRuntimeStore(),
    private val searchSuggestionEngine: SearchSuggestionEngine = SearchSuggestionEngine(),
    private val browserReferenceResolver: BrowserReferenceResolver? = null,
    private val thumbnailGenerator: ThumbnailGenerator? = null,
    private val behaviorRuntime: TaggoBehaviorRuntime? = null,
    private val recommendationRuntime: TaggoRecommendationRuntime? = null,
    private val recommendationService: TaggoRecommendationService? = null,
) : RecommendationReadOnlyState {
    val searchTags = androidx.compose.runtime.mutableStateListOf<SearchTag>()
    val startupDefaultLocale = AppLocale.ZhCn
    private val stateScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val generatingThumbnailIds = mutableSetOf<String>()
    private var lastViewedDetailId: String? = null
    private val recommendationRequestContext = RecommendationRequestContext()
    private val recommendationRefreshMutex = Mutex()
    private var homeRecommendationBatch by mutableStateOf(HomeRecommendationBatch())
    var isHomeRecommendationRefreshing by mutableStateOf(false)
        private set
    var hasLoadedHomeRecommendations by mutableStateOf(false)
        private set

    var preferredLocale by mutableStateOf(startupDefaultLocale)
    var locale by mutableStateOf(startupDefaultLocale)
    var query by mutableStateOf("")
    var selectedTag by mutableStateOf<String?>(null)
    var selectedFileType by mutableStateOf<String?>(null)
    var draftTitle by mutableStateOf("")
    var draftSource by mutableStateOf("")
    var draftType by mutableStateOf("")
    var draftFileSizeBytes by mutableStateOf<Long?>(null)
    var draftTags by mutableStateOf("")
    var activeReferenceId by mutableStateOf<String?>(null)
    var snapshotVersion by mutableStateOf(0)

    private val strings: UiStrings get() = AppStrings.forLocale(locale)
    val subtitle: String get() = strings.subtitle
    val appName: String get() = strings.appName
    val allFilesTab: String get() = strings.allFilesTab
    val detailPanelTitle: String get() = strings.detailPanelTitle
    val recentlyAdded: String get() = strings.recentlyAdded
    val recommendationTitle: String get() = strings.recommendationTitle
    val recommendationSubtitle: String get() = strings.recommendationSubtitle
    val noRecommendations: String get() = strings.noRecommendations
    val searchTitle: String get() = strings.searchTitle
    val searchPlaceholder: String get() = strings.searchPlaceholder
    val referenceTitle: String get() = strings.referenceTitle
    val referenceLocation: String get() = strings.referenceLocation
    val fileType: String get() = strings.fileType
    val tagsCommaSeparated: String get() = strings.tagsCommaSeparated
    val tagEditorTitle: String get() = strings.tagEditorTitle
    val tagLibraryTitle: String get() = strings.tagLibraryTitle
    val tagLibrarySubtitle: String get() = strings.tagLibrarySubtitle
    val tagLibraryEmpty: String get() = strings.tagLibraryEmpty
    val addTag: String get() = strings.addTag
    val removeTag: String get() = strings.removeTag
    val createdAtLabel: String get() = strings.createdAtLabel
    val lastOpenedAtLabel: String get() = strings.lastOpenedAtLabel
    val save: String get() = strings.save
    val open: String get() = strings.open
    val delete: String get() = strings.delete
    val cancel: String get() = strings.cancel
    val addReference: String get() = strings.addReference
    val emptyResultsTitle: String get() = strings.emptyResultsTitle
    val emptyResultsBody: String get() = strings.emptyResultsBody

    val recommendations: List<Suggestion>
        get() = searchSuggestionEngine.suggest(
            query = query,
            references = runtimeStore.files.toList(),
            recentSearches = runtimeStore.recentSearches.toList(),
            selectedTag = selectedTag,
        )

    val searchResults: List<SearchResult>
        get() = searchRuntimeFiles(runtimeStore.files.toList(), searchTags)

    val topTags: List<String> get() = allTags.take(10)
    val allTags: List<String>
        get() = runtimeStore.files.toList()
            .flatMap { it.tags }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .map { it.key }
    val recentSearches: List<String> get() = runtimeStore.recentSearches.toList()
    val recentReferences: List<TaggoRuntimeFile>
        get() = runtimeStore.files.toList().sortedByDescending { it.lastContentOpenedAtMs ?: 0L }.take(5)
    val allReferences: List<TaggoRuntimeFile> get() = runtimeStore.files.toList()

    override val recommendedReferences: List<FileReference>
        get() = homeRecommendationBatch.ranked.map { it.file }

    override val scoredRecommendedReferences: List<ScoredRecommendation>
        get() = homeRecommendationBatch.ranked.map { ranked ->
            ScoredRecommendation(
                file = ranked.file,
                intervalScore = ranked.result.scoreParts.periodicScore,
                transitionScore = ranked.result.scoreParts.sequencePathScore + ranked.result.scoreParts.successorScore,
                recencyScore = ranked.result.scoreParts.recencyScore,
                finalScore = ranked.result.finalScore,
            )
        }

    val homeRecommendationBindings: Map<String, HomeRecommendationFeedbackBinding>
        get() {
            val batch = homeRecommendationBatch
            val setId = batch.recommendationSetId ?: return emptyMap()
            return batch.candidates.associate { candidate ->
                candidate.fileId to HomeRecommendationFeedbackBinding(
                    recommendationSetId = setId,
                    mode = batch.request?.mode ?: RecommendationMode.HOME_INITIAL,
                    selected = candidate,
                    candidates = batch.candidates,
                )
            }
        }

    val showHomeRecommendationSkeleton: Boolean
        get() = !hasLoadedHomeRecommendations && runtimeStore.files.isNotEmpty()

    val recentAddedReferences: List<TaggoRuntimeFile>
        get() {
            val now = nowMillis()
            return runtimeStore.files
                .filter { shouldShowInNewUploadList(it, now) }
                .sortedByDescending { it.createdAtMs }
                .take(5)
        }
    val activeReference: TaggoRuntimeFile?
        get() = activeReferenceId?.let(runtimeStore::getFile)

    suspend fun loadRuntime() {
        runtimeStore.load()
        if (activeReferenceId == null || runtimeStore.getFile(activeReferenceId.orEmpty()) == null) {
            activeReferenceId = runtimeStore.files.toList().firstOrNull()?.id
        }
        snapshotVersion++
        refreshHomeRecommendations()
    }

    fun refreshHomeRecommendations() {
        if (recommendationService == null) return
        stateScope.launch { refreshHomeRecommendationsNow() }
    }

    private suspend fun refreshHomeRecommendationsNow() {
        val service = recommendationService ?: return
        recommendationRefreshMutex.withLock {
            isHomeRecommendationRefreshing = true
            try {
                val filesSnapshot = runtimeStore.files.toList()
                if (filesSnapshot.isEmpty()) {
                    homeRecommendationBatch = HomeRecommendationBatch()
                    hasLoadedHomeRecommendations = true
                    return
                }
                val now = nowMillis()
                val request = recommendationRequestContext.createRequest(
                    nowMs = now,
                    limit = MAX_HOME_RECOMMENDATIONS,
                    sessionId = behaviorRuntime?.sessionId,
                )
                val computation = withContext(Dispatchers.Default) {
                    service.recommend(filesSnapshot, request)
                }
                val candidates = computation.recommendations.mapIndexed { index, ranked ->
                    RecommendationSnapshotInput(
                        fileId = ranked.file.id,
                        rank = index + 1,
                        score = ranked.result.finalScore,
                        reasonsJson = ranked.result.reasons.toReasonsJson(),
                        featuresJson = ranked.result.featuresJson,
                    )
                }
                val recorded = withContext(Dispatchers.Default) {
                    recommendationRuntime?.recordRecommendationSet(
                        surface = "home_recommendations",
                        trigger = if (request.mode == RecommendationMode.AFTER_OPEN) {
                            "after_open_refresh"
                        } else {
                            "home_refresh"
                        },
                        candidates = candidates,
                        policyName = computation.policy.policyName,
                        policyVersion = computation.policy.updateCount.toString(),
                        mode = request.mode,
                        sessionId = request.sessionId,
                        triggerFileId = request.triggerFileId,
                    )
                }
                homeRecommendationBatch = HomeRecommendationBatch(
                    ranked = computation.recommendations,
                    recommendationSetId = recorded?.setId,
                    candidates = candidates,
                    request = computation.request,
                )
                hasLoadedHomeRecommendations = true
            } finally {
                isHomeRecommendationRefreshing = false
            }
        }
    }

    suspend fun recordRecommendationOpenResult(
        binding: HomeRecommendationFeedbackBinding?,
        openedSuccessfully: Boolean,
    ) {
        val snapshot = binding ?: return
        recommendationRuntime?.recordSelectedFromRecommendation(
            recommendationSetId = snapshot.recommendationSetId,
            selectedFileId = snapshot.selected.fileId,
            selectedRank = snapshot.selected.rank,
            openedSuccessfully = openedSuccessfully,
            candidates = snapshot.candidates,
        )
        if (!openedSuccessfully) return
        val skippedFeatures = snapshot.candidates
            .filter { it.rank < snapshot.selected.rank }
            .mapNotNull { it.featuresJson }
        recommendationService?.updatePolicyFromFeedback(
            mode = snapshot.mode,
            selectedFeaturesJson = snapshot.selected.featuresJson,
            skippedBeforeFeaturesJson = skippedFeatures,
        )
        refreshHomeRecommendations()
    }
    fun currentRecommendationRequest(
        nowMs: Long = nowMillis(),
        limit: Int = 10,
    ): RecommendationRequest = recommendationRequestContext.createRequest(
        nowMs = nowMs,
        limit = limit,
        sessionId = behaviorRuntime?.sessionId,
    )

    fun exportSnapshot(): AppSnapshot = AppSnapshot(
        locale = preferredLocale,
        query = query,
        searchTags = searchTags.toList(),
        selectedTag = selectedTag,
        selectedFileType = selectedFileType,
        activeReferenceId = activeReferenceId,
    )

    fun restoreSnapshot(snapshot: AppSnapshot) {
        preferredLocale = snapshot.locale
        locale = preferredLocale
        query = snapshot.query
        searchTags.replaceWith(snapshot.searchTags)
        selectedTag = snapshot.selectedTag
        selectedFileType = snapshot.selectedFileType
        activeReferenceId = snapshot.activeReferenceId
        snapshotVersion++
    }

    fun mergeSnapshot(snapshot: AppSnapshot) = restoreSnapshot(snapshot)

    fun resetWorkspace() {
        resetWorkspaceFields()
        activeReferenceId = runtimeStore.files.toList().firstOrNull()?.id
        snapshotVersion++
    }

    fun toggleLocale() {
        locale = if (locale == AppLocale.ZhCn) AppLocale.EnUs else AppLocale.ZhCn
        preferredLocale = locale
        snapshotVersion++
    }

    fun clearLocalData() {
        homeRecommendationBatch = HomeRecommendationBatch()
        hasLoadedHomeRecommendations = false
        resetWorkspaceFields()
        activeReferenceId = runtimeStore.files.toList().firstOrNull()?.id
        snapshotVersion++
    }

    private fun resetWorkspaceFields() {
        preferredLocale = startupDefaultLocale
        locale = startupDefaultLocale
        query = ""
        searchTags.clear()
        selectedTag = null
        selectedFileType = null
        clearDraft()
    }

    private fun clearDraft() {
        draftTitle = ""
        draftSource = ""
        draftType = ""
        draftFileSizeBytes = null
        draftTags = ""
    }

    fun createDraftReferenceId(): String = "file-${nowMillis()}"

    fun createDraftFileImportInput(referenceId: String): TaggoFileImportInput {
        val displayName = draftTitle.trim().ifBlank { "Untitled file" }
        return TaggoFileImportInput(
            oldId = referenceId,
            displayName = displayName,
            extension = displayName.substringAfterLast('.', "").takeIf { it.isNotBlank() },
            mimeType = null,
            referenceValue = draftSource.trim().ifBlank { "/local/path/$referenceId" },
            sizeBytes = draftFileSizeBytes,
            tags = parseTags(draftTags),
        )
    }

    suspend fun addDraftReference(referenceId: String = createDraftReferenceId()): TaggoRuntimeFile {
        val saved = runtimeStore.addFile(createDraftFileImportInput(referenceId))
        activeReferenceId = saved.id
        snapshotVersion++
        refreshHomeRecommendations()
        return saved
    }

    suspend fun replaceReference(referenceId: String, draft: BrowserReferenceDraft): TaggoRuntimeFile? {
        val current = runtimeStore.getFile(referenceId) ?: return null
        val normalized = draft.normalized()
        runtimeStore.updateFile(
            current.copy(
                displayName = normalized.title.ifBlank { current.displayName },
                extension = normalized.title.substringAfterLast('.', "").takeIf { it.isNotBlank() }
                    ?: current.extension,
                taggoFileCategory = normalized.fileType.ifBlank { current.taggoFileCategory },
                sizeBytes = normalized.fileSizeBytes ?: current.sizeBytes,
            ),
        )
        activeReferenceId = referenceId
        snapshotVersion++
        return runtimeStore.getFile(referenceId)
    }

    fun applyBrowserDraft(draft: BrowserReferenceDraft) {
        val normalized = draft.normalized()
        draftTitle = normalized.title.ifBlank { "Untitled file" }
        draftSource = normalized.source.ifBlank { "browser-handle:unknown" }
        draftType = normalized.fileType.ifBlank { "FILE" }
        draftFileSizeBytes = normalized.fileSizeBytes
        snapshotVersion++
    }

    fun submitSearch(rawInput: String): Boolean {
        val trimmed = rawInput.trim()
        if (trimmed.isBlank()) return false
        val tokens = tokenizeSubmittedSearch(trimmed)
        if (tokens.isEmpty()) return false
        query = trimmed
        tokens.forEach { addSearchTag(SearchTag(it, SearchTagSource.Input)) }
        stateScope.launch { runtimeStore.recordSearch(trimmed) }
        snapshotVersion++
        return true
    }

    fun resetSearchSession() {
        query = ""
        searchTags.clear()
        snapshotVersion++
    }

    fun addSearchTag(tag: SearchTag): Boolean {
        val value = normalizeSearchTagToken(tag.value)
        if (value.isBlank() || searchTags.any { normalizeSearchTagToken(it.value) == value }) return false
        searchTags.add(SearchTag(value, tag.source))
        snapshotVersion++
        return true
    }

    fun removeSearchTag(tagValue: String) {
        val value = normalizeSearchTagToken(tagValue)
        if (searchTags.removeAll { normalizeSearchTagToken(it.value) == value }) {
            if (searchTags.isEmpty()) query = ""
            snapshotVersion++
        }
    }

    fun openReference(
        referenceId: String,
        screenName: String? = "detail",
        entryPoint: String? = null,
    ) {
        val target = runtimeStore.getFile(referenceId) ?: return
        recordViewDetail(referenceId, entryPoint = entryPoint, screenName = screenName)
        activeReferenceId = target.id
        snapshotVersion++
    }

    suspend fun recordContentOpen(
        referenceId: String,
        fileReferenceId: String? = null,
        entryPoint: String? = null,
        screenName: String? = null,
    ) {
        val target = runtimeStore.getFile(referenceId) ?: return
        val openedAt = nowMillis()
        runtimeStore.recordContentOpen(referenceId, openedAt)
        behaviorRuntime?.recordOpenContent(
            fileId = referenceId,
            fileReferenceId = fileReferenceId ?: target.primaryReferenceId,
            entryPoint = entryPoint,
            screenName = screenName,
        )
        recommendationRequestContext.recordOpenContent(
            fileId = referenceId,
            occurredAtMs = openedAt,
            sessionId = behaviorRuntime?.sessionId,
        )
        recommendationService?.updatePolicyFromManualSearchOpen(
            fileId = referenceId,
            files = runtimeStore.files.toList(),
            nowMs = openedAt,
        )
        activeReferenceId = target.id
        snapshotVersion++
        refreshHomeRecommendations()
    }

    fun recordViewDetail(
        referenceId: String,
        entryPoint: String? = null,
        screenName: String? = null,
    ) {
        if (lastViewedDetailId == referenceId) return
        lastViewedDetailId = referenceId
        stateScope.launch {
            behaviorRuntime?.recordViewDetail(
                fileId = referenceId,
                entryPoint = entryPoint,
                screenName = screenName ?: "detail",
            )
        }
    }

    fun recordOpenFailed(
        referenceId: String?,
        fileReferenceId: String? = null,
        errorMessage: String? = null,
        entryPoint: String? = null,
        screenName: String? = null,
    ) {
        stateScope.launch {
            behaviorRuntime?.recordOpenFailed(
                fileId = referenceId,
                fileReferenceId = fileReferenceId,
                errorMessage = errorMessage,
                entryPoint = entryPoint,
                screenName = screenName,
            )
        }
    }

    suspend fun refreshReference(referenceId: String) {
        val current = runtimeStore.getFile(referenceId) ?: return
        val resolved = browserReferenceResolver?.resolveReference(current) ?: return
        replaceReference(referenceId, resolved)
    }

    suspend fun refreshBrowserReferences(): Int {
        var refreshed = 0
        val filesSnapshot = runtimeStore.files.toList()
        filesSnapshot.filter { it.sourceKind == FileSourceKind.BrowserHandle }.forEach { file ->
            val resolved = browserReferenceResolver?.resolveReference(file) ?: return@forEach
            replaceReference(file.id, resolved)
            refreshed++
        }
        return refreshed
    }

    fun deleteReference(referenceId: String) {
        runtimeStore.getFile(referenceId)?.thumbnailReferenceValue?.let {
            thumbnailGenerator?.deleteThumbnail(it)
        }
        stateScope.launch {
            runtimeStore.softDeleteFile(referenceId)
            if (activeReferenceId == referenceId) {
                activeReferenceId = runtimeStore.files.toList().firstOrNull()?.id
            }
            snapshotVersion++
            refreshHomeRecommendations()
        }
    }

    fun updateReferenceTags(referenceId: String, tagsText: String) {
        val current = runtimeStore.getFile(referenceId) ?: return
        val desired = parseTags(tagsText)
        stateScope.launch {
            current.tags.filterNot { existing -> desired.any { normalize(it) == normalize(existing) } }
                .forEach { runtimeStore.removeTag(referenceId, it) }
            desired.filterNot { added -> current.tags.any { normalize(it) == normalize(added) } }
                .forEach { runtimeStore.addTag(referenceId, it) }
            snapshotVersion++
        }
    }

    fun deleteTagEverywhere(tag: String): Int {
        val filesSnapshot = runtimeStore.files.toList()
        val expected = filesSnapshot.count { file -> file.tags.any { normalize(it) == normalize(tag) } }
        stateScope.launch {
            runtimeStore.removeTagEverywhere(tag)
            snapshotVersion++
        }
        return expected
    }

    fun updateReferenceTitle(referenceId: String, title: String) {
        val current = runtimeStore.getFile(referenceId) ?: return
        stateScope.launch {
            runtimeStore.updateFile(current.copy(displayName = title.trim().ifBlank { current.displayName }))
            snapshotVersion++
        }
    }

    fun updateReferenceFileType(referenceId: String, fileType: String) {
        val current = runtimeStore.getFile(referenceId) ?: return
        stateScope.launch {
            runtimeStore.updateFile(
                current.copy(taggoFileCategory = fileType.trim().ifBlank { current.taggoFileCategory }),
            )
            snapshotVersion++
        }
    }

    fun generateThumbnailForReference(referenceId: String, force: Boolean = false) {
        stateScope.launch { runThumbnailGeneration(referenceId, force) }
    }

    private suspend fun runThumbnailGeneration(referenceId: String, force: Boolean) {
        val current = runtimeStore.getFile(referenceId) ?: return
        if (!current.needsThumbnailGeneration()) {
            runtimeStore.updateFile(
                current.copy(thumbnailState = "unsupported", thumbnailReferenceValue = null),
            )
            return
        }
        if (!force && current.thumbnailStatus == ThumbnailStatus.READY &&
            !current.thumbnailReferenceValue.isNullOrBlank()
        ) {
            return
        }
        if (!generatingThumbnailIds.add(referenceId)) return
        val result = try {
            runtimeStore.updateFile(current.copy(thumbnailState = "generating"))
            thumbnailGenerator?.generateThumbnail(current)
                ?: ThumbnailResult.Unsupported("thumbnail generator unavailable")
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            ThumbnailResult.Failed(error.message ?: "thumbnail generation failed")
        } finally {
            generatingThumbnailIds.remove(referenceId)
        }
        val latest = runtimeStore.getFile(referenceId) ?: current
        runtimeStore.updateFile(
            when (result) {
                is ThumbnailResult.Ready -> latest.copy(
                    thumbnailState = "ready",
                    thumbnailReferenceValue = result.thumbnailPath,
                )
                is ThumbnailResult.Failed -> latest.copy(
                    thumbnailState = "failed",
                    thumbnailReferenceValue = null,
                )
                is ThumbnailResult.Unsupported -> latest.copy(
                    thumbnailState = "unsupported",
                    thumbnailReferenceValue = null,
                )
            },
        )
        snapshotVersion++
    }

    private fun parseTags(value: String): List<String> =
        value.split(",").map { it.trim() }.filter { it.isNotBlank() }.distinct()

    private fun <T> MutableList<T>.replaceWith(values: List<T>) {
        clear()
        addAll(values)
    }
}

private fun searchRuntimeFiles(
    files: List<TaggoRuntimeFile>,
    searchTags: List<SearchTag>,
): List<SearchResult> {
    val tokens = searchTags.map { normalizeSearchTagToken(it.value) }.filter { it.isNotBlank() }.distinct()
    if (tokens.isEmpty()) return emptyList()
    return files.mapNotNull { file ->
        val fields = listOf(file.displayName, file.taggoFileCategory, file.referenceValue) + file.tags
        val matches = tokens.count { token ->
            fields.any { field -> normalize(field).contains(normalize(token)) }
        }
        if (matches == 0) null else SearchResult(
            reference = file,
            score = matches.toDouble(),
            reason = tokens.filter { token ->
                fields.any { field -> normalize(field).contains(normalize(token)) }
            }.joinToString(),
            matchedTagCount = matches,
        )
    }.sortedWith(
        compareByDescending<SearchResult> { it.matchedTagCount }
            .thenByDescending { it.reference.lastContentOpenedAtMs ?: 0L }
            .thenByDescending { it.reference.createdAtMs },
    )
}
private fun List<com.example.cross_platformfilemanager.domain.recommendation.RecommendationReason>.toReasonsJson(): String = buildJsonArray {
    forEach { reason ->
        add(buildJsonObject {
            put("code", reason.code)
            put("message", reason.message)
            reason.contribution?.let { put("contribution", it) }
            reason.confidence?.let { put("confidence", it) }
        })
    }
}.toString()

private const val MAX_HOME_RECOMMENDATIONS = 10

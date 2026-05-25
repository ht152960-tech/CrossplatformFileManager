package com.example.cross_platformfilemanager

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Icon
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import taggo.composeapp.generated.resources.TaggoLogoBig2048
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Slideshow
import androidx.compose.material.icons.outlined.TableChart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch

private enum class AppPage {
    Home,
    AllFiles,
    Detail,
    Search,
}

private enum class ReferenceEditorMode {
    Add,
    Replace,
}

private enum class FileSortMode {
    RecentOpened,
    RecentAdded,
    Name,
    FileSize,
}

private enum class SortDirection {
    Ascending,
    Descending,
}

private enum class AllFilesTypeFilter {
    All,
    Image,
    Video,
    Document,
    Spreadsheet,
    Presentation,
    Other,
}

@Composable
@Preview
fun App() {
    val snapshotStore = remember { createAppSnapshotStore() }
    val browserReferencePicker = remember { createBrowserReferencePicker() }
    val browserReferenceResolver = remember { createBrowserReferenceResolver() }
    val thumbnailGenerator = remember { createThumbnailGenerator() }
    val appState = remember(browserReferenceResolver, thumbnailGenerator) {
        FileManagerAppState(
            browserReferenceResolver = browserReferenceResolver,
            thumbnailGenerator = thumbnailGenerator,
        )
    }
    val coroutineScope = rememberCoroutineScope()
    val pageScrollState = rememberScrollState()
    val fontLoadState = rememberAppFontLoadState()
    val fullCjkFontLoadState = rememberFullCjkFontLoadState()
    val appStartMillis = remember { nowMillis() }

    var currentPage by remember { mutableStateOf(AppPage.Home) }
    var searchDraft by remember { mutableStateOf("") }
    var sortMode by remember { mutableStateOf(FileSortMode.RecentOpened) }
    var sortDirection by remember { mutableStateOf(defaultSortDirection(FileSortMode.RecentOpened)) }
    var allFilesTypeFilter by remember { mutableStateOf(AllFilesTypeFilter.All) }
    var showManualAddDialog by remember { mutableStateOf(false) }
    var manualAddNotice by remember { mutableStateOf<String?>(null) }
    var referenceEditorMode by remember { mutableStateOf(ReferenceEditorMode.Add) }
    var referenceEditorTargetId by remember { mutableStateOf<String?>(null) }
    var draftFileSizeText by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showSideMenu by remember { mutableStateOf(false) }
    var snapshotReady by remember { mutableStateOf(false) }
    var searchFeedbackMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        reportStartupTrace("App composition entered")
    }

    LaunchedEffect(snapshotStore, browserReferenceResolver) {
        val startedAt = nowMillis()
        reportStartupTrace("snapshot load start +${startedAt - appStartMillis}ms")
        try {
            snapshotStore?.load()?.let(appState::restoreSnapshot)
            if (browserReferenceResolver != null) {
                appState.refreshBrowserReferences()
            }
        } finally {
            snapshotReady = true
            reportStartupTrace("snapshot load end +${nowMillis() - appStartMillis}ms")
        }
    }

    LaunchedEffect(appState.snapshotVersion, snapshotStore, snapshotReady) {
        if (snapshotReady) {
            snapshotStore?.save(appState.exportSnapshot())
        }
    }

    fun openReference(reference: FileReference) {
        // If the same file is opened again from the detail page, do not count it twice.
        if (appState.activeReference?.id == reference.id) {
            currentPage = AppPage.Detail
            return
        }
        openReferenceWithRefresh(appState, coroutineScope, reference)
        currentPage = AppPage.Detail
    }

    fun startSearchFromHome() {
        appState.resetSearchSession()
        val validationMessage = searchValidationMessage(searchDraft, appState.locale)
        if (validationMessage != null) {
            searchFeedbackMessage = validationMessage
            currentPage = AppPage.Search
            return
        }
        if (appState.submitSearch(searchDraft)) {
            searchDraft = ""
            searchFeedbackMessage = null
        }
        currentPage = AppPage.Search
    }

    fun startSearchFromSearchPage() {
        val validationMessage = searchValidationMessage(searchDraft, appState.locale)
        if (validationMessage != null) {
            searchFeedbackMessage = validationMessage
            currentPage = AppPage.Search
            return
        }
        if (appState.submitSearch(searchDraft)) {
            searchDraft = ""
            searchFeedbackMessage = null
        }
        currentPage = AppPage.Search
    }

    fun addPickedReference(draft: BrowserReferenceDraft) {
        appState.applyBrowserDraft(draft)
        val saved = appState.addDraftReference()
        coroutineScope.launch {
            appState.generateThumbnailForReference(saved.id)
        }
        clearDraftFields(appState)
        draftFileSizeText = ""
        currentPage = AppPage.Home
    }

    fun showAddReferenceDialog(notice: String? = null) {
        clearDraftFields(appState)
        draftFileSizeText = ""
        referenceEditorMode = ReferenceEditorMode.Add
        referenceEditorTargetId = null
        manualAddNotice = notice
        showManualAddDialog = true
    }

    fun showReplaceReferenceDialog(reference: FileReference) {
        clearDraftFields(appState)
        appState.draftTitle = reference.title
        appState.draftSource = reference.source
        appState.draftType = reference.fileType
        appState.draftFileSizeBytes = reference.fileSizeBytes ?: guessFileSizeFromNotes(reference.notes)
        appState.draftCoverArtSource = reference.coverArtSource.orEmpty()
        appState.draftTags = reference.tags.joinToString(", ")
        appState.draftNotes = reference.notes
        draftFileSizeText = appState.draftFileSizeBytes?.toString().orEmpty()
        referenceEditorMode = ReferenceEditorMode.Replace
        referenceEditorTargetId = reference.id
        manualAddNotice = if (appState.locale == AppLocale.ZhCn) {
            "替换会保留当前标签和打开历史。"
        } else {
            "Replacement keeps the current tags and open history."
        }
        showManualAddDialog = true
    }

    fun currentDraftReference(): BrowserReferenceDraft = BrowserReferenceDraft(
        title = appState.draftTitle,
        source = appState.draftSource,
        fileType = appState.draftType,
        fileSizeBytes = appState.draftFileSizeBytes,
        coverArtSource = appState.draftCoverArtSource.ifBlank { null },
        notes = appState.draftNotes,
    )

    @Composable
    fun AppMainSurface() {
        val fullCjkFontReady = fullCjkFontLoadState.fullCjkFontReady
        val displayLocale = appState.locale
        val uiFontFamily = rememberAppFontFamily()
        val fullCjkFontFamily = rememberFullCjkFontFamily()
        val appTextStyle = LocalTextStyle.current.copy(fontFamily = uiFontFamily)
        val fullCjkTextStyle = LocalTextStyle.current.copy(fontFamily = fullCjkFontFamily)
        CompositionLocalProvider(LocalTextStyle provides appTextStyle) {
            MaterialTheme(
                colorScheme = appColorScheme(),
                typography = appTypography(uiFontFamily),
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(appBackgroundBrush()),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp)
                                .verticalScroll(pageScrollState),
                            verticalArrangement = Arrangement.spacedBy(18.dp),
                        ) {
                            when (currentPage) {
                                AppPage.Home -> HomePage(
                                    appState = appState,
                                    locale = displayLocale,
                                    fullCjkFontReady = fullCjkFontReady,
                                    fullCjkTextStyle = fullCjkTextStyle,
                                    fullCjkFontFamily = fullCjkFontFamily,
                                    searchDraft = searchDraft,
                                    onSearchDraftChange = { searchDraft = it },
                                    onOpenMenu = { showSideMenu = true },
                                    onPickFile = {
                                        if (browserReferencePicker == null) {
                                            showAddReferenceDialog(browserPickerUnavailableMessage(displayLocale))
                                        } else {
                                            coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
                                                runCatching { browserReferencePicker.pickReference() }
                                                    .onSuccess { draft ->
                                                        if (draft != null) {
                                                            addPickedReference(draft)
                                                        }
                                                    }
                                                    .onFailure { }
                                            }
                                        }
                                    },
                                    onOpenAllFiles = { currentPage = AppPage.AllFiles },
                                    onSearch = ::startSearchFromHome,
                                    onOpenReference = ::openReference,
                                )

                                AppPage.AllFiles -> AllFilesPage(
                                    appState = appState,
                                    locale = displayLocale,
                                    fullCjkFontReady = fullCjkFontReady,
                                    fullCjkFontFamily = fullCjkFontFamily,
                                    sortMode = sortMode,
                                    sortDirection = sortDirection,
                                    typeFilter = allFilesTypeFilter,
                                    onSortModeChange = { nextMode ->
                                        if (sortMode == nextMode) {
                                            sortDirection = sortDirection.toggled()
                                        } else {
                                            sortMode = nextMode
                                            sortDirection = defaultSortDirection(nextMode)
                                        }
                                    },
                                    onTypeFilterChange = { allFilesTypeFilter = it },
                                    onBackHome = { currentPage = AppPage.Home },
                                    onOpenReference = ::openReference,
                                )

                                AppPage.Detail -> DetailPage(
                                    appState = appState,
                                    browserReferencePicker = browserReferencePicker,
                                    browserReferenceResolver = browserReferenceResolver,
                                    locale = displayLocale,
                                    fullCjkFontReady = fullCjkFontReady,
                                    fullCjkFontFamily = fullCjkFontFamily,
                                    reference = appState.activeReference,
                                    onBackHome = { currentPage = AppPage.Home },
                                    onReplaceReference = { reference ->
                                        if (browserReferencePicker != null) {
                                            coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
                                                runCatching { browserReferencePicker.pickReference() }
                                                    .onSuccess { draft ->
                                                        if (draft != null) {
                                                            val replaced = appState.replaceReference(reference.id, draft)
                                                            if (replaced != null) {
                                                                appState.generateThumbnailForReference(replaced.id)
                                                            }
                                                            clearDraftFields(appState)
                                                            draftFileSizeText = ""
                                                        }
                                                    }
                                                    .onFailure { }
                                            }
                                        } else {
                                            showReplaceReferenceDialog(reference)
                                        }
                                    },
                                    onDeleteReference = { showDeleteConfirm = true },
                                )

                                AppPage.Search -> SearchResultsPage(
                                    appState = appState,
                                    locale = displayLocale,
                                    fullCjkFontReady = fullCjkFontReady,
                                    fullCjkTextStyle = fullCjkTextStyle,
                                    fullCjkFontFamily = fullCjkFontFamily,
                                    searchDraft = searchDraft,
                                    searchFeedbackMessage = searchFeedbackMessage,
                                    onSearchDraftChange = {
                                        searchDraft = it
                                        searchFeedbackMessage = null
                                    },
                                    onSearch = ::startSearchFromSearchPage,
                                    onBackHome = { currentPage = AppPage.Home },
                                    onOpenReference = ::openReference,
                                    onTagClick = { tag ->
                                        appState.addSearchTag(SearchTag(value = tag, source = SearchTagSource.LibraryTag))
                                        searchFeedbackMessage = null
                                        currentPage = AppPage.Search
                                    },
                                )
                            }
                        }

                        if (showSideMenu) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.14f))
                                    .clickable { showSideMenu = false },
                            )
                            AnimatedVisibility(
                                visible = showSideMenu,
                                enter = slideInHorizontally { -it },
                                exit = slideOutHorizontally { -it },
                            ) {
                                SideMenuPanel(
                                    appState = appState,
                                    locale = displayLocale,
                                    fullCjkFontReady = fullCjkFontReady,
                                    fullCjkFontFamily = fullCjkFontFamily,
                                    onClose = { showSideMenu = false },
                                    onToggleLanguage = appState::toggleLocale,
                                )
                            }
                        }

                        if (showManualAddDialog) {
                            ManualAddDialog(
                                appState = appState,
                                locale = displayLocale,
                                fullCjkFontReady = fullCjkFontReady,
                                fullCjkTextStyle = fullCjkTextStyle,
                                notice = manualAddNotice,
                                editorMode = referenceEditorMode,
                                draftFileSizeText = draftFileSizeText,
                                onDraftFileSizeTextChange = { text ->
                                    draftFileSizeText = text
                                    appState.draftFileSizeBytes = text.trim().toLongOrNull()
                                },
                                onDismiss = {
                                    manualAddNotice = null
                                    showManualAddDialog = false
                                    referenceEditorMode = ReferenceEditorMode.Add
                                    referenceEditorTargetId = null
                                    draftFileSizeText = ""
                                },
                                onConfirm = {
                                    when (referenceEditorMode) {
                                        ReferenceEditorMode.Add -> {
                                            val saved = appState.addDraftReference()
                                            coroutineScope.launch {
                                                appState.generateThumbnailForReference(saved.id)
                                            }
                                            currentPage = AppPage.Home
                                        }

                                        ReferenceEditorMode.Replace -> {
                                            referenceEditorTargetId?.let { referenceId ->
                                                val draft = currentDraftReference()
                                                coroutineScope.launch {
                                                    val replaced = appState.replaceReference(referenceId, draft)
                                                    if (replaced != null) {
                                                        appState.generateThumbnailForReference(replaced.id)
                                                    }
                                                }
                                            }
                                            currentPage = AppPage.Detail
                                        }
                                    }
                                    clearDraftFields(appState)
                                    draftFileSizeText = ""
                                    manualAddNotice = null
                                    showManualAddDialog = false
                                    referenceEditorMode = ReferenceEditorMode.Add
                                    referenceEditorTargetId = null
                                },
                            )
                        }

                        if (showDeleteConfirm && appState.activeReference != null) {
                            val reference = appState.activeReference
                            AlertDialog(
                                onDismissRequest = { showDeleteConfirm = false },
                                title = { Text(if (displayLocale == AppLocale.ZhCn) "\u5220\u9664\u6587\u4ef6" else "Delete file") },
                                text = { Text(displayTextForUi(reference?.title.orEmpty(), true), fontFamily = fullCjkFontFamily) },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            reference?.let { appState.deleteReference(it.id) }
                                            showDeleteConfirm = false
                                            currentPage = AppPage.Home
                                        },
                                    ) {
                                        Text(
                                            text = if (displayLocale == AppLocale.ZhCn) "\u5220\u9664" else "Delete",
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteConfirm = false }) {
                                        Text(if (displayLocale == AppLocale.ZhCn) "\u53d6\u6d88" else "Cancel")
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
    AppStartupGate(
        snapshotReady = snapshotReady,
        fontLoadState = fontLoadState,
        fullCjkFontLoadState = fullCjkFontLoadState,
    ) {
        AppMainSurface()
    }
}

@Composable
private fun HomePage(
    appState: FileManagerAppState,
    locale: AppLocale,
    fullCjkFontReady: Boolean,
    fullCjkTextStyle: androidx.compose.ui.text.TextStyle,
    fullCjkFontFamily: FontFamily,
    searchDraft: String,
    onSearchDraftChange: (String) -> Unit,
    onOpenMenu: () -> Unit,
    onPickFile: () -> Unit,
    onOpenAllFiles: () -> Unit,
    onSearch: () -> Unit,
    onOpenReference: (FileReference) -> Unit,
) {
    val recommendedReferences = resolveRecommendedReferences(appState)

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TopBarCard(
            locale = locale,
            onMenuClick = onOpenMenu,
            title = appState.appName,
            leading = {
                Image(
                    painter = painterResource(TaggoLogoBig2048),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(32.dp),
                )
                Spacer(modifier = Modifier.width(10.dp))
            },
            trailing = {
                SearchBarInline(
                    query = searchDraft,
                    onQueryChange = onSearchDraftChange,
                    onSearch = onSearch,
                    placeholder = appState.searchPlaceholder,
                    buttonLabel = if (locale == AppLocale.ZhCn) "\u641c\u7d22" else "Search",
                    textStyle = fullCjkTextStyle,
                )
            },
        )

        SectionCard(
            title = appState.recentlyAdded,
            subtitle = if (locale == AppLocale.ZhCn) {
                "\u70b9\u51fb\u4efb\u610f\u6761\u76ee\u8fdb\u5165\u8be6\u60c5\u9875\u3002"
            } else {
                "Open any item to view its details."
            },
            trailing = {
                Button(onClick = onPickFile) {
                    Text(if (locale == AppLocale.ZhCn) "\u4e0a\u4f20\u6587\u4ef6" else "Upload file")
                }
            },
        ) {
            if (appState.recentAddedReferences.isEmpty()) {
                EmptyPanel(
                    title = if (locale == AppLocale.ZhCn) "\u8fd8\u6ca1\u6709\u65b0\u6587\u4ef6" else "No recent files yet",
                    body = if (locale == AppLocale.ZhCn) {
                        "\u9009\u62e9\u4e00\u4e2a\u6587\u4ef6\u540e\uff0c\u5b83\u4f1a\u51fa\u73b0\u5728\u8fd9\u91cc\u3002"
                    } else {
                        "Pick a file and it will appear here."
                    },
                )
            } else {
                AdaptiveFileGrid(
                    items = appState.recentAddedReferences,
                    fullCjkFontReady = fullCjkFontReady,
                    fullCjkFontFamily = fullCjkFontFamily,
                    onOpen = onOpenReference,
                )
            }
        }

        SectionCard(
            title = appState.recommendationTitle,
            subtitle = appState.recommendationSubtitle,
            trailing = {
                OutlinedButton(onClick = onOpenAllFiles) {
                    Text(if (locale == AppLocale.ZhCn) "\u5168\u90e8\u6587\u4ef6" else "All files")
                }
            },
        ) {
            if (recommendedReferences.isEmpty()) {
                EmptyPanel(
                    title = appState.noRecommendations,
                    body = if (locale == AppLocale.ZhCn) {
                        "\u7cfb\u7edf\u8fd8\u6ca1\u6709\u8db3\u591f\u7684\u6570\u636e\u751f\u6210\u63a8\u8350\u3002"
                    } else {
                        "The system has not collected enough signals yet."
                    },
                )
            } else {
                AdaptiveFileGrid(
                    items = recommendedReferences,
                    fullCjkFontReady = fullCjkFontReady,
                    fullCjkFontFamily = fullCjkFontFamily,
                    onOpen = onOpenReference,
                )
            }
        }
    }
}

@Composable
private fun AllFilesPage(
    appState: FileManagerAppState,
    locale: AppLocale,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    sortMode: FileSortMode,
    sortDirection: SortDirection,
    typeFilter: AllFilesTypeFilter,
    onSortModeChange: (FileSortMode) -> Unit,
    onTypeFilterChange: (AllFilesTypeFilter) -> Unit,
    onBackHome: () -> Unit,
    onOpenReference: (FileReference) -> Unit,
) {
    val availableTypeFilters = remember(appState.allReferences) {
        resolveAvailableTypeFilters(appState.allReferences)
    }
    val filteredFiles = remember(appState.allReferences, typeFilter) {
        filterReferencesByType(appState.allReferences, typeFilter)
    }
    val sortedFiles = remember(filteredFiles, sortMode, sortDirection) {
        sortReferences(filteredFiles, sortMode, sortDirection)
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TopBarCard(
            locale = locale,
            title = if (locale == AppLocale.ZhCn) "\u5168\u90e8\u6587\u4ef6" else "All files",
            onBack = onBackHome,
        )

        if (appState.allReferences.isEmpty()) {
            EmptyPanel(
                title = if (locale == AppLocale.ZhCn) "\u6682\u65e0\u6587\u4ef6" else "No files yet",
                body = if (locale == AppLocale.ZhCn) {
                    "\u5f53\u524d\u6587\u4ef6\u5217\u8868\u4e3a\u7a7a\u3002"
                } else {
                    "The file list is currently empty."
                },
            )
        } else {
            SectionCard(
                title = if (locale == AppLocale.ZhCn) "\u6392\u5e8f" else "Sort",
                subtitle = if (locale == AppLocale.ZhCn) {
                    "\u518d\u70b9\u5f53\u524d\u6309\u94ae\u53ef\u5207\u6362\u5347\u964d\u5e8f\uff0c\u5207\u5230\u5176\u4ed6\u6392\u5e8f\u65f6\u4f1a\u6062\u590d\u8be5\u6392\u5e8f\u7684\u9ed8\u8ba4\u65b9\u5411\u3002"
                } else {
                    "Tap the current option again to flip the direction. Switching to another sort resets it to that mode's default direction."
                },
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SortChip(
                        label = sortChipLabel(FileSortMode.RecentOpened, sortMode, sortDirection, locale),
                        selected = sortMode == FileSortMode.RecentOpened,
                        onClick = { onSortModeChange(FileSortMode.RecentOpened) },
                    )
                    SortChip(
                        label = sortChipLabel(FileSortMode.Name, sortMode, sortDirection, locale),
                        selected = sortMode == FileSortMode.Name,
                        onClick = { onSortModeChange(FileSortMode.Name) },
                    )
                    SortChip(
                        label = sortChipLabel(FileSortMode.RecentAdded, sortMode, sortDirection, locale),
                        selected = sortMode == FileSortMode.RecentAdded,
                        onClick = { onSortModeChange(FileSortMode.RecentAdded) },
                    )
                    SortChip(
                        label = sortChipLabel(FileSortMode.FileSize, sortMode, sortDirection, locale),
                        selected = sortMode == FileSortMode.FileSize,
                        onClick = { onSortModeChange(FileSortMode.FileSize) },
                    )
                }
            }

            SectionCard(
                title = if (locale == AppLocale.ZhCn) "\u7c7b\u578b" else "Type",
                subtitle = if (locale == AppLocale.ZhCn) {
                    "\u53ea\u663e\u793a\u5f53\u524d\u6587\u4ef6\u5e93\u91cc\u771f\u6b63\u5b58\u5728\u7684\u5927\u7c7b\uff0c\u4e0d\u91cd\u7f6e\u5f53\u524d\u6392\u5e8f\u3002"
                } else {
                    "Only categories that exist in the current library are shown, and switching type keeps the current sort."
                },
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    availableTypeFilters.forEach { filter ->
                        SortChip(
                            label = typeFilterLabel(filter, locale),
                            selected = typeFilter == filter,
                            onClick = { onTypeFilterChange(filter) },
                        )
                    }
                }
            }

            if (sortedFiles.isEmpty()) {
                EmptyPanel(
                    title = if (locale == AppLocale.ZhCn) "\u6682\u65e0\u8be5\u7c7b\u578b\u6587\u4ef6" else "No files in this type",
                    body = if (locale == AppLocale.ZhCn) {
                        "\u5207\u6362\u5230\u5176\u4ed6\u7c7b\u578b\uff0c\u6216\u8005\u4fdd\u6301\u201c\u5168\u90e8\u201d\u67e5\u770b\u6240\u6709\u6587\u4ef6\u3002"
                    } else {
                        "Switch to another type, or keep All selected to browse every file."
                    },
                )
            } else {
                AdaptiveFileGrid(
                    items = sortedFiles,
                    fullCjkFontReady = fullCjkFontReady,
                    fullCjkFontFamily = fullCjkFontFamily,
                    onOpen = onOpenReference,
                )
            }
        }
    }
}

@Composable
private fun SearchResultsPage(
    appState: FileManagerAppState,
    locale: AppLocale,
    fullCjkFontReady: Boolean,
    fullCjkTextStyle: androidx.compose.ui.text.TextStyle,
    fullCjkFontFamily: FontFamily,
    searchDraft: String,
    searchFeedbackMessage: String?,
    onSearchDraftChange: (String) -> Unit,
    onSearch: () -> Unit,
    onBackHome: () -> Unit,
    onOpenReference: (FileReference) -> Unit,
    onTagClick: (String) -> Unit,
) {
    val activeSearchTags = appState.searchTags.toList()
    val showSearchPrompt = activeSearchTags.isEmpty()
    val searchResults = if (showSearchPrompt) emptyList() else appState.searchResults
    val showTagLibrary = searchDraft.isBlank() && activeSearchTags.isEmpty()
    val resultStatus = if (activeSearchTags.isNotEmpty()) {
        val summary = activeSearchTags.joinToString(separator = " ") { "[${displayTextForUi(it.value, fullCjkFontReady)}]" }
        if (locale == AppLocale.ZhCn) {
            "$summary · ${searchResults.size} 个结果"
        } else {
            "$summary · ${searchResults.size} results"
        }
    } else {
        if (locale == AppLocale.ZhCn) {
            "输入关键词，或点击下方高频标签开始搜索"
        } else {
            "Type keywords or tap a frequent tag to start searching"
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TopBarCard(
            locale = locale,
            title = appState.searchTitle,
            onBack = onBackHome,
        )

        SearchBarCard(
            query = searchDraft,
            onQueryChange = onSearchDraftChange,
            onSearch = onSearch,
            placeholder = appState.searchPlaceholder,
            buttonLabel = if (locale == AppLocale.ZhCn) "\u641c\u7d22" else "Search",
            textStyle = fullCjkTextStyle,
            supportingText = searchFeedbackMessage,
        )

        if (activeSearchTags.isNotEmpty()) {
            ActiveSearchTagsSection(
                tags = activeSearchTags,
                locale = locale,
                fullCjkFontReady = fullCjkFontReady,
                fullCjkFontFamily = fullCjkFontFamily,
                onRemove = appState::removeSearchTag,
            )
        }

        if (showTagLibrary) {
            SearchFilterSection(
                title = if (locale == AppLocale.ZhCn) "高频标签" else "Frequent tags",
                subtitle = if (locale == AppLocale.ZhCn) "点击任意标签即可加入当前搜索" else "Tap a tag to add it to the current search",
                tags = appState.topTags,
                selectedTags = activeSearchTags.map { normalizeSearchTagToken(it.value) }.toSet(),
                emptyHint = appState.tagLibraryEmpty,
                fullCjkFontReady = fullCjkFontReady,
                fullCjkFontFamily = fullCjkFontFamily,
                onTagClick = onTagClick,
            )
        }

        Text(
            text = resultStatus,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 4.dp),
            fontFamily = fullCjkFontFamily,
        )

        if (showSearchPrompt) {
            SearchEmptyState(
                title = if (locale == AppLocale.ZhCn) "还没有搜索标签" else "No search tags yet",
                body = if (locale == AppLocale.ZhCn) {
                    "输入关键词，或点击下方高频标签开始搜索。"
                } else {
                    "Type keywords, or tap a frequent tag below to start searching."
                },
            )
        } else if (searchResults.isEmpty()) {
            SearchEmptyState(
                title = if (locale == AppLocale.ZhCn) "没有找到相关文件" else "No matching files found",
                body = if (locale == AppLocale.ZhCn) {
                    "可以删除部分搜索标签、换一个关键词。当前只搜索文件名、标签、路径和文件类型，不搜索正文内容。"
                } else {
                    "Try removing some tags or changing a keyword. Search only checks file names, tags, paths, and file types."
                },
            )
        } else {
            AdaptiveFileGrid(
                items = searchResults.map { it.reference },
                fullCjkFontReady = fullCjkFontReady,
                fullCjkFontFamily = fullCjkFontFamily,
                onOpen = onOpenReference,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchFilterSection(
    title: String,
    subtitle: String,
    tags: List<String>,
    selectedTags: Set<String>,
    emptyHint: String,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    onTagClick: (String) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                )
            }

            if (tags.isEmpty()) {
                Text(
                    text = emptyHint,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    tags.forEach { tag ->
                        TagFilterChip(
                            tag = tag,
                            selected = selectedTags.contains(normalizeSearchTagToken(tag)),
                            fullCjkFontReady = fullCjkFontReady,
                            fullCjkFontFamily = fullCjkFontFamily,
                            onClick = { onTagClick(tag) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TagFilterChip(
    tag: String,
    selected: Boolean,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = displayTextForUi(tag, fullCjkFontReady),
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontFamily = fullCjkFontFamily,
            )
        },
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                )
            }
        } else {
            null
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFFDDEBFF),
            selectedLabelColor = Color(0xFF1D4ED8),
            containerColor = Color.White,
            labelColor = MaterialTheme.colorScheme.onSurface,
        )
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActiveSearchTagsSection(
    tags: List<SearchTag>,
    locale: AppLocale,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    onRemove: (String) -> Unit,
) {
    SectionCard(
        title = if (locale == AppLocale.ZhCn) "当前搜索标签" else "Active search tags",
        subtitle = if (locale == AppLocale.ZhCn) "删除任意标签后会立刻用剩余标签重新搜索" else "Removing a tag immediately refreshes the results",
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            tags.forEach { tag ->
                SearchTagChip(
                    tag = tag,
                    fullCjkFontReady = fullCjkFontReady,
                    fullCjkFontFamily = fullCjkFontFamily,
                    onRemove = { onRemove(tag.value) },
                )
            }
        }
    }
}

@Composable
private fun SearchTagChip(
    tag: SearchTag,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    onRemove: () -> Unit,
) {
    Surface(
        color = if (tag.source == SearchTagSource.LibraryTag) Color(0xFFDDEBFF) else Color(0xFFE2E8F0),
        contentColor = if (tag.source == SearchTagSource.LibraryTag) Color(0xFF1D4ED8) else Color(0xFF334155),
        shape = RoundedCornerShape(999.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        ) {
            Text(
                text = displayTextForUi(tag.value, fullCjkFontReady),
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontFamily = fullCjkFontFamily,
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(20.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

@Composable
private fun TagPill(tag: String, fullCjkFontReady: Boolean, fullCjkFontFamily: FontFamily) {
    Surface(
        color = Color(0xFFE2E8F0),
        contentColor = Color(0xFF334155),
        shape = RoundedCornerShape(999.dp),
    ) {
        Text(
            text = displayTextForUi(tag, fullCjkFontReady),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontFamily = fullCjkFontFamily,
        )
    }
}

@Composable
private fun RemovableTagChip(
    tag: String,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    onRemove: () -> Unit,
    actionIcon: ImageVector = Icons.Outlined.Remove,
) {
    Box {
        Surface(
            color = Color(0xFFE2E8F0),
            contentColor = Color(0xFF334155),
            shape = RoundedCornerShape(999.dp),
        ) {
            Text(
                text = displayTextForUi(tag, fullCjkFontReady),
                modifier = Modifier.padding(start = 12.dp, end = 24.dp, top = 8.dp, bottom = 8.dp),
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontFamily = fullCjkFontFamily,
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 6.dp, y = (-6).dp)
                .size(18.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(999.dp))
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = actionIcon,
                contentDescription = null,
                tint = Color(0xFF475569),
                modifier = Modifier.size(12.dp),
            )
        }
    }
}

@Composable
private fun SearchEmptyState(
    title: String,
    body: String,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = Color(0xFF475569),
                    modifier = Modifier.size(28.dp),
                )
            }
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailPage(
    appState: FileManagerAppState,
    browserReferencePicker: BrowserReferencePicker?,
    browserReferenceResolver: BrowserReferenceResolver?,
    locale: AppLocale,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    reference: FileReference?,
    onBackHome: () -> Unit,
    onReplaceReference: (FileReference) -> Unit,
    onDeleteReference: () -> Unit,
) {
    if (reference == null) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            TopBarCard(
                locale = locale,
                title = if (locale == AppLocale.ZhCn) "\u6587\u4ef6\u8be6\u60c5" else "File details",
                onBack = onBackHome,
            )
            SectionCard(
                title = if (locale == AppLocale.ZhCn) "\u6ca1\u6709\u9009\u4e2d\u6587\u4ef6" else "No file selected",
                subtitle = if (locale == AppLocale.ZhCn) {
                    "\u5148\u4ece\u4e3b\u9875\u3001\u5168\u90e8\u6587\u4ef6\u9875\u6216\u641c\u7d22\u7ed3\u679c\u9875\u6253\u5f00\u4e00\u4e2a\u6587\u4ef6\u3002"
                } else {
                    "Open a file from the home page, library, or search results first."
                },
            ) {
                EmptyPanel(
                    title = if (locale == AppLocale.ZhCn) "\u5f53\u524d\u6ca1\u6709\u6587\u4ef6" else "Nothing open",
                    body = if (locale == AppLocale.ZhCn) {
                        "\u6253\u5f00\u4e00\u4e2a\u6587\u4ef6\u540e\uff0c\u8fd9\u91cc\u4f1a\u663e\u793a\u6587\u4ef6\u56fe\u6807\u3001\u6807\u7b7e\u548c\u64cd\u4f5c\u6309\u94ae\u3002"
                    } else {
                        "Once opened, this page shows the icon, tags, and actions."
                    },
                )
            }
        }
        return
    }

    val coroutineScope = rememberCoroutineScope()
    var showTagDialog by remember(reference.id) { mutableStateOf(false) }
    var newTagDraft by remember(reference.id) { mutableStateOf("") }
    var openFileMessage by remember(reference.id) { mutableStateOf<String?>(null) }
    var tagFeedbackMessage by remember(reference.id) { mutableStateOf<String?>(null) }
    var pendingOpenReplacement by remember(reference.id) { mutableStateOf<BrowserReferenceDraft?>(null) }
    val isBrowserSelectedReference = reference.source.startsWith("browser-", ignoreCase = true)
    val tagCandidates = remember(reference.tags, appState.topTags) {
        resolveDetailTagCandidates(reference.tags, appState.topTags)
    }
    val canUseWebReopenFlow = browserReferencePicker != null &&
        browserReferenceResolver != null &&
        isBrowserSelectedReference
    val canOpenFile = canOpenReferenceExternally(reference) || canUseWebReopenFlow
    val openButtonLabel = if (locale == AppLocale.ZhCn) "\u2197 \u6253\u5f00\u6b64\u6587\u4ef6" else "\u2197 Open this file"

    fun addTagToCurrentReference(rawTag: String): Boolean {
        val cleaned = rawTag.trim()
        if (cleaned.isBlank()) {
            tagFeedbackMessage = null
            return false
        }

        val existingTag = resolveExistingTagExact(tagCandidates, cleaned) ?: cleaned
        if (reference.tags.any { it.trim() == existingTag }) {
            tagFeedbackMessage = if (locale == AppLocale.ZhCn) {
                "\u8be5\u6587\u4ef6\u5df2\u5305\u542b\u6b64\u6807\u7b7e"
            } else {
                "This file already has that tag."
            }
            return false
        }

        appState.updateReferenceTags(
            reference.id,
            reference.tags.plus(existingTag).joinToString(", "),
        )
        tagFeedbackMessage = null
        newTagDraft = ""
        return true
    }

    fun removeTagFromCurrentReference(tag: String) {
        appState.updateReferenceTags(
            reference.id,
            removeTagExact(reference.tags, tag),
        )
        tagFeedbackMessage = null
    }

    LaunchedEffect(reference.id, reference.source, reference.sourceKind) {
        // 鏂囦欢鏉ユ簮涓€鏃﹀彉鍖栵紝灏辨妸涓婁竴娆＄殑鎵撳紑澶辫触鎻愮ず娓呮帀锛岄伩鍏嶆棫鐘舵€佽瀵肩敤鎴枫€?        openFileMessage = null
        tagFeedbackMessage = null
    }

    fun handleOpenFile() {
        coroutineScope.launch {
            suspend fun replaceAndOpen(draft: BrowserReferenceDraft) {
                val replaced = appState.replaceReference(reference.id, draft) ?: return
                appState.generateThumbnailForReference(replaced.id)
                appState.openReference(replaced.id)
                val refreshed = appState.activeReference?.takeIf { it.id == replaced.id } ?: replaced
                val openResult = openReferenceExternallyWithResult(refreshed)
                val localizedMessage = openResult.message?.let { localizeWebOpenMessage(it, locale) }
                openFileMessage = if (openResult.opened) {
                    null
                } else if (localizedMessage != null) {
                    localizedMessage
                } else if (canUseWebReopenFlow) {
                    webFileReselectionMessage(locale)
                } else {
                    openFileFailureMessage(refreshed, locale)
                }
            }

            if (canUseWebReopenFlow) {
                val needsRefresh = isBrowserSelectedReference
                if (needsRefresh) {
                    appState.refreshReference(reference.id)
                    val resolvedReference = appState.activeReference?.takeIf { it.id == reference.id } ?: reference
                    val refreshOpenResult = openReferenceExternallyWithResult(resolvedReference)
                    if (refreshOpenResult.opened) {
                        openFileMessage = null
                        return@launch
                    }
                    val localizedRefreshMessage = refreshOpenResult.message?.let { localizeWebOpenMessage(it, locale) }
                    if (localizedRefreshMessage != null) {
                        openFileMessage = localizedRefreshMessage
                        return@launch
                    }
                }

                val pickedDraft = runCatching { browserReferencePicker.pickReference() }.getOrNull()
                if (pickedDraft == null) {
                    openFileMessage = webFileReselectionMessage(locale)
                    return@launch
                }

                if (webDraftWeaklyMatches(reference, pickedDraft)) {
                    replaceAndOpen(pickedDraft)
                } else {
                    pendingOpenReplacement = pickedDraft
                }
                return@launch
            }

            val needsRefresh = reference.sourceKind == FileSourceKind.BrowserHandle && reference.source.startsWith("browser-", ignoreCase = true)
            val resolvedReference = if (needsRefresh) {
                appState.refreshReference(reference.id)
                appState.activeReference ?: reference
            } else {
                reference
            }

            val openableReference = appState.activeReference?.takeIf { it.id == resolvedReference.id } ?: resolvedReference
            val openResult = openReferenceExternallyWithResult(openableReference)
            if (openResult.opened) {
                if (!(needsRefresh && openableReference.source.startsWith("browser-", ignoreCase = true))) {
                    appState.openReference(openableReference.id)
                }
                openFileMessage = null
            } else {
                openFileMessage = openResult.message?.let { localizeWebOpenMessage(it, locale) }
                    ?: openFileFailureMessage(openableReference, locale)
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TopBarCard(
            locale = locale,
            title = if (locale == AppLocale.ZhCn) "\u6587\u4ef6\u8be6\u60c5" else "File details",
            onBack = onBackHome,
        )

        DetailHeroCard(
            reference = reference,
            locale = locale,
            fullCjkFontReady = fullCjkFontReady,
            fullCjkFontFamily = fullCjkFontFamily,
            canOpenFile = canOpenFile,
            openButtonLabel = openButtonLabel,
            onOpenFile = ::handleOpenFile,
        )

        SectionCard(
            title = if (locale == AppLocale.ZhCn) "\u6587\u4ef6\u4fe1\u606f" else "File info",
            subtitle = if (locale == AppLocale.ZhCn) {
                "\u8fd9\u91cc\u663e\u793a\u4e0a\u4f20\u65f6\u95f4\u3001\u5927\u5c0f\u548c\u6587\u4ef6\u8def\u5f84\u3002"
            } else {
                "Upload time, size, and file path are shown here."
            },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoRow(
                    label = appState.createdAtLabel,
                    value = formatRelativeTime(reference.createdAtMillis),
                )
                InfoRow(
                    label = if (locale == AppLocale.ZhCn) "\u6587\u4ef6\u5927\u5c0f" else "File size",
                    value = formatFileSize(reference.fileSizeBytes ?: guessFileSizeFromNotes(reference.notes)),
                )
                InfoRow(
                    label = if (locale == AppLocale.ZhCn) "\u6587\u4ef6\u8def\u5f84" else "File path",
                    value = summarizeReferenceSource(reference, locale, fullCjkFontReady).ifBlank {
                        if (locale == AppLocale.ZhCn) "\u6682\u672a\u4fdd\u5b58\u8def\u5f84" else "No file path saved"
                    },
                    valueFontFamily = fullCjkFontFamily,
                )
                if (reference.coverArtSource?.isNotBlank() == true) {
                    InfoRow(
                        label = if (locale == AppLocale.ZhCn) "\u5c01\u9762\u6765\u6e90" else "Cover art source",
                        value = displayTextForUi(reference.coverArtSource.orEmpty(), fullCjkFontReady),
                        valueFontFamily = fullCjkFontFamily,
                    )
                }
                if (reference.thumbnailPath?.isNotBlank() == true) {
                    InfoRow(
                        label = if (locale == AppLocale.ZhCn) "\u7f29\u7565\u56fe\u7f13\u5b58" else "Thumbnail cache",
                        value = thumbnailStatusLabel(reference, locale),
                        valueFontFamily = fullCjkFontFamily,
                    )
                }
                if (openFileMessage != null) {
                    Text(
                        text = openFileMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                    )
                } else if (canUseWebReopenFlow && !canOpenReferenceExternally(reference)) {
                    Text(
                        text = webFileReselectionMessage(locale),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                    )
                } else if (!canOpenFile) {
                    Text(
                        text = if (locale == AppLocale.ZhCn) {
                            "\u8fd9\u4e2a\u6587\u4ef6\u8fd8\u6ca1\u6709\u53ef\u76f4\u63a5\u6253\u5f00\u7684\u672c\u5730\u8def\u5f84\u3002"
                        } else {
                            "This file does not have a directly openable local path."
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                    )
                }
            }
        }

        SectionCard(
            title = if (locale == AppLocale.ZhCn) "\u6807\u7b7e" else "Tags",
            subtitle = if (locale == AppLocale.ZhCn) {
                "\u7528\u6807\u7b7e\u7ed9\u6587\u4ef6\u5206\u7c7b\uff0c\u4fbf\u4e8e\u641c\u7d22\u548c\u63a8\u8350"
            } else {
                "Use tags to organize files for search and recommendations."
            },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (reference.tags.isEmpty()) {
                    Text(
                        text = if (locale == AppLocale.ZhCn) "\u6682\u65e0\u6807\u7b7e" else "No tags yet",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        reference.tags.forEach { tag ->
                            RemovableTagChip(
                                tag = tag,
                                fullCjkFontReady = fullCjkFontReady,
                                fullCjkFontFamily = fullCjkFontFamily,
                                onRemove = { removeTagFromCurrentReference(tag) },
                            )
                        }
                    }
                }

                if (!tagFeedbackMessage.isNullOrBlank()) {
                    Text(
                        text = tagFeedbackMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                    )
                }

                TextButton(onClick = { showTagDialog = true }) {
                    Text(if (locale == AppLocale.ZhCn) "+ \u6dfb\u52a0\u6807\u7b7e" else "+ Add tag")
                }
            }
        }

        SectionCard(
            title = if (locale == AppLocale.ZhCn) "\u6587\u4ef6\u64cd\u4f5c" else "File actions",
            subtitle = if (locale == AppLocale.ZhCn) {
                "\u4fdd\u7559\u5f53\u524d\u6807\u7b7e\u548c\u8bb0\u5f55\uff0c\u53ea\u66f4\u6362\u8fd9\u4e2a\u6761\u76ee\u6307\u5411\u7684\u672c\u5730\u6587\u4ef6\u3002"
            } else {
                "Keep the current tags and history, and only replace the local file linked to this entry."
            },
        ) {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onReplaceReference(reference) },
            ) {
                Text(if (locale == AppLocale.ZhCn) "\u66f4\u6362\u6587\u4ef6\u8def\u5f84" else "Change file path")
            }
        }

        SectionCard(
            title = if (locale == AppLocale.ZhCn) "\u5371\u9669\u64cd\u4f5c" else "Danger zone",
            subtitle = if (locale == AppLocale.ZhCn) {
                "\u4ece\u5e94\u7528\u4e2d\u5220\u9664\u8fd9\u4e2a\u6587\u4ef6\u6761\u76ee\uff0c\u4e0d\u4f1a\u5220\u9664\u672c\u5730\u539f\u6587\u4ef6\u3002"
            } else {
                "Remove this file entry from the app. The original local file will not be deleted."
            },
        ) {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onDeleteReference,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFDC2626),
                ),
            ) {
                Text(if (locale == AppLocale.ZhCn) "\u5220\u9664\u6587\u4ef6\u6761\u76ee" else "Delete file entry")
            }
        }
    }

    if (showTagDialog) {
        AlertDialog(
            onDismissRequest = {
                showTagDialog = false
                newTagDraft = ""
            },
            title = { Text(if (locale == AppLocale.ZhCn) "\u6dfb\u52a0\u6807\u7b7e" else "Add tag") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = newTagDraft,
                        onValueChange = { newTagDraft = it },
                        label = { Text(if (locale == AppLocale.ZhCn) "\u6807\u7b7e\u540d" else "Tag name") },
                        placeholder = {
                            Text(
                                if (locale == AppLocale.ZhCn) "\u8f93\u5165\u6807\u7b7e" else "Type a tag",
                            )
                        },
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontFamily = fullCjkFontFamily),
                    )

                    if (!tagFeedbackMessage.isNullOrBlank()) {
                        Text(
                            text = tagFeedbackMessage.orEmpty(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (locale == AppLocale.ZhCn) "\u5e38\u7528\u6807\u7b7e" else "Common tags",
                            fontWeight = FontWeight.Medium,
                        )
                        if (tagCandidates.isEmpty()) {
                            Text(
                                text = if (locale == AppLocale.ZhCn) "\u6682\u65e0\u5019\u9009\u6807\u7b7e" else "No tag suggestions yet",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                            )
                        } else {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                tagCandidates.forEach { tag ->
                                    FilterChip(
                                        selected = false,
                                        onClick = {
                                            if (addTagToCurrentReference(tag)) {
                                                newTagDraft = ""
                                                showTagDialog = false
                                            }
                                        },
                                        label = {
                                            Text(
                                                text = displayTextForUi(tag, fullCjkFontReady),
                                                fontFamily = fullCjkFontFamily,
                                            )
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val added = addTagToCurrentReference(newTagDraft)
                        if (added) {
                            newTagDraft = ""
                            showTagDialog = false
                        }
                    },
                    enabled = newTagDraft.trim().isNotEmpty(),
                ) {
                    Text(if (locale == AppLocale.ZhCn) "\u6dfb\u52a0" else "Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        newTagDraft = ""
                        showTagDialog = false
                    },
                ) {
                    Text(if (locale == AppLocale.ZhCn) "\u53d6\u6d88" else "Cancel")
                }
            },
        )
    }

    if (pendingOpenReplacement != null) {
        val draft = pendingOpenReplacement!!
        AlertDialog(
            onDismissRequest = { pendingOpenReplacement = null },
            title = {
                Text(if (locale == AppLocale.ZhCn) "\u6240\u9009\u6587\u4ef6\u53ef\u80fd\u4e0d\u662f\u539f\u6587\u4ef6" else "The selected file may not be the original")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (locale == AppLocale.ZhCn) {
                            "\u65b0\u9009\u62e9\u7684\u6587\u4ef6\u548c\u5f53\u524d\u6761\u76ee\u7684\u540d\u79f0\u3001\u7c7b\u578b\u6216\u5927\u5c0f\u4e0d\u5b8c\u5168\u4e00\u81f4\u3002"
                        } else {
                            "The newly selected file does not fully match the current entry's name, type, or size."
                        },
                    )
                    Text(
                        text = if (locale == AppLocale.ZhCn) {
                            "\u4f60\u53ef\u4ee5\u53d6\u6d88\uff0c\u6216\u8005\u7ee7\u7eed\u7528\u8fd9\u4e2a\u6587\u4ef6\u66ff\u6362\u5f53\u524d\u5f15\u7528\u3002"
                        } else {
                            "You can cancel, or continue and replace the current reference with this file."
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val replacement = pendingOpenReplacement ?: return@TextButton
                        pendingOpenReplacement = null
                        coroutineScope.launch {
                            val replaced = appState.replaceReference(reference.id, replacement) ?: return@launch
                            appState.generateThumbnailForReference(replaced.id)
                            appState.openReference(replaced.id)
                            val refreshed = appState.activeReference?.takeIf { it.id == replaced.id } ?: replaced
                            val openResult = openReferenceExternallyWithResult(refreshed)
                            openFileMessage = if (openResult.opened) {
                                null
                            } else {
                                openResult.message?.let { localizeWebOpenMessage(it, locale) }
                                    ?: webFileReselectionMessage(locale)
                            }
                        }
                    },
                ) {
                    Text(if (locale == AppLocale.ZhCn) "\u4f5c\u4e3a\u66ff\u6362\u5f15\u7528\u7ee7\u7eed" else "Continue as replacement")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingOpenReplacement = null }) {
                    Text(if (locale == AppLocale.ZhCn) "\u53d6\u6d88" else "Cancel")
                }
            },
        )
    }
}

@Composable
private fun TopBarCard(
    locale: AppLocale,
    title: String,
    onMenuClick: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    leading: @Composable RowScope.() -> Unit = {},
    trailing: @Composable RowScope.() -> Unit = {},
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onMenuClick != null) {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Outlined.Menu,
                        contentDescription = if (locale == AppLocale.ZhCn) "\u83dc\u5355" else "Menu",
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = if (locale == AppLocale.ZhCn) "\u8fd4\u56de" else "Back",
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            leading()

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(modifier = Modifier.weight(1f))
            trailing()
        }
    }
}

@Composable
private fun LanguageToggleButton(
    locale: AppLocale,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = if (locale == AppLocale.ZhCn) "\u4e2d\u6587" else "English",
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Medium,
        )
        Icon(
            imageVector = Icons.Outlined.ArrowDropDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun SideMenuPanel(
    appState: FileManagerAppState,
    locale: AppLocale,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    onClose: () -> Unit,
    onToggleLanguage: () -> Unit,
) {
    var showAllTagsDialog by remember { mutableStateOf(false) }
    var pendingDeleteTag by remember { mutableStateOf<String?>(null) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopStart,
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
            modifier = Modifier
                .fillMaxHeight()
                .width(280.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (locale == AppLocale.ZhCn) "\u529f\u80fd" else "Menu",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    TextButton(onClick = onClose) {
                        Text(if (locale == AppLocale.ZhCn) "\u5173\u95ed" else "Close")
                    }
                }

                Text(
                    text = if (locale == AppLocale.ZhCn) {
                        "\u5728\u8fd9\u91cc\u5207\u6362\u8bed\u8a00\u3002"
                    } else {
                        "Switch the app language here."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                )

                var languageMenuExpanded by remember { mutableStateOf(false) }
                Box {
                    LanguageToggleButton(
                        locale = locale,
                        onClick = { languageMenuExpanded = true },
                    )
                    DropdownMenu(
                        expanded = languageMenuExpanded,
                        onDismissRequest = { languageMenuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    if (locale == AppLocale.ZhCn) {
                                        Icon(
                                            imageVector = Icons.Outlined.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                    Text("\u4e2d\u6587")
                                }
                            },
                            onClick = {
                                languageMenuExpanded = false
                                if (locale != AppLocale.ZhCn) {
                                    onToggleLanguage()
                                }
                            },
                        )
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    if (locale == AppLocale.EnUs) {
                                        Icon(
                                            imageVector = Icons.Outlined.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                    Text("English")
                                }
                            },
                            onClick = {
                                languageMenuExpanded = false
                                if (locale != AppLocale.EnUs) {
                                    onToggleLanguage()
                                }
                            },
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .clickable { showAllTagsDialog = true }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = if (locale == AppLocale.ZhCn) "\u5168\u90e8\u6807\u7b7e" else "All tags",
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = ">",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }

    if (showAllTagsDialog) {
        AlertDialog(
            onDismissRequest = { showAllTagsDialog = false },
            containerColor = Color.White,
            title = { Text(if (locale == AppLocale.ZhCn) "\u5168\u90e8\u6807\u7b7e" else "All tags") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (appState.allTags.isEmpty()) {
                        Text(
                            text = if (locale == AppLocale.ZhCn) "\u6682\u65e0\u6807\u7b7e" else "No tags yet",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            appState.allTags.forEach { tag ->
                                RemovableTagChip(
                                    tag = tag,
                                    fullCjkFontReady = fullCjkFontReady,
                                    fullCjkFontFamily = fullCjkFontFamily,
                                    onRemove = { pendingDeleteTag = tag },
                                    actionIcon = Icons.Outlined.Close,
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAllTagsDialog = false }) {
                    Text(if (locale == AppLocale.ZhCn) "\u5173\u95ed" else "Close")
                }
            },
        )
    }

    val deleteTarget = pendingDeleteTag
    if (deleteTarget != null) {
        val affectedFileCount = appState.allReferences.count { reference ->
            reference.tags.any { normalize(it) == normalize(deleteTarget) }
        }
        AlertDialog(
            onDismissRequest = { pendingDeleteTag = null },
            title = { Text(if (locale == AppLocale.ZhCn) "\u5220\u9664\u6807\u7b7e" else "Delete tag") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        if (locale == AppLocale.ZhCn) {
                            "\u5220\u9664\u540e\u4f1a\u4ece\u6240\u6709\u6587\u4ef6\u4e2d\u79fb\u9664\u6b64\u6807\u7b7e\u3002"
                        } else {
                            "This will remove the tag from all files."
                        },
                    )
                    Text(
                        if (locale == AppLocale.ZhCn) {
                            "\u8be5\u6807\u7b7e\u5c06\u4ece $affectedFileCount \u4e2a\u6587\u4ef6\u4e2d\u79fb\u9664\u3002"
                        } else {
                            "This tag will be removed from $affectedFileCount files."
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        appState.deleteTagEverywhere(deleteTarget)
                        pendingDeleteTag = null
                    },
                ) {
                    Text(if (locale == AppLocale.ZhCn) "\u5220\u9664" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteTag = null }) {
                    Text(if (locale == AppLocale.ZhCn) "\u53d6\u6d88" else "Cancel")
                }
            },
        )
    }
}

@Composable
private fun SearchBarCard(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    placeholder: String,
    buttonLabel: String,
    textStyle: androidx.compose.ui.text.TextStyle,
    supportingText: String? = null,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = { Text(placeholder) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                    textStyle = textStyle,
                )
                Button(onClick = onSearch) {
                    Text(buttonLabel)
                }
            }
            val feedback = supportingText?.trim()?.takeIf { it.isNotEmpty() }
            if (feedback != null) {
                Text(
                    text = feedback,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun SearchBarInline(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    placeholder: String,
    buttonLabel: String,
    textStyle: androidx.compose.ui.text.TextStyle,
) {
    Row(
        modifier = Modifier.widthIn(min = 280.dp, max = 520.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text(placeholder) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            textStyle = textStyle,
        )
        Button(onClick = onSearch) {
            Text(buttonLabel)
        }
    }
}

@Composable
private fun SortMenuButton(
    sortMode: FileSortMode,
    onSortModeChange: (FileSortMode) -> Unit,
    locale: AppLocale,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(if (locale == AppLocale.ZhCn) "\u6392\u5e8f" else "Sort")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            FileSortMode.entries.forEach { mode ->
                DropdownMenuItem(
                    text = { Text(sortModeLabel(mode, locale)) },
                    onClick = {
                        onSortModeChange(mode)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    subtitle: String,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = subtitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                    )
                }
                trailing?.invoke()
            }
            content()
        }
    }
}

@Composable
private fun EmptyPanel(title: String, body: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(text = title, fontWeight = FontWeight.SemiBold)
            Text(
                text = body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun AdaptiveFileGrid(
    items: List<FileReference>,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    onOpen: (FileReference) -> Unit,
) {
    if (items.isEmpty()) return

    BoxWithConstraints {
        val columns = when {
            maxWidth >= 1280.dp -> 9
            maxWidth >= 1120.dp -> 8
            maxWidth >= 960.dp -> 7
            maxWidth >= 720.dp -> 5
            maxWidth >= 520.dp -> 4
            maxWidth >= 380.dp -> 3
            else -> 2
        }
        val spacing = 10.dp
        val tileWidth = (maxWidth - spacing * (columns - 1)) / columns

        Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
            for (rowItems in items.chunked(columns)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing),
                ) {
                    for (reference in rowItems) {
                        FileTileCard(
                            reference = reference,
                            fullCjkFontReady = fullCjkFontReady,
                            fullCjkFontFamily = fullCjkFontFamily,
                            onOpen = { onOpen(reference) },
                            modifier = Modifier.width(tileWidth),
                        )
                    }
                    for (index in rowItems.size until columns) {
                        Spacer(modifier = Modifier.width(tileWidth))
                    }
                }
            }
        }
    }
}

@Composable
private fun FileTileCard(
    reference: FileReference,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconStyle = fileTypeIconStyle(reference)
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .aspectRatio(0.92f)
            .border(
                width = 1.dp,
                color = Color(0xFFD9E3F0),
                shape = RoundedCornerShape(20.dp),
            )
            .clickable(onClick = onOpen),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            FileCoverArtFrame(
                reference = reference,
                iconStyle = iconStyle,
                fullCjkFontReady = fullCjkFontReady,
                fullCjkFontFamily = fullCjkFontFamily,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp),
                iconSize = 54.dp,
            )

            Text(
                text = displayTextForUi(reference.title, fullCjkFontReady),
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontFamily = fullCjkFontFamily,
            )
        }
    }
}

@Composable
private fun DetailHeroCard(
    reference: FileReference,
    locale: AppLocale,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    canOpenFile: Boolean,
    openButtonLabel: String,
    onOpenFile: () -> Unit,
) {
    val iconStyle = fileTypeIconStyle(reference)
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            FileCoverArtFrame(
                reference = reference,
                iconStyle = iconStyle,
                fullCjkFontReady = fullCjkFontReady,
                fullCjkFontFamily = fullCjkFontFamily,
                modifier = Modifier
                    .size(120.dp),
                cornerShape = RoundedCornerShape(30.dp),
                iconSize = 54.dp,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = displayTextForUi(reference.title, fullCjkFontReady),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            fontFamily = fullCjkFontFamily,
                        )
                    }

                    OutlinedButton(
                        onClick = onOpenFile,
                        enabled = canOpenFile,
                    ) {
                        Text(openButtonLabel)
                    }
                }
            }
        }
    }
}

@Composable
private fun FileCoverArtFrame(
    reference: FileReference,
    iconStyle: FileTypeIconStyle,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    modifier: Modifier = Modifier,
    cornerShape: RoundedCornerShape = RoundedCornerShape(16.dp),
    iconSize: androidx.compose.ui.unit.Dp = 24.dp,
) {
    val thumbnailPainter = rememberThumbnailPainter(reference.thumbnailPath)
    val hasThumbnail = thumbnailPainter != null && reference.thumbnailStatus == ThumbnailStatus.READY
    val hasCoverArt = reference.coverArtSource?.isNotBlank() == true
    println("Thumbnail UI state: fileId=${reference.id}, hasThumbnailPath=${!reference.thumbnailPath.isNullOrBlank()}, thumbnailStatus=${reference.thumbnailStatus}, painterReady=$hasThumbnail, fallback=${!hasThumbnail}")
    Box(
        modifier = modifier
            .clip(cornerShape)
            .background(iconStyle.backgroundBrush),
        contentAlignment = Alignment.Center,
    ) {
        if (hasThumbnail) {
            Image(
                painter = thumbnailPainter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else if (hasCoverArt) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Cover art",
                    color = iconStyle.tint,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                )
                Text(
                    text = displayTextForUi(reference.coverArtSource.orEmpty(), fullCjkFontReady),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = fullCjkFontFamily,
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = iconStyle.icon,
                    contentDescription = null,
                    tint = iconStyle.tint,
                    modifier = Modifier.size(iconSize),
                )
                if (FileTypeClassifier.classify(reference) == FileTypeCategory.Video || FileTypeClassifier.classify(reference) == FileTypeCategory.Audio) {
                    Text(
                        text = if (reference.thumbnailStatus == ThumbnailStatus.GENERATING) "Loading" else "Reserved",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueFontFamily: FontFamily? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.width(96.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
        )
        Text(
            text = value,
            fontWeight = FontWeight.Medium,
            fontFamily = valueFontFamily,
        )
    }
}

private data class FileTypeIconStyle(
    val icon: ImageVector,
    val tint: Color,
    val backgroundBrush: Brush,
)

@Suppress("DEPRECATION")
private fun fileTypeIconStyle(reference: FileReference): FileTypeIconStyle {
    return when (FileTypeClassifier.classify(reference)) {
        FileTypeCategory.TextDocument -> FileTypeIconStyle(
            icon = Icons.Outlined.Description,
            tint = Color(0xFF2563EB),
            backgroundBrush = Brush.linearGradient(listOf(Color(0xFFE8F1FF), Color(0xFFDDEBFF))),
        )
        FileTypeCategory.PdfDocument -> FileTypeIconStyle(
            icon = Icons.Outlined.PictureAsPdf,
            tint = Color(0xFFB91C1C),
            backgroundBrush = Brush.linearGradient(listOf(Color(0xFFFFE8E8), Color(0xFFFED7D7))),
        )
        FileTypeCategory.Video -> FileTypeIconStyle(
            icon = Icons.Outlined.Movie,
            tint = Color(0xFF7C3AED),
            backgroundBrush = Brush.linearGradient(listOf(Color(0xFFF0E8FF), Color(0xFFE4D7FE))),
        )
        FileTypeCategory.Audio -> FileTypeIconStyle(
            icon = Icons.Outlined.MusicNote,
            tint = Color(0xFF0F766E),
            backgroundBrush = Brush.linearGradient(listOf(Color(0xFFE6FBF8), Color(0xFFCFF7F0))),
        )
        FileTypeCategory.Image -> FileTypeIconStyle(
            icon = Icons.Outlined.Image,
            tint = Color(0xFF0891B2),
            backgroundBrush = Brush.linearGradient(listOf(Color(0xFFE5FBFF), Color(0xFFCFF6FF))),
        )
        FileTypeCategory.Archive -> FileTypeIconStyle(
            icon = Icons.Outlined.Archive,
            tint = Color(0xFFB45309),
            backgroundBrush = Brush.linearGradient(listOf(Color(0xFFFFF1E3), Color(0xFFFDE7C7))),
        )
        FileTypeCategory.Code -> FileTypeIconStyle(
            icon = Icons.Outlined.Code,
            tint = Color(0xFF1D4ED8),
            backgroundBrush = Brush.linearGradient(listOf(Color(0xFFEAF2FF), Color(0xFFD8E6FF))),
        )
        FileTypeCategory.Spreadsheet -> FileTypeIconStyle(
            icon = Icons.Outlined.TableChart,
            tint = Color(0xFF059669),
            backgroundBrush = Brush.linearGradient(listOf(Color(0xFFE6FBF0), Color(0xFFD1F5E0))),
        )
        FileTypeCategory.Presentation -> FileTypeIconStyle(
            icon = Icons.Outlined.Slideshow,
            tint = Color(0xFFDB2777),
            backgroundBrush = Brush.linearGradient(listOf(Color(0xFFFFECF4), Color(0xFFFAD6E8))),
        )
        FileTypeCategory.Folder -> FileTypeIconStyle(
            icon = Icons.Outlined.Folder,
            tint = Color(0xFFB45309),
            backgroundBrush = Brush.linearGradient(listOf(Color(0xFFFFF4E5), Color(0xFFFFE7C4))),
        )
        else -> FileTypeIconStyle(
            icon = Icons.Outlined.InsertDriveFile,
            tint = Color(0xFF475569),
            backgroundBrush = Brush.linearGradient(listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0))),
        )
    }
}

@Composable
private fun SortChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
    )
}

@Composable
private fun ManualAddDialog(
    appState: FileManagerAppState,
    locale: AppLocale,
    fullCjkFontReady: Boolean,
    fullCjkTextStyle: androidx.compose.ui.text.TextStyle,
    notice: String?,
    editorMode: ReferenceEditorMode,
    draftFileSizeText: String,
    onDraftFileSizeTextChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val isReplaceMode = editorMode == ReferenceEditorMode.Replace
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (isReplaceMode) {
                    if (locale == AppLocale.ZhCn) "\u66ff\u6362\u6587\u4ef6" else "Replace file"
                } else {
                    if (locale == AppLocale.ZhCn) "\u6dfb\u52a0\u6587\u4ef6" else "Add file"
                },
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!notice.isNullOrBlank()) {
                    Text(
                        text = notice,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = draftFileSizeText,
                    onValueChange = onDraftFileSizeTextChange,
                    label = { Text(if (locale == AppLocale.ZhCn) "\u6587\u4ef6\u5927\u5c0f\uff08\u5b57\u8282\uff09" else "File size (bytes)") },
                    singleLine = true,
                    textStyle = fullCjkTextStyle,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = appState.draftTitle,
                    onValueChange = { appState.draftTitle = it },
                    label = { Text(appState.referenceTitle) },
                    singleLine = true,
                    textStyle = fullCjkTextStyle,
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = appState.draftSource,
                    onValueChange = { appState.draftSource = it },
                    label = { Text(appState.referenceLocation) },
                    singleLine = true,
                    textStyle = fullCjkTextStyle,
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = appState.draftType,
                    onValueChange = { appState.draftType = it },
                    label = { Text(appState.fileType) },
                    singleLine = true,
                    textStyle = fullCjkTextStyle,
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = appState.draftCoverArtSource,
                    onValueChange = { appState.draftCoverArtSource = it },
                    label = { Text(if (locale == AppLocale.ZhCn) "\u5c01\u9762\u6765\u6e90" else "Cover art source") },
                    singleLine = true,
                    textStyle = fullCjkTextStyle,
                )
                if (!isReplaceMode) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = appState.draftTags,
                        onValueChange = { appState.draftTags = it },
                        label = { Text(appState.tagsCommaSeparated) },
                        singleLine = true,
                        textStyle = fullCjkTextStyle,
                    )
                } else {
                    Text(
                        text = if (locale == AppLocale.ZhCn) {
                            "\u66ff\u6362\u65f6\u4f1a\u4fdd\u7559\u5f53\u524d\u6807\u7b7e\u3002"
                        } else {
                            "Current tags will be kept during replacement."
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = appState.draftNotes,
                    onValueChange = { appState.draftNotes = it },
                    label = { Text(appState.notes) },
                    minLines = 2,
                    textStyle = fullCjkTextStyle,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    if (isReplaceMode) {
                        if (locale == AppLocale.ZhCn) "\u66ff\u6362\u6b64\u5f15\u7528" else "Replace reference"
                    } else {
                        appState.addReference
                    },
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(appState.cancel)
            }
        },
    )
}

private fun browserPickerUnavailableMessage(locale: AppLocale): String =
    when (locale) {
        AppLocale.ZhCn -> "浏览器文件选择器不可用，请手动添加引用。"
        AppLocale.EnUs -> "Browser file picker is not available. Please add a reference manually."
    }

private fun sortReferences(
    references: List<FileReference>,
    sortMode: FileSortMode,
    sortDirection: SortDirection,
): List<FileReference> =
    when (sortMode) {
        FileSortMode.RecentOpened -> references.sortedWith(compareByRecentOpened(sortDirection))
        FileSortMode.RecentAdded -> references.sortedWith(compareByCreatedAt(sortDirection))
        FileSortMode.Name -> references.sortedWith(compareByName(sortDirection))
        FileSortMode.FileSize -> references.sortedWith(compareByFileSize(sortDirection))
    }

private fun sortModeLabel(mode: FileSortMode, locale: AppLocale): String =
    when (mode) {
        FileSortMode.RecentOpened -> if (locale == AppLocale.ZhCn) "\u6700\u8fd1\u6253\u5f00" else "Recent opened"
        FileSortMode.RecentAdded -> if (locale == AppLocale.ZhCn) "\u6700\u8fd1\u6dfb\u52a0" else "Recent added"
        FileSortMode.Name -> if (locale == AppLocale.ZhCn) "\u6587\u4ef6\u540d" else "Name"
        FileSortMode.FileSize -> if (locale == AppLocale.ZhCn) "\u5927\u5c0f" else "Size"
    }

private fun defaultSortDirection(mode: FileSortMode): SortDirection =
    when (mode) {
        FileSortMode.RecentOpened -> SortDirection.Descending
        FileSortMode.RecentAdded -> SortDirection.Descending
        FileSortMode.Name -> SortDirection.Ascending
        FileSortMode.FileSize -> SortDirection.Descending
    }

private fun SortDirection.toggled(): SortDirection =
    when (this) {
        SortDirection.Ascending -> SortDirection.Descending
        SortDirection.Descending -> SortDirection.Ascending
    }

private fun sortChipLabel(
    mode: FileSortMode,
    activeMode: FileSortMode,
    activeDirection: SortDirection,
    locale: AppLocale,
): String {
    val direction = if (mode == activeMode) activeDirection else defaultSortDirection(mode)
    val arrow = if (direction == SortDirection.Descending) "\u2193" else "\u2191"
    return "${sortModeLabel(mode, locale)} $arrow"
}

private fun typeFilterLabel(filter: AllFilesTypeFilter, locale: AppLocale): String =
    when (filter) {
        AllFilesTypeFilter.All -> if (locale == AppLocale.ZhCn) "\u5168\u90e8" else "All"
        AllFilesTypeFilter.Image -> if (locale == AppLocale.ZhCn) "\u56fe\u7247" else "Images"
        AllFilesTypeFilter.Video -> if (locale == AppLocale.ZhCn) "\u89c6\u9891" else "Videos"
        AllFilesTypeFilter.Document -> if (locale == AppLocale.ZhCn) "\u6587\u6863" else "Documents"
        AllFilesTypeFilter.Spreadsheet -> if (locale == AppLocale.ZhCn) "\u8868\u683c" else "Spreadsheets"
        AllFilesTypeFilter.Presentation -> if (locale == AppLocale.ZhCn) "\u6f14\u793a" else "Presentations"
        AllFilesTypeFilter.Other -> if (locale == AppLocale.ZhCn) "\u5176\u4ed6" else "Other"
    }

private fun resolveAvailableTypeFilters(references: List<FileReference>): List<AllFilesTypeFilter> {
    val filters = linkedSetOf(AllFilesTypeFilter.All)
    references
        .map(::resolveTypeFilter)
        .forEach(filters::add)
    return filters.toList()
}

private fun filterReferencesByType(
    references: List<FileReference>,
    filter: AllFilesTypeFilter,
): List<FileReference> {
    if (filter == AllFilesTypeFilter.All) return references
    return references.filter { resolveTypeFilter(it) == filter }
}

private fun resolveTypeFilter(reference: FileReference): AllFilesTypeFilter =
    when (FileTypeClassifier.classify(reference)) {
        FileTypeCategory.Image -> AllFilesTypeFilter.Image
        FileTypeCategory.Video -> AllFilesTypeFilter.Video
        FileTypeCategory.TextDocument,
        FileTypeCategory.PdfDocument -> AllFilesTypeFilter.Document
        FileTypeCategory.Spreadsheet -> AllFilesTypeFilter.Spreadsheet
        FileTypeCategory.Presentation -> AllFilesTypeFilter.Presentation
        else -> AllFilesTypeFilter.Other
    }

private fun compareByRecentOpened(direction: SortDirection): Comparator<FileReference> =
    Comparator { left, right ->
        compareNullableLong(
            left = left.lastOpenedAtMillis,
            right = right.lastOpenedAtMillis,
            direction = direction,
        ).takeIf { it != 0 }
            ?: compareByCreatedAt(SortDirection.Descending).compare(left, right)
    }

private fun compareByCreatedAt(direction: SortDirection): Comparator<FileReference> =
    Comparator { left, right ->
        compareLong(left.createdAtMillis, right.createdAtMillis, direction).takeIf { it != 0 }
            ?: compareByName(SortDirection.Ascending).compare(left, right)
    }

private fun compareByName(direction: SortDirection): Comparator<FileReference> =
    Comparator { left, right ->
        compareString(normalize(left.title), normalize(right.title), direction).takeIf { it != 0 }
            ?: compareLong(left.createdAtMillis, right.createdAtMillis, SortDirection.Descending)
    }

private fun compareByFileSize(direction: SortDirection): Comparator<FileReference> =
    Comparator { left, right ->
        compareNullableLong(
            left = left.fileSizeBytes ?: guessFileSizeFromNotes(left.notes),
            right = right.fileSizeBytes ?: guessFileSizeFromNotes(right.notes),
            direction = direction,
        ).takeIf { it != 0 }
            ?: compareByCreatedAt(SortDirection.Descending).compare(left, right)
    }

private fun compareLong(left: Long, right: Long, direction: SortDirection): Int =
    when (direction) {
        SortDirection.Ascending -> left.compareTo(right)
        SortDirection.Descending -> right.compareTo(left)
    }

private fun compareNullableLong(
    left: Long?,
    right: Long?,
    direction: SortDirection,
): Int = when {
    left == null && right == null -> 0
    left == null -> 1
    right == null -> -1
    else -> compareLong(left, right, direction)
}

private fun compareString(left: String, right: String, direction: SortDirection): Int =
    when (direction) {
        SortDirection.Ascending -> left.compareTo(right)
        SortDirection.Descending -> right.compareTo(left)
    }

private fun resolveRecommendedReferences(
    recommendationState: RecommendationReadOnlyState,
): List<FileReference> = recommendationState.recommendedReferences.take(10)

private fun mergeTags(existingTags: List<String>, newTag: String): String {
    val cleaned = newTag.trim()
    if (cleaned.isBlank()) return existingTags.joinToString(", ")

    val merged = existingTags.toMutableList()
    if (merged.none { normalize(it) == normalize(cleaned) }) {
        merged += cleaned
    }
    return merged.joinToString(", ")
}

private fun resolveDetailTagCandidates(
    currentTags: List<String>,
    topTags: List<String>,
): List<String> =
    topTags
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .filterNot { candidate -> currentTags.any { it.trim().equals(candidate, ignoreCase = true) } }
        .distinct()
        .take(MAX_DETAIL_TAG_CANDIDATES)

private fun resolveExistingTagExact(
    tags: List<String>,
    target: String,
): String? = tags.firstOrNull { it.trim() == target }

private fun removeTagExact(existingTags: List<String>, tag: String): String =
    existingTags
        .filterNot { it.trim() == tag.trim() }
        .joinToString(", ")

private fun removeTag(existingTags: List<String>, tag: String): String =
    existingTags
        .filterNot { normalize(it) == normalize(tag) }
        .joinToString(", ")

private fun canOpenReferenceExternally(reference: FileReference): Boolean {
    val source = reference.source.trim()
    if (source.isBlank()) return false
    return when (reference.sourceKind) {
        FileSourceKind.ManualPath,
        FileSourceKind.Url,
        FileSourceKind.BrowserHandle,
        -> true
        else -> false
    }
}

private fun searchValidationMessage(query: String, locale: AppLocale): String? {
    val trimmed = query.trim()
    if (trimmed.isBlank()) {
        return if (locale == AppLocale.ZhCn) {
            "\u8bf7\u8f93\u5165\u5173\u952e\u8bcd\u3002"
        } else {
            "Enter a keyword."
        }
    }
    if (tokenizeSubmittedSearch(trimmed).isNotEmpty()) return null
    return if (locale == AppLocale.ZhCn) {
        "请输入更具体的关键词；中文关键词至少两个字。"
    } else {
        "Enter a more specific keyword. Chinese keywords must contain at least two characters."
    }
}

private const val MAX_DETAIL_TAG_CANDIDATES = 8

private fun localizeWebOpenMessage(message: String, locale: AppLocale): String {
    val normalized = message.trim()
    if (!normalized.contains('\n')) return normalized
    val parts = normalized
        .split('\n')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
    if (parts.isEmpty()) return normalized
    return when (locale) {
        AppLocale.ZhCn -> parts.first()
        AppLocale.EnUs -> parts.last()
    }
}

private fun openFileFailureMessage(reference: FileReference, locale: AppLocale): String =
    when {
        reference.source.isBlank() -> if (locale == AppLocale.ZhCn) {
            "\u8fd9\u4e2a\u6587\u4ef6\u6ca1\u6709\u4fdd\u5b58\u8def\u5f84\u3002"
        } else {
            "This file does not have a saved path."
        }
        reference.source.startsWith("browser-", ignoreCase = true) -> if (locale == AppLocale.ZhCn) {
            "\u8fd9\u4e2a\u6587\u4ef6\u63a5\u53e3\u8fd8\u6ca1\u6709\u89e3\u6790\u6210\u53ef\u76f4\u63a5\u6253\u5f00\u7684\u8def\u5f84\u3002"
        } else {
            "This browser file handle has not been resolved to an openable path."
        }
        reference.sourceKind == FileSourceKind.ManualPath -> if (locale == AppLocale.ZhCn) {
            "\u4fdd\u5b58\u7684\u6587\u4ef6\u8def\u5f84\u627e\u4e0d\u5230\u6587\u4ef6\u3002"
        } else {
            "The saved file path could not be found."
        }
        reference.sourceKind == FileSourceKind.Url -> if (locale == AppLocale.ZhCn) {
            "\u8fd9\u4e2a\u7f51\u5740\u65e0\u6cd5\u6253\u5f00\u3002"
        } else {
            "This URL could not be opened."
        }
        else -> if (locale == AppLocale.ZhCn) {
            "\u8fd9\u4e2a\u6765\u6e90\u7c7b\u578b\u4e0d\u652f\u6301\u76f4\u63a5\u6253\u5f00\u3002"
        } else {
            "This source type cannot be opened directly."
        }
    }

private fun webFileReselectionMessage(locale: AppLocale): String =
    if (locale == AppLocale.ZhCn) {
        "Web \u7248\u65e0\u6cd5\u76f4\u63a5\u8bbf\u95ee\u7535\u8111\u4e2d\u7684\u539f\u59cb\u8def\u5f84\uff0c\u8bf7\u91cd\u65b0\u9009\u62e9\u8be5\u6587\u4ef6\u4ee5\u6253\u5f00\u3002"
    } else {
        "Web cannot access the original local path directly. Reselect the file to open it."
    }

private fun summarizeReferenceSource(
    reference: FileReference,
    locale: AppLocale,
    fullCjkFontReady: Boolean,
): String {
    val source = reference.source.trim()
    if (source.isBlank()) return ""
    return when (reference.sourceKind) {
        FileSourceKind.BrowserHandle,
        -> if (locale == AppLocale.ZhCn) {
            "\u6d4f\u89c8\u5668\u5df2\u9009\u6587\u4ef6\uff1a${displayTextForUi(reference.title, fullCjkFontReady)}"
        } else {
            "Browser-selected file: ${displayTextForUi(reference.title, fullCjkFontReady)}"
        }

        else -> displayTextForUi(source, fullCjkFontReady)
    }
}

private fun thumbnailStatusLabel(reference: FileReference, locale: AppLocale): String =
    when (reference.thumbnailStatus) {
        ThumbnailStatus.READY -> if (locale == AppLocale.ZhCn) "\u5df2\u751f\u6210" else "Ready"
        ThumbnailStatus.FAILED -> if (locale == AppLocale.ZhCn) "\u751f\u6210\u5931\u8d25" else "Failed"
        ThumbnailStatus.UNSUPPORTED -> if (locale == AppLocale.ZhCn) "\u5f53\u524d\u4e0d\u652f\u6301" else "Unsupported"
        ThumbnailStatus.GENERATING -> if (locale == AppLocale.ZhCn) "\u751f\u6210\u4e2d" else "Generating"
        ThumbnailStatus.NONE -> if (locale == AppLocale.ZhCn) "\u672a\u751f\u6210" else "Not generated"
    }

private fun webDraftWeaklyMatches(
    existing: FileReference,
    replacement: BrowserReferenceDraft,
): Boolean {
    val normalizedTitle = existing.title.trim()
    val replacementTitle = replacement.title.trim()
    if (normalizedTitle.isNotBlank() && replacementTitle.isNotBlank() && normalizedTitle != replacementTitle) {
        return false
    }

    val existingSize = existing.fileSizeBytes ?: guessFileSizeFromNotes(existing.notes)
    val replacementSize = replacement.fileSizeBytes ?: guessFileSizeFromNotes(replacement.notes)
    if (existingSize != null && replacementSize != null && existingSize != replacementSize) {
        return false
    }

    val existingType = existing.fileType.trim()
    val replacementType = replacement.fileType.trim()
    if (existingType.isNotBlank() && replacementType.isNotBlank() && existingType != replacementType) {
        return false
    }

    return true
}

private fun clearDraftFields(appState: FileManagerAppState) {
    appState.draftTitle = ""
    appState.draftSource = ""
    appState.draftType = ""
    appState.draftFileSizeBytes = null
    appState.draftCoverArtSource = ""
    appState.draftTags = ""
    appState.draftNotes = ""
}

private fun appColorScheme() = lightColorScheme(
    primary = Color(0xFF2563EB),
    secondary = Color(0xFF0F766E),
    tertiary = Color(0xFF16A34A),
    background = Color.White,
    surface = Color.White,
    surfaceVariant = Color(0xFFF1F5F9),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1F2937),
    onSurface = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFF64748B),
)

private fun appBackgroundBrush(): Brush = Brush.verticalGradient(
    colors = listOf(
        Color.White,
        Color(0xFFFCFDFF),
        Color(0xFFF8FAFC),
    ),
)

private fun openReferenceWithRefresh(
    appState: FileManagerAppState,
    coroutineScope: CoroutineScope,
    reference: FileReference,
) {
    appState.openReference(reference.id)
    if (reference.source.startsWith("browser-", ignoreCase = true)) {
        coroutineScope.launch {
            appState.refreshReference(reference.id)
        }
    }
}


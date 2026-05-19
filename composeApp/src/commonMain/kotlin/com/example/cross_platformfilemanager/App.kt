package com.example.cross_platformfilemanager

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
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

private enum class FileSortMode {
    RecentAdded,
    Name,
    Type,
    Tags,
    RecentOpened,
}

@Composable
@Preview
fun App() {
    val snapshotStore = remember { createAppSnapshotStore() }
    val browserReferencePicker = remember { createBrowserReferencePicker() }
    val browserReferenceResolver = remember { createBrowserReferenceResolver() }
    val appState = remember(browserReferenceResolver) {
        FileManagerAppState(browserReferenceResolver = browserReferenceResolver)
    }
    val coroutineScope = rememberCoroutineScope()
    val pageScrollState = rememberScrollState()
    val fontLoadState = rememberAppFontLoadState()
    val appStartMillis = remember { nowMillis() }

    var currentPage by remember { mutableStateOf(AppPage.Home) }
    var searchDraft by remember { mutableStateOf("") }
    var sortMode by remember { mutableStateOf(FileSortMode.RecentAdded) }
    var showManualAddDialog by remember { mutableStateOf(false) }
    var manualAddNotice by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showSideMenu by remember { mutableStateOf(false) }
    var snapshotReady by remember { mutableStateOf(false) }

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
        val trimmedQuery = searchDraft.trim()
        if (trimmedQuery.isBlank()) {
            return
        }
        appState.query = trimmedQuery
        appState.selectedTag = null
        appState.selectedFileType = null
        appState.favoritesOnly = false
        appState.commitSearch()
        currentPage = AppPage.Search
    }

    fun startSearchFromSearchPage() {
        val trimmedQuery = searchDraft.trim()
        appState.query = trimmedQuery
        appState.selectedTag = null
        appState.selectedFileType = null
        appState.favoritesOnly = false
        if (trimmedQuery.isNotBlank()) {
            appState.commitSearch()
        } else {
            appState.snapshotVersion++
        }
        currentPage = AppPage.Search
    }

    fun addPickedReference(draft: BrowserReferenceDraft) {
        appState.applyBrowserDraft(draft)
        appState.addDraftReference()
        clearDraftFields(appState)
        currentPage = AppPage.Home
    }

    fun showAddReferenceDialog(notice: String? = null) {
        clearDraftFields(appState)
        manualAddNotice = notice
        showManualAddDialog = true
    }

    @Composable
    fun AppMainSurface() {
        val fullCjkFontReady = true
        val displayLocale = appState.locale
        val fontFamily = rememberAppFontFamily()
        val appTextStyle = LocalTextStyle.current.copy(fontFamily = fontFamily)
        CompositionLocalProvider(LocalTextStyle provides appTextStyle) {
            MaterialTheme(
                colorScheme = appColorScheme(),
                typography = appTypography(fontFamily),
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
                                                        if (draft == null) {
                                                            showAddReferenceDialog(browserPickerUnavailableMessage(displayLocale))
                                                        } else {
                                                            addPickedReference(draft)
                                                        }
                                                    }
                                                    .onFailure {
                                                        showAddReferenceDialog(browserPickerUnavailableMessage(displayLocale))
                                                    }
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
                                    sortMode = sortMode,
                                    onSortModeChange = { sortMode = it },
                                    onBackHome = { currentPage = AppPage.Home },
                                    onOpenReference = ::openReference,
                                )

                                AppPage.Detail -> DetailPage(
                                    appState = appState,
                                    locale = displayLocale,
                                    fullCjkFontReady = fullCjkFontReady,
                                    reference = appState.activeReference,
                                    onBackHome = { currentPage = AppPage.Home },
                                    onDeleteReference = { showDeleteConfirm = true },
                                )

                                AppPage.Search -> SearchResultsPage(
                                    appState = appState,
                                    locale = displayLocale,
                                    fullCjkFontReady = fullCjkFontReady,
                                    searchDraft = searchDraft,
                                    onSearchDraftChange = { searchDraft = it },
                                    onSearch = ::startSearchFromSearchPage,
                                    onBackHome = { currentPage = AppPage.Home },
                                    onOpenReference = ::openReference,
                                    onTagClick = { tag ->
                                        appState.selectedTag = if (appState.selectedTag == tag) null else tag
                                        appState.selectedFileType = null
                                        appState.favoritesOnly = false
                                        appState.commitSearch()
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
                                    locale = displayLocale,
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
                                notice = manualAddNotice,
                                onDismiss = {
                                    manualAddNotice = null
                                    showManualAddDialog = false
                                },
                                onConfirm = {
                                    appState.addDraftReference()
                                    clearDraftFields(appState)
                                    manualAddNotice = null
                                    showManualAddDialog = false
                                    currentPage = AppPage.Home
                                },
                            )
                        }

                        if (showDeleteConfirm && appState.activeReference != null) {
                            val reference = appState.activeReference
                            AlertDialog(
                                onDismissRequest = { showDeleteConfirm = false },
                                title = { Text(if (displayLocale == AppLocale.ZhCn) "\u5220\u9664\u6587\u4ef6" else "Delete file") },
                                text = { Text(displayTextForUi(reference?.title.orEmpty(), true)) },
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
    ) {
        AppMainSurface()
    }
}

@Composable
private fun HomePage(
    appState: FileManagerAppState,
    locale: AppLocale,
    fullCjkFontReady: Boolean,
    searchDraft: String,
    onSearchDraftChange: (String) -> Unit,
    onOpenMenu: () -> Unit,
    onPickFile: () -> Unit,
    onOpenAllFiles: () -> Unit,
    onSearch: () -> Unit,
    onOpenReference: (FileReference) -> Unit,
) {
    val recommendedReferences = resolveRecommendedReferences(
        recommendationState = appState,
        fallbackReferences = appState.recentReferences,
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TopBarCard(
            locale = locale,
            onMenuClick = onOpenMenu,
            title = if (locale == AppLocale.ZhCn) "\u6587\u4ef6\u7ba1\u7406\u5668" else "File Atlas",
            trailing = {
                SearchBarInline(
                    query = searchDraft,
                    onQueryChange = onSearchDraftChange,
                    onSearch = onSearch,
                    placeholder = appState.searchPlaceholder,
                    buttonLabel = if (locale == AppLocale.ZhCn) "\u641c\u7d22" else "Search",
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
    sortMode: FileSortMode,
    onSortModeChange: (FileSortMode) -> Unit,
    onBackHome: () -> Unit,
    onOpenReference: (FileReference) -> Unit,
) {
    val sortedFiles = remember(appState.allReferences, sortMode) {
        sortReferences(appState.allReferences, sortMode)
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TopBarCard(
            locale = locale,
            title = if (locale == AppLocale.ZhCn) "\u5168\u90e8\u6587\u4ef6" else "All files",
            onBack = onBackHome,
        )

        SectionCard(
            title = if (locale == AppLocale.ZhCn) "\u6392\u5e8f\u6761\u4ef6" else "Sort by",
            subtitle = if (locale == AppLocale.ZhCn) {
                "\u5207\u6362\u6392\u5e8f\u65b9\u5f0f\u540e\uff0c\u4e0b\u9762\u7684\u7f51\u683c\u4f1a\u7acb\u5373\u66f4\u65b0\u3002"
            } else {
                "Switch the order and the grid updates immediately."
            },
        ) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SortChip(
                    label = if (locale == AppLocale.ZhCn) "\u6700\u8fd1\u6dfb\u52a0" else "Recent added",
                    selected = sortMode == FileSortMode.RecentAdded,
                    onClick = { onSortModeChange(FileSortMode.RecentAdded) },
                )
                SortChip(
                    label = if (locale == AppLocale.ZhCn) "\u6587\u4ef6\u540d" else "Name",
                    selected = sortMode == FileSortMode.Name,
                    onClick = { onSortModeChange(FileSortMode.Name) },
                )
                SortChip(
                    label = if (locale == AppLocale.ZhCn) "\u6587\u4ef6\u7c7b\u578b" else "Type",
                    selected = sortMode == FileSortMode.Type,
                    onClick = { onSortModeChange(FileSortMode.Type) },
                )
                SortChip(
                    label = if (locale == AppLocale.ZhCn) "\u6807\u7b7e" else "Tags",
                    selected = sortMode == FileSortMode.Tags,
                    onClick = { onSortModeChange(FileSortMode.Tags) },
                )
            }
        }

        if (sortedFiles.isEmpty()) {
            EmptyPanel(
                title = if (locale == AppLocale.ZhCn) "\u6682\u65e0\u6587\u4ef6" else "No files uploaded yet",
                body = if (locale == AppLocale.ZhCn) {
                    "\u5148\u9009\u62e9\u4e00\u4e2a\u6587\u4ef6\uff0c\u5217\u8868\u5c31\u4f1a\u51fa\u73b0\u3002"
                } else {
                    "Pick a file first and it will show up here."
                },
            )
        } else {
            AdaptiveFileGrid(
                items = sortedFiles,
                fullCjkFontReady = fullCjkFontReady,
                onOpen = onOpenReference,
            )
        }
    }
}

@Composable
private fun SearchResultsPage(
    appState: FileManagerAppState,
    locale: AppLocale,
    fullCjkFontReady: Boolean,
    searchDraft: String,
    onSearchDraftChange: (String) -> Unit,
    onSearch: () -> Unit,
    onBackHome: () -> Unit,
    onOpenReference: (FileReference) -> Unit,
    onTagClick: (String) -> Unit,
) {
    val activeTag = appState.selectedTag
    val showSearchPrompt = appState.query.isBlank() &&
        activeTag == null &&
        appState.selectedFileType == null &&
        !appState.favoritesOnly
    val searchResults = if (showSearchPrompt) emptyList() else appState.searchResults
    val resultStatus = if (appState.query.isNotBlank()) {
        if (locale == AppLocale.ZhCn) {
            "\u201c${displayTextForUi(appState.query, fullCjkFontReady)}\u201d\u7684\u641c\u7d22\u7ed3\u679c \u00b7 ${searchResults.size} \u4e2a"
        } else {
            "\"${displayTextForUi(appState.query, fullCjkFontReady)}\" search results \u00b7 ${searchResults.size}"
        }
    } else {
        if (showSearchPrompt) {
            if (locale == AppLocale.ZhCn) {
                "\u8f93\u5165\u5173\u952e\u5b57\u540e\u518d\u641c\u7d22"
            } else {
                "Enter a keyword to search"
            }
        } else {
            if (locale == AppLocale.ZhCn) {
                "\u641c\u7d22\u7ed3\u679c \u00b7 ${searchResults.size} \u4e2a"
            } else {
                "Search results \u00b7 ${searchResults.size}"
            }
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
        )

        SearchFilterSection(
            title = appState.tagLibraryTitle,
            subtitle = appState.tagLibrarySubtitle,
            tags = appState.allTags,
            selectedTag = activeTag,
            emptyHint = appState.tagLibraryEmpty,
            fullCjkFontReady = fullCjkFontReady,
            onTagClick = onTagClick,
        )

        Text(
            text = resultStatus,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 4.dp),
        )

        if (showSearchPrompt) {
            SearchEmptyState(
                title = if (locale == AppLocale.ZhCn) "\u8fd8\u6ca1\u6709\u8f93\u5165\u641c\u7d22\u5173\u952e\u5b57" else "No search keyword yet",
                body = if (locale == AppLocale.ZhCn) {
                    "\u8f93\u5165\u4e00\u4e2a\u5173\u952e\u5b57\u540e\u518d\u70b9\u641c\u7d22\u3002"
                } else {
                    "Type a keyword first, then press Search."
                },
            )
        } else if (searchResults.isEmpty()) {
            SearchEmptyState(
                title = appState.emptyResultsTitle,
                body = appState.emptyResultsBody,
            )
        } else {
            AdaptiveFileGrid(
                items = searchResults.map { it.reference },
                fullCjkFontReady = fullCjkFontReady,
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
    selectedTag: String?,
    emptyHint: String,
    fullCjkFontReady: Boolean,
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
                            selected = selectedTag == tag,
                            fullCjkFontReady = fullCjkFontReady,
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

@Composable
private fun TagPill(tag: String, fullCjkFontReady: Boolean) {
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
        )
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
    locale: AppLocale,
    fullCjkFontReady: Boolean,
    reference: FileReference?,
    onBackHome: () -> Unit,
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
    var showRemoveTagDialog by remember(reference.id) { mutableStateOf(false) }
    var newTagDraft by remember(reference.id) { mutableStateOf("") }
    var openFileMessage by remember(reference.id) { mutableStateOf<String?>(null) }

    LaunchedEffect(reference.id, reference.source, reference.sourceKind) {
        // 鏂囦欢鏉ユ簮涓€鏃﹀彉鍖栵紝灏辨妸涓婁竴娆＄殑鎵撳紑澶辫触鎻愮ず娓呮帀锛岄伩鍏嶆棫鐘舵€佽瀵肩敤鎴枫€?        openFileMessage = null
    }

    fun handleOpenFile() {
        coroutineScope.launch {
            val needsRefresh = reference.sourceKind == FileSourceKind.BrowserHandle && reference.source.startsWith("browser-", ignoreCase = true)
            val resolvedReference = if (needsRefresh) {
                // 娴忚鍣ㄥ彞鏌勫厛灏介噺鍒锋柊鎴愮湡瀹炶矾寰勶紝鍐嶄氦缁欑郴缁熸墦寮€锛岃繖鏍锋寜閽笉浼氱偣浜嗘病鍔ㄩ潤銆?                appState.refreshReference(reference.id)
                appState.activeReference ?: reference
            } else {
                reference
            }

            val openableReference = appState.activeReference?.takeIf { it.id == resolvedReference.id } ?: resolvedReference
            val opened = openReferenceExternally(openableReference)
            if (opened) {
                if (!(needsRefresh && openableReference.source.startsWith("browser-", ignoreCase = true))) {
                    appState.openReference(openableReference.id)
                }
                openFileMessage = null
            } else {
                openFileMessage = openFileFailureMessage(openableReference, locale)
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
            canOpenFile = reference.source.trim().isNotBlank(),
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
                    value = displayTextForUi(reference.source, fullCjkFontReady).ifBlank {
                        if (locale == AppLocale.ZhCn) "\u6682\u672a\u4fdd\u5b58\u8def\u5f84" else "No file path saved"
                    },
                )
                if (reference.coverArtSource?.isNotBlank() == true) {
                    InfoRow(
                        label = if (locale == AppLocale.ZhCn) "\u5c01\u9762\u6765\u6e90" else "Cover art source",
                        value = displayTextForUi(reference.coverArtSource.orEmpty(), fullCjkFontReady),
                    )
                }
                if (openFileMessage != null) {
                    Text(
                        text = openFileMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                    )
                } else if (!canOpenReferenceExternally(reference)) {
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
                "\u53f3\u4e0a\u89d2\u6309\u94ae\u53ea\u4f1a\u4ece\u672c\u6587\u4ef6\u79fb\u9664\u6807\u7b7e\uff0c\u4e0d\u4f1a\u5220\u9664\u6807\u7b7e\u672c\u8eab\u3002"
            } else {
                "Use the top-right button to remove a tag from this file without deleting the tag everywhere."
            },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        enabled = reference.tags.isNotEmpty(),
                        onClick = { showRemoveTagDialog = true },
                    ) {
                        Text(if (locale == AppLocale.ZhCn) "\u2212 \u79fb\u9664\u6807\u7b7e" else "\u2212 Remove tag")
                    }
                }

                if (reference.tags.isEmpty()) {
                    EmptyPanel(
                        title = if (locale == AppLocale.ZhCn) "\u6682\u65e0\u6807\u7b7e" else "No tags yet",
                        body = if (locale == AppLocale.ZhCn) {
                            "\u53ef\u4ee5\u5148\u70b9\u51fb\u53f3\u4fa7\u7684\u201c+\u65b0\u589e\u6807\u7b7e\u201d\u3002"
                        } else {
                            "Use the + add tag button to create one."
                        },
                    )
                } else {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        reference.tags.forEach { tag ->
                            TagPill(tag = tag, fullCjkFontReady = fullCjkFontReady)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = { showTagDialog = true }) {
                        Text(if (locale == AppLocale.ZhCn) "+ \u65b0\u589e\u6807\u7b7e" else "+ Add tag")
                    }
                }
            }
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onDeleteReference,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFDC2626),
                contentColor = Color.White,
            ),
        ) {
            Text(if (locale == AppLocale.ZhCn) "\u5220\u9664\u6b64\u6587\u4ef6" else "Delete this file")
        }
    }

    if (showRemoveTagDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveTagDialog = false },
            title = { Text(if (locale == AppLocale.ZhCn) "\u79fb\u9664\u6807\u7b7e" else "Remove tag") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = if (locale == AppLocale.ZhCn) {
                            "\u9009\u4e00\u4e2a\u6807\u7b7e\uff0c\u53ea\u4ece\u5f53\u524d\u6587\u4ef6\u79fb\u9664\u3002"
                        } else {
                            "Pick a tag to remove it from this file only."
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        reference.tags.forEach { tag ->
                            // Removing a tag only affects this file, not the global tag store.
                            AssistChip(
                                onClick = {
                                    appState.updateReferenceTags(
                                        reference.id,
                                        removeTag(reference.tags, tag),
                                    )
                                    showRemoveTagDialog = false
                                },
                                label = { Text(displayTextForUi(tag, fullCjkFontReady)) },
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRemoveTagDialog = false }) {
                    Text(if (locale == AppLocale.ZhCn) "\u5b8c\u6210" else "Done")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveTagDialog = false }) {
                    Text(if (locale == AppLocale.ZhCn) "\u53d6\u6d88" else "Cancel")
                }
            },
        )
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
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = newTagDraft,
                        onValueChange = { newTagDraft = it },
                        label = { Text(if (locale == AppLocale.ZhCn) "\u8f93\u5165\u6807\u7b7e" else "Tag name") },
                        singleLine = true,
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (locale == AppLocale.ZhCn) "\u5df2\u6709\u6807\u7b7e" else "Existing tags",
                            fontWeight = FontWeight.Medium,
                        )
                        if (reference.tags.isEmpty()) {
                            Text(
                                text = if (locale == AppLocale.ZhCn) "\u6682\u65e0\u6807\u7b7e" else "No tags yet",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                            )
                        } else {
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                reference.tags.forEach { tag ->
                                    AssistChip(
                                        onClick = { newTagDraft = displayTextForUi(tag, fullCjkFontReady) },
                                        label = { Text(displayTextForUi(tag, fullCjkFontReady)) },
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
                        val cleaned = newTagDraft.trim()
                        if (cleaned.isNotBlank()) {
                            appState.updateReferenceTags(
                                reference.id,
                                mergeTags(reference.tags, cleaned),
                            )
                        }
                        newTagDraft = ""
                        showTagDialog = false
                    },
                ) {
                    Text(if (locale == AppLocale.ZhCn) "\u786e\u5b9a\u65b0\u589e" else "Add")
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
}

@Composable
private fun TopBarCard(
    locale: AppLocale,
    title: String,
    onMenuClick: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
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
    OutlinedButton(onClick = onClick) {
        Text(if (locale == AppLocale.ZhCn) "EN" else "\u4e2d\u6587")
    }
}

@Composable
private fun SideMenuPanel(
    locale: AppLocale,
    onClose: () -> Unit,
    onToggleLanguage: () -> Unit,
) {
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

                LanguageToggleButton(
                    locale = locale,
                    onClick = onToggleLanguage,
                )
            }
        }
    }
}

@Composable
private fun SearchBarCard(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    placeholder: String,
    buttonLabel: String,
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
                )
                Button(onClick = onSearch) {
                    Text(buttonLabel)
                }
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
            )
        }
    }
}

@Composable
private fun DetailHeroCard(
    reference: FileReference,
    locale: AppLocale,
    fullCjkFontReady: Boolean,
    canOpenFile: Boolean,
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
                        )
                    }

                    OutlinedButton(
                        onClick = onOpenFile,
                        enabled = canOpenFile,
                    ) {
                        Text(if (locale == AppLocale.ZhCn) "\u2197 \u6253\u5f00\u6b64\u6587\u4ef6" else "\u2197 Open this file")
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
    modifier: Modifier = Modifier,
    cornerShape: RoundedCornerShape = RoundedCornerShape(16.dp),
    iconSize: androidx.compose.ui.unit.Dp = 24.dp,
) {
    val hasCoverArt = reference.coverArtSource?.isNotBlank() == true
    Box(
        modifier = modifier
            .clip(cornerShape)
            .background(iconStyle.backgroundBrush),
        contentAlignment = Alignment.Center,
    ) {
        if (hasCoverArt) {
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
                        text = "Reserved",
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
    notice: String?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (locale == AppLocale.ZhCn) "\u6dfb\u52a0\u6587\u4ef6" else "Add file") },
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
                    value = appState.draftTitle,
                    onValueChange = { appState.draftTitle = it },
                    label = { Text(appState.referenceTitle) },
                    singleLine = true,
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = appState.draftSource,
                    onValueChange = { appState.draftSource = it },
                    label = { Text(appState.referenceLocation) },
                    singleLine = true,
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = appState.draftType,
                    onValueChange = { appState.draftType = it },
                    label = { Text(appState.fileType) },
                    singleLine = true,
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = appState.draftCoverArtSource,
                    onValueChange = { appState.draftCoverArtSource = it },
                    label = { Text(if (locale == AppLocale.ZhCn) "\u5c01\u9762\u6765\u6e90" else "Cover art source") },
                    singleLine = true,
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = appState.draftTags,
                    onValueChange = { appState.draftTags = it },
                    label = { Text(appState.tagsCommaSeparated) },
                    singleLine = true,
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = appState.draftNotes,
                    onValueChange = { appState.draftNotes = it },
                    label = { Text(appState.notes) },
                    minLines = 2,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(appState.addReference)
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

private fun sortReferences(references: List<FileReference>, sortMode: FileSortMode): List<FileReference> =
    when (sortMode) {
        FileSortMode.RecentAdded -> references.sortedByDescending { it.createdAtMillis }
        FileSortMode.Name -> references.sortedWith(compareBy<FileReference> { normalize(it.title) }.thenByDescending { it.createdAtMillis })
        FileSortMode.Type -> references.sortedWith(compareBy<FileReference> { normalize(it.fileType) }.thenBy { normalize(it.title) })
        FileSortMode.Tags -> references.sortedWith(compareBy<FileReference> { normalize(it.tags.joinToString(", ")) }.thenBy { normalize(it.title) })
        FileSortMode.RecentOpened -> references.sortedByDescending { it.lastOpenedAtMillis }
    }

private fun sortModeLabel(mode: FileSortMode, locale: AppLocale): String =
    when (mode) {
        FileSortMode.RecentAdded -> if (locale == AppLocale.ZhCn) "\u6700\u8fd1\u6dfb\u52a0" else "Recent added"
        FileSortMode.Name -> if (locale == AppLocale.ZhCn) "\u6587\u4ef6\u540d" else "Name"
        FileSortMode.Type -> if (locale == AppLocale.ZhCn) "\u6587\u4ef6\u7c7b\u578b" else "Type"
        FileSortMode.Tags -> if (locale == AppLocale.ZhCn) "\u6807\u7b7e" else "Tags"
        FileSortMode.RecentOpened -> if (locale == AppLocale.ZhCn) "\u6700\u8fd1\u6253\u5f00" else "Recent opened"
    }

private fun resolveRecommendedReferences(
    recommendationState: RecommendationReadOnlyState,
    fallbackReferences: List<FileReference>,
): List<FileReference> {
    val recommended = recommendationState.recommendedReferences.take(10)
    if (recommended.isEmpty()) {
        return fallbackReferences.take(10)
    }

    val seenIds = linkedSetOf<String>()
    val merged = mutableListOf<FileReference>()

    recommended.forEach { reference ->
        if (seenIds.add(reference.id)) {
            merged += reference
        }
    }

    fallbackReferences.forEach { reference ->
        if (merged.size >= 10) return@forEach
        if (seenIds.add(reference.id)) {
            merged += reference
        }
    }

    return merged.take(10)
}

private fun mergeTags(existingTags: List<String>, newTag: String): String {
    val cleaned = newTag.trim()
    if (cleaned.isBlank()) return existingTags.joinToString(", ")

    val merged = existingTags.toMutableList()
    if (merged.none { normalize(it) == normalize(cleaned) }) {
        merged += cleaned
    }
    return merged.joinToString(", ")
}

private fun removeTag(existingTags: List<String>, tag: String): String =
    existingTags
        .filterNot { normalize(it) == normalize(tag) }
        .joinToString(", ")

private fun canOpenReferenceExternally(reference: FileReference): Boolean {
    val source = reference.source.trim()
    if (source.isBlank()) return false
    return when (reference.sourceKind) {
        FileSourceKind.ManualPath, FileSourceKind.Url -> true
        else -> false
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
    background = Color(0xFFF4F6FA),
    surface = Color.White,
    surfaceVariant = Color(0xFFE8EEF7),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1F2937),
    onSurface = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFF64748B),
)

private fun appBackgroundBrush(): Brush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFF8FAFC),
        Color(0xFFF1F5F9),
        Color(0xFFEFF4FB),
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


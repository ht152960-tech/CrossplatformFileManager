package com.example.cross_platformfilemanager

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.LocalOffer
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
import com.example.cross_platformfilemanager.ui.adaptive.AdaptiveAppScaffold
import com.example.cross_platformfilemanager.ui.adaptive.AdaptiveNavigationItem
import com.example.cross_platformfilemanager.ui.adaptive.LocalTaggoWindowSizeClass
import com.example.cross_platformfilemanager.ui.adaptive.SidebarNavigationItem
import com.example.cross_platformfilemanager.ui.adaptive.TaggoWindowSizeClass
import com.example.cross_platformfilemanager.ui.theme.TaggoTheme
import com.example.cross_platformfilemanager.ui.theme.ProvideTaggoTheme
import com.example.cross_platformfilemanager.ui.components.EmptyPanel
import com.example.cross_platformfilemanager.ui.components.FileCoverArtFrame
import com.example.cross_platformfilemanager.ui.components.InfoRow
import com.example.cross_platformfilemanager.ui.components.RemovableTagChip
import com.example.cross_platformfilemanager.ui.components.SearchEmptyState
import com.example.cross_platformfilemanager.ui.components.SearchTagChip
import com.example.cross_platformfilemanager.ui.components.SectionCard
import com.example.cross_platformfilemanager.ui.components.SortChip
import com.example.cross_platformfilemanager.ui.components.TagFilterChip
import com.example.cross_platformfilemanager.ui.components.TagPill
import com.example.cross_platformfilemanager.ui.components.fileTypeIconStyle

private enum class AppPage {
    Home,
    Tags,
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
/**
 * 应用根入口。
 *
 * 这里负责创建跨页面共享的状态对象、恢复快照、接入启动门面，
 * 并把主要页面之间的导航和通用操作入口组织起来。
 */
fun App() {
    val snapshotStore = remember { createAppSnapshotStore() }
    val browserReferencePicker = remember { createBrowserReferencePicker() }
    val browserReferenceResolver = remember { createBrowserReferenceResolver() }
    val thumbnailGenerator = remember { createThumbnailGenerator() }
    val uiFontFamily = rememberAppFontFamily()
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
    var selectedNavigationPage by remember { mutableStateOf(AppPage.Home) }
    var detailBackTarget by remember { mutableStateOf<AppPage?>(AppPage.Home) }
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
        reportStartupTimeline("App first composition")
        reportStartupTrace("App composition entered")
    }

    LaunchedEffect(snapshotStore, browserReferenceResolver) {
        val startedAt = nowMillis()
        reportStartupTimeline("snapshot load launched")
        reportStartupTimeline("snapshot load start")
        reportStartupTrace("snapshot load start +${startedAt - appStartMillis}ms")
        try {
            snapshotStore?.load()?.let(appState::restoreSnapshot)
            if (browserReferenceResolver != null) {
                appState.refreshBrowserReferences()
            }
        } finally {
            snapshotReady = true
            reportStartupTimeline("snapshot load end")
            reportStartupTrace("snapshot load end +${nowMillis() - appStartMillis}ms")
        }
    }

    LaunchedEffect(appState.snapshotVersion, snapshotStore, snapshotReady) {
        if (snapshotReady) {
            snapshotStore?.save(appState.exportSnapshot())
        }
    }

    LaunchedEffect(currentPage) {
        if (currentPage == AppPage.Home || currentPage == AppPage.Tags || currentPage == AppPage.AllFiles) {
            selectedNavigationPage = currentPage
        }
    }

    fun openReference(reference: FileReference) {
        if (currentPage != AppPage.Detail) {
            detailBackTarget = currentPage
        }
        // If the same file is opened again from the detail page, do not count it twice.
        if (appState.activeReference?.id == reference.id) {
            currentPage = AppPage.Detail
            return
        }
        // 无论入口来自最近新增、推荐列表还是搜索结果，真正的打开行为都统一走应用状态层，
        // 这样文件条目状态和推荐学习信号才能在同一个入口里保持一致。
        openReferenceWithRefresh(appState, coroutineScope, reference)
        currentPage = AppPage.Detail
    }

    fun navigateBackFromDetail() {
        val target = detailBackTarget
        currentPage = when (target) {
            AppPage.Home,
            AppPage.Tags,
            AppPage.AllFiles,
            AppPage.Search,
            -> target

            else -> AppPage.Home
        }
        detailBackTarget = currentPage
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

    fun openTagSearch(tag: String) {
        appState.resetSearchSession()
        appState.addSearchTag(SearchTag(value = tag, source = SearchTagSource.LibraryTag))
        searchFeedbackMessage = null
        currentPage = AppPage.Search
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
        val fullCjkFontFamily = rememberFullCjkFontFamily()
        val appTextStyle = LocalTextStyle.current.copy(fontFamily = uiFontFamily)
        val fullCjkTextStyle = LocalTextStyle.current.copy(fontFamily = fullCjkFontFamily)
        val navigationItems = remember(displayLocale) {
            listOf(
                AdaptiveNavigationItem(
                    page = AppPage.Home,
                    label = if (displayLocale == AppLocale.ZhCn) "\u63a8\u8350" else "Recommended",
                    icon = Icons.Outlined.Home,
                ),
                AdaptiveNavigationItem(
                    page = AppPage.Tags,
                    label = if (displayLocale == AppLocale.ZhCn) "\u6807\u7b7e" else "Tags",
                    icon = Icons.Outlined.LocalOffer,
                ),
                AdaptiveNavigationItem(
                    page = AppPage.AllFiles,
                    label = if (displayLocale == AppLocale.ZhCn) "\u6587\u4ef6" else "Files",
                    icon = Icons.Outlined.Folder,
                ),
            )
        }
        CompositionLocalProvider(LocalTextStyle provides appTextStyle) {
            ProvideTaggoTheme(uiFontFamily = uiFontFamily) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AdaptiveAppScaffold(
                        currentPage = currentPage,
                        selectedNavigationPage = selectedNavigationPage,
                        navigationItems = navigationItems,
                        onPageSelected = { currentPage = it },
                        sideBarFooter = { windowSizeClass ->
                            if (windowSizeClass == TaggoWindowSizeClass.Expanded) {
                                SidebarNavigationItem(
                                    item = AdaptiveNavigationItem(
                                        page = currentPage,
                                        label = if (displayLocale == AppLocale.ZhCn) "\u8bbe\u7f6e" else "Settings",
                                        icon = Icons.Outlined.Menu,
                                    ),
                                    selected = false,
                                    onClick = { showSideMenu = true },
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(TaggoTheme.colors.backgroundBrush),
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
                                        onSearch = ::startSearchFromHome,
                                        onOpenReference = ::openReference,
                                    )

                                    AppPage.Tags -> TagsPage(
                                        appState = appState,
                                        locale = displayLocale,
                                        fullCjkFontReady = fullCjkFontReady,
                                        fullCjkFontFamily = fullCjkFontFamily,
                                        onTagClick = ::openTagSearch,
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
                                        onBackHome = ::navigateBackFromDetail,
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
                                        onBackHome = { currentPage = selectedNavigationPage },
                                        onOpenReference = ::openReference,
                                        onTagClick = ::openTagSearch,
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
                                    containerColor = TaggoTheme.colors.surfaceElevated,
                                    title = {
                                        Text(
                                            text = if (displayLocale == AppLocale.ZhCn) "\u5220\u9664\u6587\u4ef6" else "Delete file",
                                            color = TaggoTheme.colors.textPrimary,
                                        )
                                    },
                                    text = {
                                        Text(
                                            text = displayTextForUi(reference?.title.orEmpty(), true),
                                            color = TaggoTheme.colors.textSecondary,
                                            fontFamily = fullCjkFontFamily,
                                        )
                                    },
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
                                                color = TaggoTheme.colors.danger,
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
    }
    ProvideTaggoTheme(uiFontFamily = uiFontFamily) {
        AppStartupGate(
            snapshotReady = snapshotReady,
            fontLoadState = fontLoadState,
            fullCjkFontLoadState = fullCjkFontLoadState,
        ) {
            AppMainSurface()
        }
    }
}

@Composable
/**
 * 首页。
 *
 * 首页主要展示最近新增、推荐文件和全局搜索入口，
 * 不直接参与推荐计算，只消费应用状态层已经准备好的结果。
 */
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
    onSearch: () -> Unit,
    onOpenReference: (FileReference) -> Unit,
) {
    LaunchedEffect(Unit) {
        reportStartupTimeline("Home first composition")
    }

    // 首页推荐面板只消费只读推荐结果，不在 UI 层直接拼装推荐算法输入。
    val recommendedReferences = resolveRecommendedReferences(appState)
    val windowSizeClass = LocalTaggoWindowSizeClass.current
    val compactLayout = windowSizeClass == TaggoWindowSizeClass.Compact
    val expandedLayout = windowSizeClass == TaggoWindowSizeClass.Expanded
    val homeSpacing = if (compactLayout) 10.dp else 14.dp

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        LaunchedEffect(appState.allReferences.size, recommendedReferences.size) {
            reportStartupTimeline(
                "Home key content visible files=${appState.allReferences.size} recommended=${recommendedReferences.size}"
            )
        }
        Column(
            modifier = expandedPageContentModifier()
                .then(if (compactLayout) Modifier.padding(horizontal = 4.dp) else Modifier),
            verticalArrangement = Arrangement.spacedBy(homeSpacing),
        ) {
            TopBarCard(
                locale = locale,
                onMenuClick = if (windowSizeClass == TaggoWindowSizeClass.Expanded) null else onOpenMenu,
                title = if (locale == AppLocale.ZhCn) "\u63a8\u8350" else "Recommended",
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

            if (expandedLayout) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
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
                            responsiveHeader = true,
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
                                    locale = locale,
                                    fullCjkFontReady = fullCjkFontReady,
                                    fullCjkFontFamily = fullCjkFontFamily,
                                    onOpen = onOpenReference,
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        SectionCard(
                            title = appState.recommendationTitle,
                            subtitle = appState.recommendationSubtitle,
                            responsiveHeader = true,
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
                                    locale = locale,
                                    fullCjkFontReady = fullCjkFontReady,
                                    fullCjkFontFamily = fullCjkFontFamily,
                                    onOpen = onOpenReference,
                                )
                            }
                        }
                    }
                }
            } else {
                SectionCard(
                    title = appState.recentlyAdded,
                    subtitle = if (locale == AppLocale.ZhCn) {
                        "\u70b9\u51fb\u4efb\u610f\u6761\u76ee\u8fdb\u5165\u8be6\u60c5\u9875\u3002"
                    } else {
                        "Open any item to view its details."
                    },
                    trailing = {
                        Button(
                            modifier = if (compactLayout) Modifier.fillMaxWidth() else Modifier,
                            onClick = onPickFile,
                        ) {
                            Text(if (locale == AppLocale.ZhCn) "\u4e0a\u4f20\u6587\u4ef6" else "Upload file")
                        }
                    },
                    responsiveHeader = true,
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
                            locale = locale,
                            fullCjkFontReady = fullCjkFontReady,
                            fullCjkFontFamily = fullCjkFontFamily,
                            onOpen = onOpenReference,
                        )
                    }
                }

                SectionCard(
                    title = appState.recommendationTitle,
                    subtitle = appState.recommendationSubtitle,
                    responsiveHeader = true,
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
                        // 推荐面板只展示推荐链路返回的前若干个文件条目；
                        // 用户点击其中任意条目时，会回到统一的打开入口写入推荐反馈上下文。
                        AdaptiveFileGrid(
                            items = recommendedReferences,
                            locale = locale,
                            fullCjkFontReady = fullCjkFontReady,
                            fullCjkFontFamily = fullCjkFontFamily,
                            onOpen = onOpenReference,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagsPage(
    appState: FileManagerAppState,
    locale: AppLocale,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    onTagClick: (String) -> Unit,
) {
    val windowSizeClass = LocalTaggoWindowSizeClass.current
    val compactLayout = windowSizeClass == TaggoWindowSizeClass.Compact
    val compactBottomPadding = if (compactLayout) 96.dp else 0.dp
    val allTags = appState.allTags
    var pendingDeleteTag by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = expandedPageContentModifier().padding(
                start = if (compactLayout) 4.dp else 0.dp,
                end = if (compactLayout) 4.dp else 0.dp,
                bottom = compactBottomPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            TopBarCard(
                locale = locale,
                title = if (locale == AppLocale.ZhCn) "\u6807\u7b7e" else "Tags",
            )

            SectionCard(
                title = if (locale == AppLocale.ZhCn) "\u6807\u7b7e\u7ba1\u7406" else "Tag management",
                subtitle = if (locale == AppLocale.ZhCn) {
                    "\u70b9\u51fb\u6807\u7b7e\u67e5\u770b\u6240\u6709\u5df2\u8d34\u4e0a\u8be5\u6807\u7b7e\u7684\u6587\u4ef6\uff0c\u70b9\u53f3\u4fa7\u6309\u94ae\u53ef\u4ece\u5168\u90e8\u6587\u4ef6\u4e2d\u5220\u9664\u6b64\u6807\u7b7e\u3002"
                } else {
                    "Tap a tag to find every file using it. Use the trailing action to remove that tag from the entire library."
                },
            ) {
                if (allTags.isEmpty()) {
                    EmptyPanel(
                        title = if (locale == AppLocale.ZhCn) "\u6682\u65e0\u6807\u7b7e" else "No tags yet",
                        body = if (locale == AppLocale.ZhCn) {
                            "\u5148\u4e3a\u6587\u4ef6\u6dfb\u52a0\u6807\u7b7e\uff0c\u8fd9\u91cc\u4f1a\u5c55\u793a\u5168\u90e8\u6807\u7b7e\u5e76\u652f\u6301\u7edf\u4e00\u7ba1\u7406\u3002"
                        } else {
                            "Add tags to files first. This page will then list them here for search and removal."
                        },
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        allTags.forEach { tag ->
                            RemovableTagChip(
                                tag = tag,
                                fullCjkFontReady = fullCjkFontReady,
                                fullCjkFontFamily = fullCjkFontFamily,
                                onClick = { onTagClick(tag) },
                                onRemove = { pendingDeleteTag = tag },
                                actionIcon = Icons.Outlined.Close,
                            )
                        }
                    }
                }
            }
        }
    }

    val deleteTarget = pendingDeleteTag
    if (deleteTarget != null) {
        val affectedFileCount = appState.allReferences.count { reference ->
            reference.tags.any { normalize(it) == normalize(deleteTarget) }
        }
        AlertDialog(
            onDismissRequest = { pendingDeleteTag = null },
            containerColor = TaggoTheme.colors.surfaceElevated,
            title = {
                Text(
                    text = if (locale == AppLocale.ZhCn) "\u5220\u9664\u6807\u7b7e" else "Delete tag",
                    color = TaggoTheme.colors.textPrimary,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        if (locale == AppLocale.ZhCn) {
                            "\u5220\u9664\u540e\u4f1a\u4ece\u6240\u6709\u6587\u4ef6\u4e2d\u79fb\u9664\u6b64\u6807\u7b7e\u3002"
                        } else {
                            "This will remove the tag from all files."
                        },
                        color = TaggoTheme.colors.textPrimary,
                    )
                    Text(
                        if (locale == AppLocale.ZhCn) {
                            "\u8be5\u6807\u7b7e\u5c06\u4ece $affectedFileCount \u4e2a\u6587\u4ef6\u4e2d\u79fb\u9664\u3002"
                        } else {
                            "This tag will be removed from $affectedFileCount files."
                        },
                        color = TaggoTheme.colors.textSecondary,
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
                    Text(
                        text = if (locale == AppLocale.ZhCn) "\u5220\u9664" else "Delete",
                        color = TaggoTheme.colors.danger,
                    )
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
    onOpenReference: (FileReference) -> Unit,
) {
    val windowSizeClass = LocalTaggoWindowSizeClass.current
    val compactLayout = windowSizeClass == TaggoWindowSizeClass.Compact
    val expandedLayout = windowSizeClass == TaggoWindowSizeClass.Expanded
    val compactBottomPadding = if (compactLayout) 96.dp else 0.dp
    val availableTypeFilters = remember(appState.allReferences) {
        resolveAvailableTypeFilters(appState.allReferences)
    }
    val filteredFiles = remember(appState.allReferences, typeFilter) {
        filterReferencesByType(appState.allReferences, typeFilter)
    }
    val sortedFiles = remember(filteredFiles, sortMode, sortDirection) {
        sortReferences(filteredFiles, sortMode, sortDirection)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = expandedPageContentModifier().padding(
                start = if (compactLayout) 4.dp else 0.dp,
                end = if (compactLayout) 4.dp else 0.dp,
                bottom = compactBottomPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            TopBarCard(
                locale = locale,
                title = if (locale == AppLocale.ZhCn) "\u6587\u4ef6" else "Files",
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
                if (expandedLayout) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            SectionCard(
                                title = if (locale == AppLocale.ZhCn) "\u6392\u5e8f" else "Sort",
                                subtitle = if (locale == AppLocale.ZhCn) {
                                    "\u518d\u70b9\u5f53\u524d\u6309\u94ae\u53ef\u5207\u6362\u5347\u964d\u5e8f\uff0c\u5207\u5230\u5176\u4ed6\u6392\u5e8f\u65f6\u4f1a\u6062\u590d\u8be5\u6392\u5e8f\u7684\u9ed8\u8ba4\u65b9\u5411\u3002"
                                } else {
                                    "Tap the current option again to flip the direction. Switching to another sort resets it to that mode's default direction."
                                },
                                responsiveHeader = true,
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
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            SectionCard(
                                title = if (locale == AppLocale.ZhCn) "\u7c7b\u578b" else "Type",
                                subtitle = if (locale == AppLocale.ZhCn) {
                                    "\u53ea\u663e\u793a\u5f53\u524d\u6587\u4ef6\u5e93\u91cc\u771f\u6b63\u5b58\u5728\u7684\u5927\u7c7b\uff0c\u4e0d\u91cd\u7f6e\u5f53\u524d\u6392\u5e8f\u3002"
                                } else {
                                    "Only categories that exist in the current library are shown, and switching type keeps the current sort."
                                },
                                responsiveHeader = true,
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
                        }
                    }
                } else {
                    SectionCard(
                        title = if (locale == AppLocale.ZhCn) "\u6392\u5e8f" else "Sort",
                        subtitle = if (locale == AppLocale.ZhCn) {
                            "\u518d\u70b9\u5f53\u524d\u6309\u94ae\u53ef\u5207\u6362\u5347\u964d\u5e8f\uff0c\u5207\u5230\u5176\u4ed6\u6392\u5e8f\u65f6\u4f1a\u6062\u590d\u8be5\u6392\u5e8f\u7684\u9ed8\u8ba4\u65b9\u5411\u3002"
                        } else {
                            "Tap the current option again to flip the direction. Switching to another sort resets it to that mode's default direction."
                        },
                        responsiveHeader = true,
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
                        responsiveHeader = true,
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
                        locale = locale,
                        fullCjkFontReady = fullCjkFontReady,
                        fullCjkFontFamily = fullCjkFontFamily,
                        onOpen = onOpenReference,
                    )
                }
            }
        }
    }
}

@Composable
/**
 * 搜索结果页。
 *
 * 页面负责接收搜索输入、展示当前搜索标签和结果列表，
 * 真正的搜索标签归一化与匹配逻辑由状态层和仓储层处理。
 */
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
    val windowSizeClass = LocalTaggoWindowSizeClass.current
    val compactLayout = windowSizeClass == TaggoWindowSizeClass.Compact
    val compactBottomPadding = if (compactLayout) 96.dp else 0.dp
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

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = expandedPageContentModifier().padding(
                start = if (compactLayout) 4.dp else 0.dp,
                end = if (compactLayout) 4.dp else 0.dp,
                bottom = compactBottomPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
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
                color = TaggoTheme.colors.textMuted,
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
                    locale = locale,
                    fullCjkFontReady = fullCjkFontReady,
                    fullCjkFontFamily = fullCjkFontFamily,
                    onOpen = onOpenReference,
                )
            }
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
        colors = CardDefaults.cardColors(containerColor = TaggoTheme.colors.surface),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = subtitle,
                    color = TaggoTheme.colors.textSecondary,
                    fontSize = 12.sp,
                )
            }

            if (tags.isEmpty()) {
                Text(
                    text = emptyHint,
                    color = TaggoTheme.colors.textSecondary,
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
        responsiveHeader = true,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
/**
 * 详情页。
 *
 * 这里展示单个文件条目的详细信息，并承接打开、刷新、替换、删除和标签编辑等入口。
 */
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
    val windowSizeClass = LocalTaggoWindowSizeClass.current
    val compactLayout = windowSizeClass == TaggoWindowSizeClass.Compact

    if (reference == null) {
        Column(
            modifier = if (compactLayout) Modifier.padding(horizontal = 4.dp) else Modifier,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
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

    Column(
        modifier = if (compactLayout) Modifier.padding(horizontal = 4.dp) else Modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
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
                    contentColor = TaggoTheme.colors.danger,
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
            containerColor = TaggoTheme.colors.surfaceElevated,
            title = {
                Text(
                    text = if (locale == AppLocale.ZhCn) "\u6dfb\u52a0\u6807\u7b7e" else "Add tag",
                    color = TaggoTheme.colors.textPrimary,
                )
            },
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
                        colors = taggoOutlinedTextFieldColors(),
                    )

                    if (!tagFeedbackMessage.isNullOrBlank()) {
                        Text(
                            text = tagFeedbackMessage.orEmpty(),
                            color = TaggoTheme.colors.textSecondary,
                            fontSize = 12.sp,
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (locale == AppLocale.ZhCn) "\u5e38\u7528\u6807\u7b7e" else "Common tags",
                            color = TaggoTheme.colors.textPrimary,
                            fontWeight = FontWeight.Medium,
                        )
                        if (tagCandidates.isEmpty()) {
                            Text(
                                text = if (locale == AppLocale.ZhCn) "\u6682\u65e0\u5019\u9009\u6807\u7b7e" else "No tag suggestions yet",
                                color = TaggoTheme.colors.textSecondary,
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
            containerColor = TaggoTheme.colors.surfaceElevated,
            title = {
                Text(
                    text = if (locale == AppLocale.ZhCn) "\u6240\u9009\u6587\u4ef6\u53ef\u80fd\u4e0d\u662f\u539f\u6587\u4ef6" else "The selected file may not be the original",
                    color = TaggoTheme.colors.textPrimary,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (locale == AppLocale.ZhCn) {
                            "\u65b0\u9009\u62e9\u7684\u6587\u4ef6\u548c\u5f53\u524d\u6761\u76ee\u7684\u540d\u79f0\u3001\u7c7b\u578b\u6216\u5927\u5c0f\u4e0d\u5b8c\u5168\u4e00\u81f4\u3002"
                        } else {
                            "The newly selected file does not fully match the current entry's name, type, or size."
                        },
                        color = TaggoTheme.colors.textPrimary,
                    )
                    Text(
                        text = if (locale == AppLocale.ZhCn) {
                            "\u4f60\u53ef\u4ee5\u53d6\u6d88\uff0c\u6216\u8005\u7ee7\u7eed\u7528\u8fd9\u4e2a\u6587\u4ef6\u66ff\u6362\u5f53\u524d\u5f15\u7528\u3002"
                        } else {
                            "You can cancel, or continue and replace the current reference with this file."
                        },
                        color = TaggoTheme.colors.textSecondary,
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
        colors = CardDefaults.cardColors(containerColor = TaggoTheme.colors.surfaceElevated),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        val windowSizeClass = LocalTaggoWindowSizeClass.current
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(if (windowSizeClass == TaggoWindowSizeClass.Compact) 8.dp else 0.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (windowSizeClass != TaggoWindowSizeClass.Compact) {
                    Spacer(modifier = Modifier.width(12.dp))
                    trailing()
                }
            }

            if (windowSizeClass == TaggoWindowSizeClass.Compact) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    trailing()
                }
            }
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
/**
 * 侧边菜单面板。
 *
 * 该面板承接工作区级别的操作入口，例如语言切换、快照导入导出和本地数据清理。
 */
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
            colors = CardDefaults.cardColors(containerColor = TaggoTheme.colors.surfaceElevated),
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
                        text = if (locale == AppLocale.ZhCn) "\u8bbe\u7f6e" else "Settings",
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
    textStyle: androidx.compose.ui.text.TextStyle,
    supportingText: String? = null,
) {
    val windowSizeClass = LocalTaggoWindowSizeClass.current
    val compactLayout = windowSizeClass == TaggoWindowSizeClass.Compact
    Card(
        colors = CardDefaults.cardColors(containerColor = TaggoTheme.colors.surfaceElevated),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (compactLayout) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = { Text(placeholder) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                    textStyle = textStyle,
                    colors = taggoOutlinedTextFieldColors(),
                )
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TaggoTheme.colors.primaryAccent.copy(alpha = 0.9f),
                        contentColor = TaggoTheme.colors.textPrimary,
                    ),
                    onClick = onSearch,
                ) {
                    Text(buttonLabel)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                        colors = taggoOutlinedTextFieldColors(),
                    )
                    Button(
                        modifier = Modifier.height(46.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TaggoTheme.colors.primaryAccent.copy(alpha = 0.9f),
                            contentColor = TaggoTheme.colors.textPrimary,
                        ),
                        onClick = onSearch,
                    ) {
                        Text(buttonLabel)
                    }
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
    val windowSizeClass = LocalTaggoWindowSizeClass.current
    val compactLayout = windowSizeClass == TaggoWindowSizeClass.Compact
    val containerModifier = when (windowSizeClass) {
        TaggoWindowSizeClass.Expanded -> Modifier.widthIn(min = 280.dp, max = 520.dp)
        TaggoWindowSizeClass.Medium -> Modifier.fillMaxWidth().widthIn(max = 360.dp)
        TaggoWindowSizeClass.Compact -> Modifier.fillMaxWidth()
    }

    if (compactLayout) {
        Column(
            modifier = containerModifier,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text(placeholder) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                textStyle = textStyle,
                colors = taggoOutlinedTextFieldColors(),
            )
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TaggoTheme.colors.primaryAccent.copy(alpha = 0.9f),
                    contentColor = TaggoTheme.colors.textPrimary,
                ),
                onClick = onSearch,
            ) {
                Text(buttonLabel)
            }
        }
    } else {
        Row(
            modifier = containerModifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                colors = taggoOutlinedTextFieldColors(),
            )
            Button(
                modifier = Modifier.height(46.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TaggoTheme.colors.primaryAccent.copy(alpha = 0.9f),
                    contentColor = TaggoTheme.colors.textPrimary,
                ),
                onClick = onSearch,
            ) {
                Text(buttonLabel)
            }
        }
    }
}

@Composable
private fun taggoOutlinedTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = TaggoTheme.colors.surface,
    unfocusedContainerColor = TaggoTheme.colors.surface,
    disabledContainerColor = TaggoTheme.colors.surfaceVariant,
    focusedTextColor = TaggoTheme.colors.textPrimary,
    unfocusedTextColor = TaggoTheme.colors.textPrimary,
    disabledTextColor = TaggoTheme.colors.textMuted,
    focusedLabelColor = TaggoTheme.colors.textPrimary,
    unfocusedLabelColor = TaggoTheme.colors.textSecondary,
    disabledLabelColor = TaggoTheme.colors.textMuted,
    focusedIndicatorColor = TaggoTheme.colors.primaryAccent,
    unfocusedIndicatorColor = TaggoTheme.colors.borderStrong,
    disabledIndicatorColor = TaggoTheme.colors.border,
    focusedPlaceholderColor = TaggoTheme.colors.textMuted,
    unfocusedPlaceholderColor = TaggoTheme.colors.textMuted,
    focusedSupportingTextColor = TaggoTheme.colors.textSecondary,
    unfocusedSupportingTextColor = TaggoTheme.colors.textSecondary,
    disabledSupportingTextColor = TaggoTheme.colors.textMuted,
    cursorColor = TaggoTheme.colors.primaryAccent,
)

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
private fun AdaptiveFileGrid(
    items: List<FileReference>,
    locale: AppLocale,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    onOpen: (FileReference) -> Unit,
) {
    if (items.isEmpty()) return

    val windowSizeClass = LocalTaggoWindowSizeClass.current

    BoxWithConstraints {
        val columns = when (windowSizeClass) {
            TaggoWindowSizeClass.Compact -> 1
            TaggoWindowSizeClass.Medium -> when {
                maxWidth >= 840.dp -> 2
                else -> 1
            }
            TaggoWindowSizeClass.Expanded -> when {
                maxWidth >= 1180.dp -> 4
                maxWidth >= 900.dp -> 3
                maxWidth >= 620.dp -> 2
                else -> 1
            }
        }
        val spacing = if (windowSizeClass == TaggoWindowSizeClass.Compact) 8.dp else 10.dp
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
                            locale = locale,
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
    locale: AppLocale,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconStyle = fileTypeIconStyle(reference)
    val windowSizeClass = LocalTaggoWindowSizeClass.current
    val compactLayout = windowSizeClass == TaggoWindowSizeClass.Compact
    val desktopThumbnailSize = if (windowSizeClass == TaggoWindowSizeClass.Medium) 72.dp else 78.dp
    val metaLine = remember(reference, fullCjkFontReady) {
        buildList {
            val fileTypeLabel = displayTextForUi(reference.fileType, fullCjkFontReady).ifBlank { "file" }
            add(fileTypeLabel)
            add(formatRelativeTime(reference.lastOpenedAtMillis))
            add(formatFileSize(reference.fileSizeBytes ?: guessFileSizeFromNotes(reference.notes)))
        }.joinToString(separator = " • ")
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = TaggoTheme.colors.surface),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .border(
                width = 1.dp,
                color = TaggoTheme.colors.border,
                shape = RoundedCornerShape(20.dp),
            )
            .clickable(onClick = onOpen),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        if (compactLayout) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FileCoverArtFrame(
                    reference = reference,
                    iconStyle = iconStyle,
                    fullCjkFontReady = fullCjkFontReady,
                    fullCjkFontFamily = fullCjkFontFamily,
                    modifier = Modifier.size(58.dp),
                    iconSize = 34.dp,
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = displayTextForUi(reference.title, fullCjkFontReady),
                        color = TaggoTheme.colors.textPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        minLines = 2,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = fullCjkFontFamily,
                    )

                    Text(
                        text = metaLine,
                        color = TaggoTheme.colors.textSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    if (reference.tags.isNotEmpty()) {
                        ReferenceTagSummary(
                            tags = reference.tags,
                            locale = locale,
                            fullCjkFontReady = fullCjkFontReady,
                            fullCjkFontFamily = fullCjkFontFamily,
                        )
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FileCoverArtFrame(
                    reference = reference,
                    iconStyle = iconStyle,
                    fullCjkFontReady = fullCjkFontReady,
                    fullCjkFontFamily = fullCjkFontFamily,
                    modifier = Modifier.size(desktopThumbnailSize),
                    iconSize = if (windowSizeClass == TaggoWindowSizeClass.Medium) 42.dp else 46.dp,
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = displayTextForUi(reference.title, fullCjkFontReady),
                        modifier = Modifier.fillMaxWidth(),
                        color = TaggoTheme.colors.textPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        minLines = 2,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = fullCjkFontFamily,
                    )

                    Text(
                        text = metaLine,
                        modifier = Modifier.fillMaxWidth(),
                        color = TaggoTheme.colors.textSecondary,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    if (reference.tags.isNotEmpty()) {
                        ReferenceTagSummary(
                            tags = reference.tags,
                            locale = locale,
                            fullCjkFontReady = fullCjkFontReady,
                            fullCjkFontFamily = fullCjkFontFamily,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReferenceTagSummary(
    tags: List<String>,
    locale: AppLocale,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
) {
    val visibleTags = tags.take(3)
    val remainingTagCount = tags.size - visibleTags.size
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            visibleTags.forEach { tag ->
                TagPill(
                    tag = tag,
                    fullCjkFontReady = fullCjkFontReady,
                    fullCjkFontFamily = fullCjkFontFamily,
                )
            }
        }
        if (remainingTagCount > 0) {
            OverflowTagHint(
                count = remainingTagCount,
                locale = locale,
            )
        }
    }
}

@Composable
private fun OverflowTagHint(
    count: Int,
    locale: AppLocale,
) {
    Text(
        text = if (locale == AppLocale.ZhCn) "\u8fd8\u6709$count\u4e2a" else "$count more",
        color = TaggoTheme.colors.textMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
    )
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
    val windowSizeClass = LocalTaggoWindowSizeClass.current
    val compactLayout = windowSizeClass == TaggoWindowSizeClass.Compact
    val mediumLayout = windowSizeClass == TaggoWindowSizeClass.Medium
    val heroMetaLine = remember(reference, fullCjkFontReady) {
        buildList {
            add(displayTextForUi(reference.fileType, fullCjkFontReady).ifBlank { "file" })
            add(formatRelativeTime(reference.lastOpenedAtMillis))
            add(formatFileSize(reference.fileSizeBytes ?: guessFileSizeFromNotes(reference.notes)))
        }.joinToString(separator = " • ")
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = TaggoTheme.colors.surfaceElevated),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TaggoTheme.colors.border, RoundedCornerShape(20.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        if (compactLayout) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FileCoverArtFrame(
                    reference = reference,
                    iconStyle = iconStyle,
                    fullCjkFontReady = fullCjkFontReady,
                    fullCjkFontFamily = fullCjkFontFamily,
                    modifier = Modifier.size(100.dp),
                    cornerShape = RoundedCornerShape(24.dp),
                    iconSize = 46.dp,
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = displayTextForUi(reference.title, fullCjkFontReady),
                        style = MaterialTheme.typography.titleMedium,
                        color = TaggoTheme.colors.textPrimary,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        fontFamily = fullCjkFontFamily,
                    )

                    Text(
                        text = heroMetaLine,
                        color = TaggoTheme.colors.textSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TaggoTheme.colors.primaryAccent.copy(alpha = 0.9f),
                            contentColor = TaggoTheme.colors.textPrimary,
                        ),
                        onClick = onOpenFile,
                        enabled = canOpenFile,
                    ) {
                        Text(openButtonLabel)
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top,
            ) {
                FileCoverArtFrame(
                    reference = reference,
                    iconStyle = iconStyle,
                    fullCjkFontReady = fullCjkFontReady,
                    fullCjkFontFamily = fullCjkFontFamily,
                    modifier = Modifier
                        .size(112.dp),
                    cornerShape = RoundedCornerShape(26.dp),
                    iconSize = 50.dp,
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(if (mediumLayout) 10.dp else 0.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = displayTextForUi(reference.title, fullCjkFontReady),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TaggoTheme.colors.textPrimary,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 16.sp,
                                    maxLines = if (mediumLayout) 3 else 2,
                                    overflow = TextOverflow.Ellipsis,
                                    fontFamily = fullCjkFontFamily,
                                )
                                Text(
                                    text = heroMetaLine,
                                    color = TaggoTheme.colors.textSecondary,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }

                            if (!mediumLayout) {
                                Button(
                                    modifier = Modifier.height(46.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = TaggoTheme.colors.primaryAccent.copy(alpha = 0.9f),
                                        contentColor = TaggoTheme.colors.textPrimary,
                                    ),
                                    onClick = onOpenFile,
                                    enabled = canOpenFile,
                                ) {
                                    Text(openButtonLabel)
                                }
                            }
                        }

                        if (mediumLayout) {
                            Button(
                                modifier = Modifier.wrapContentWidth(Alignment.End),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TaggoTheme.colors.primaryAccent.copy(alpha = 0.9f),
                                    contentColor = TaggoTheme.colors.textPrimary,
                                ),
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
    }
}

@Composable
/**
 * 手动添加或替换文件条目的对话框。
 *
 * 对话框只负责收集草稿字段和触发确认动作，
 * 真正的草稿入库、替换和后续缩略图处理仍由应用状态层完成。
 */
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
        containerColor = TaggoTheme.colors.surfaceElevated,
        title = {
            Text(
                text = if (isReplaceMode) {
                    if (locale == AppLocale.ZhCn) "\u66ff\u6362\u6587\u4ef6" else "Replace file"
                } else {
                    if (locale == AppLocale.ZhCn) "\u6dfb\u52a0\u6587\u4ef6" else "Add file"
                },
                color = TaggoTheme.colors.textPrimary,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!notice.isNullOrBlank()) {
                    Text(
                        text = notice,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TaggoTheme.colors.textSecondary,
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
                    colors = taggoOutlinedTextFieldColors(),
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = appState.draftTitle,
                    onValueChange = { appState.draftTitle = it },
                    label = { Text(appState.referenceTitle) },
                    singleLine = true,
                    textStyle = fullCjkTextStyle,
                    colors = taggoOutlinedTextFieldColors(),
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = appState.draftSource,
                    onValueChange = { appState.draftSource = it },
                    label = { Text(appState.referenceLocation) },
                    singleLine = true,
                    textStyle = fullCjkTextStyle,
                    colors = taggoOutlinedTextFieldColors(),
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = appState.draftType,
                    onValueChange = { appState.draftType = it },
                    label = { Text(appState.fileType) },
                    singleLine = true,
                    textStyle = fullCjkTextStyle,
                    colors = taggoOutlinedTextFieldColors(),
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = appState.draftCoverArtSource,
                    onValueChange = { appState.draftCoverArtSource = it },
                    label = { Text(if (locale == AppLocale.ZhCn) "\u5c01\u9762\u6765\u6e90" else "Cover art source") },
                    singleLine = true,
                    textStyle = fullCjkTextStyle,
                    colors = taggoOutlinedTextFieldColors(),
                )
                if (!isReplaceMode) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = appState.draftTags,
                        onValueChange = { appState.draftTags = it },
                        label = { Text(appState.tagsCommaSeparated) },
                        singleLine = true,
                        textStyle = fullCjkTextStyle,
                        colors = taggoOutlinedTextFieldColors(),
                    )
                } else {
                    Text(
                        text = if (locale == AppLocale.ZhCn) {
                            "\u66ff\u6362\u65f6\u4f1a\u4fdd\u7559\u5f53\u524d\u6807\u7b7e\u3002"
                        } else {
                            "Current tags will be kept during replacement."
                        },
                        color = TaggoTheme.colors.textSecondary,
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
                    colors = taggoOutlinedTextFieldColors(),
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

/**
 * 从只读推荐状态中提取首页展示用的推荐文件列表。
 *
 * 首页不直接依赖带打分明细的结构，只保留前 10 个文件条目用于展示。
 */
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

@Composable
private fun expandedPageContentModifier(): Modifier {
    val windowSizeClass = LocalTaggoWindowSizeClass.current
    return if (windowSizeClass == TaggoWindowSizeClass.Expanded) {
        Modifier
            .fillMaxWidth()
            .widthIn(max = 1180.dp)
    } else {
        Modifier.fillMaxWidth()
    }
}

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


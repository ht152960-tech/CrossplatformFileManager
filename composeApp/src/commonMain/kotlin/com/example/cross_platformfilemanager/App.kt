package com.example.cross_platformfilemanager

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
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    var currentPage by remember { mutableStateOf(AppPage.Home) }
    var searchDraft by remember { mutableStateOf("") }
    var sortMode by remember { mutableStateOf(FileSortMode.RecentAdded) }
    var showManualAddDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(snapshotStore, browserReferenceResolver) {
        snapshotStore?.load()?.let(appState::restoreSnapshot)
        if (browserReferenceResolver != null) {
            appState.refreshBrowserReferences()
        }
    }

    LaunchedEffect(appState.snapshotVersion, snapshotStore) {
        snapshotStore?.save(appState.exportSnapshot())
    }

    fun openReference(reference: FileReference) {
        openReferenceWithRefresh(appState, coroutineScope, reference)
        currentPage = AppPage.Detail
    }

    fun startSearch() {
        appState.query = searchDraft.trim()
        appState.selectedTag = null
        appState.selectedFileType = null
        appState.favoritesOnly = false
        appState.commitSearch()
        currentPage = AppPage.Search
    }

    fun addPickedReference(draft: BrowserReferenceDraft) {
        appState.applyBrowserDraft(draft)
        appState.addDraftReference()
        clearDraftFields(appState)
        currentPage = AppPage.Home
    }

    fun showAddReferenceDialog() {
        clearDraftFields(appState)
        showManualAddDialog = true
    }

        MaterialTheme(colorScheme = appColorScheme()) {
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
                            searchDraft = searchDraft,
                            onSearchDraftChange = { searchDraft = it },
                            onPickFile = {
                                if (browserReferencePicker == null) {
                                    showAddReferenceDialog()
                                } else {
                                    coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
                                        runCatching { browserReferencePicker.pickReference() }
                                            .onSuccess { draft ->
                                                if (draft == null) {
                                                    showAddReferenceDialog()
                                                } else {
                                                    addPickedReference(draft)
                                                }
                                            }
                                            .onFailure {
                                                showAddReferenceDialog()
                                            }
                                    }
                                }
                            },
                            onOpenAllFiles = { currentPage = AppPage.AllFiles },
                            onSearch = ::startSearch,
                            onOpenReference = ::openReference,
                        )

                        AppPage.AllFiles -> AllFilesPage(
                            appState = appState,
                            sortMode = sortMode,
                            onSortModeChange = { sortMode = it },
                            onBackHome = { currentPage = AppPage.Home },
                            onOpenReference = ::openReference,
                        )

                        AppPage.Detail -> DetailPage(
                            appState = appState,
                            reference = appState.activeReference,
                            onBackHome = { currentPage = AppPage.Home },
                            onDeleteReference = { showDeleteConfirm = true },
                        )

                        AppPage.Search -> SearchResultsPage(
                            appState = appState,
                            searchDraft = searchDraft,
                            onSearchDraftChange = { searchDraft = it },
                            onSearch = ::startSearch,
                            onBackHome = { currentPage = AppPage.Home },
                            onOpenReference = ::openReference,
                            onTagClick = { tag ->
                                searchDraft = tag
                                appState.query = tag
                                appState.selectedTag = tag
                                appState.selectedFileType = null
                                appState.favoritesOnly = false
                                appState.commitSearch()
                                currentPage = AppPage.Search
                            },
                            onDeleteTag = { tag ->
                                appState.deleteTagEverywhere(tag)
                                if (searchDraft == tag) {
                                    searchDraft = ""
                                }
                            },
                        )
                    }
                }

                if (showManualAddDialog) {
                    ManualAddDialog(
                        appState = appState,
                        onDismiss = { showManualAddDialog = false },
                        onConfirm = {
                            appState.addDraftReference()
                            clearDraftFields(appState)
                            showManualAddDialog = false
                            currentPage = AppPage.Home
                        },
                    )
                }

                if (showDeleteConfirm && appState.activeReference != null) {
                    val reference = appState.activeReference
                    AlertDialog(
                        onDismissRequest = { showDeleteConfirm = false },
                        title = { Text(if (appState.locale == AppLocale.ZhCn) "删除文件" else "Delete file") },
                        text = { Text(reference?.title.orEmpty()) },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    reference?.let { appState.deleteReference(it.id) }
                                    showDeleteConfirm = false
                                    currentPage = AppPage.Home
                                },
                            ) {
                                Text(
                                    text = if (appState.locale == AppLocale.ZhCn) "删除" else "Delete",
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteConfirm = false }) {
                                Text(if (appState.locale == AppLocale.ZhCn) "取消" else "Cancel")
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun HomePage(
    appState: FileManagerAppState,
    searchDraft: String,
    onSearchDraftChange: (String) -> Unit,
    onPickFile: () -> Unit,
    onOpenAllFiles: () -> Unit,
    onSearch: () -> Unit,
    onOpenReference: (FileReference) -> Unit,
) {
    val locale = appState.locale
    val recommendedReferences = resolveRecommendedReferences(appState)

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TopBarCard(
            title = if (locale == AppLocale.ZhCn) "文件管理器" else "File Atlas",
            trailing = {
                OutlinedButton(onClick = onOpenAllFiles) {
                    Text(if (locale == AppLocale.ZhCn) "全部文件" else "All files")
                }
            },
        )

        SearchBarCard(
            query = searchDraft,
            onQueryChange = onSearchDraftChange,
            onSearch = onSearch,
            placeholder = appState.searchPlaceholder,
            buttonLabel = if (locale == AppLocale.ZhCn) "搜索" else "Search",
        )

        SectionCard(
            title = if (locale == AppLocale.ZhCn) "快捷操作" else "Quick actions",
            subtitle = if (locale == AppLocale.ZhCn) {
                "先选择文件，或者直接进入全部文件。"
            } else {
                "Pick a file or jump to the full library."
            },
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(onClick = onPickFile) {
                    Text(if (locale == AppLocale.ZhCn) "选择文件" else "Pick file")
                }
            }
        }

        SectionCard(
            title = appState.recentlyAdded,
            subtitle = if (locale == AppLocale.ZhCn) {
                "点击任意条目进入详情页。"
            } else {
                "Open any item to view its details."
            },
        ) {
            if (appState.recentAddedReferences.isEmpty()) {
                EmptyPanel(
                    title = if (locale == AppLocale.ZhCn) "还没有新文件" else "No recent files yet",
                    body = if (locale == AppLocale.ZhCn) {
                        "选择一个文件后，它会出现在这里。"
                    } else {
                        "Pick a file and it will appear here."
                    },
                )
            } else {
                AdaptiveFileGrid(
                    items = appState.recentAddedReferences,
                    onOpen = onOpenReference,
                )
            }
        }

        SectionCard(
            title = appState.recommendationTitle,
            subtitle = appState.recommendationSubtitle,
        ) {
            if (recommendedReferences.isEmpty()) {
                EmptyPanel(
                    title = appState.noRecommendations,
                    body = if (locale == AppLocale.ZhCn) {
                        "系统还没有足够的数据生成推荐。"
                    } else {
                        "The system has not collected enough signals yet."
                    },
                )
            } else {
                AdaptiveFileGrid(
                    items = recommendedReferences,
                    onOpen = onOpenReference,
                )
            }
        }
    }
}

@Composable
private fun AllFilesPage(
    appState: FileManagerAppState,
    sortMode: FileSortMode,
    onSortModeChange: (FileSortMode) -> Unit,
    onBackHome: () -> Unit,
    onOpenReference: (FileReference) -> Unit,
) {
    val locale = appState.locale
    val sortedFiles = remember(appState.allReferences, sortMode) {
        sortReferences(appState.allReferences, sortMode)
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TopBarCard(
            title = if (locale == AppLocale.ZhCn) "全部文件" else "All files",
            onBack = onBackHome,
        )

        SectionCard(
            title = if (locale == AppLocale.ZhCn) "排序条件" else "Sort by",
            subtitle = if (locale == AppLocale.ZhCn) {
                "切换排序方式后，下面的网格会立即更新。"
            } else {
                "Switch the order and the grid updates immediately."
            },
        ) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SortChip(
                    label = if (locale == AppLocale.ZhCn) "最近添加" else "Recent added",
                    selected = sortMode == FileSortMode.RecentAdded,
                    onClick = { onSortModeChange(FileSortMode.RecentAdded) },
                )
                SortChip(
                    label = if (locale == AppLocale.ZhCn) "文件名" else "Name",
                    selected = sortMode == FileSortMode.Name,
                    onClick = { onSortModeChange(FileSortMode.Name) },
                )
                SortChip(
                    label = if (locale == AppLocale.ZhCn) "文件类型" else "Type",
                    selected = sortMode == FileSortMode.Type,
                    onClick = { onSortModeChange(FileSortMode.Type) },
                )
                SortChip(
                    label = if (locale == AppLocale.ZhCn) "标签" else "Tags",
                    selected = sortMode == FileSortMode.Tags,
                    onClick = { onSortModeChange(FileSortMode.Tags) },
                )
            }
        }

        if (sortedFiles.isEmpty()) {
            EmptyPanel(
                title = if (locale == AppLocale.ZhCn) "暂无文件" else "No files uploaded yet",
                body = if (locale == AppLocale.ZhCn) {
                    "先选择一个文件，列表就会出现。"
                } else {
                    "Pick a file first and it will show up here."
                },
            )
        } else {
            AdaptiveFileGrid(
                items = sortedFiles,
                onOpen = onOpenReference,
            )
        }
    }
}

@Composable
private fun SearchResultsPage(
    appState: FileManagerAppState,
    searchDraft: String,
    onSearchDraftChange: (String) -> Unit,
    onSearch: () -> Unit,
    onBackHome: () -> Unit,
    onOpenReference: (FileReference) -> Unit,
    onTagClick: (String) -> Unit,
    onDeleteTag: (String) -> Unit,
) {
    val locale = appState.locale
    val searchResults = appState.searchResults
    var tagPendingDelete by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TopBarCard(
            title = if (locale == AppLocale.ZhCn) "\u67e5\u627e\u7ed3\u679c" else "Search results",
            onBack = onBackHome,
        )

        SearchBarCard(
            query = searchDraft,
            onQueryChange = onSearchDraftChange,
            onSearch = onSearch,
            placeholder = appState.searchPlaceholder,
            buttonLabel = if (locale == AppLocale.ZhCn) "\u641c\u7d22" else "Search",
        )

        SectionCard(
            title = if (locale == AppLocale.ZhCn) "\u641c\u7d22\u72b6\u6001" else "Search status",
            subtitle = if (locale == AppLocale.ZhCn) {
                "\u53ef\u4ee5\u7ee7\u7eed\u8f93\u5165\u65b0\u5173\u952e\u5b57\u518d\u67e5\u627e\u4e00\u6b21\u3002"
            } else {
                "You can enter a new keyword and search again."
            },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (appState.query.isBlank()) {
                        if (locale == AppLocale.ZhCn) "\u5173\u952e\u5b57\uff1a\u5168\u90e8" else "Keyword: All"
                    } else {
                        if (locale == AppLocale.ZhCn) {
                            "\u5173\u952e\u5b57\uff1a${appState.query}"
                        } else {
                            "Keyword: ${appState.query}"
                        }
                    },
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = if (locale == AppLocale.ZhCn) {
                        "\u627e\u5230 ${searchResults.size} \u4e2a\u76f8\u5173\u6587\u4ef6"
                    } else {
                        "${searchResults.size} matching files"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                )
            }
        }

        if (appState.allTags.isNotEmpty()) {
            SectionCard(
                title = appState.tagLibraryTitle,
                subtitle = appState.tagLibrarySubtitle,
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    appState.allTags.forEach { tag ->
                        TagLibraryRow(
                            tag = tag,
                            onTagClick = { onTagClick(tag) },
                            onDeleteClick = { tagPendingDelete = tag },
                        )
                    }
                }
            }
        }

        if (searchResults.isEmpty()) {
            EmptyPanel(
                title = appState.emptyResultsTitle,
                body = appState.emptyResultsBody,
            )
        } else {
            AdaptiveFileGrid(
                items = searchResults.map { it.reference },
                onOpen = onOpenReference,
            )
        }
    }

    if (tagPendingDelete != null) {
        val tag = tagPendingDelete
        AlertDialog(
            onDismissRequest = { tagPendingDelete = null },
            title = {
                Text(text = if (locale == AppLocale.ZhCn) "\u786e\u8ba4\u5220\u9664\uff1f" else "Delete tag everywhere")
            },
            text = {
                Text(
                    text = if (locale == AppLocale.ZhCn) {
                        "\u8fd9\u4f1a\u628a \"$tag\" \u4ece\u6240\u6709\u6587\u4ef6\u4e0a\u79fb\u9664\uff0c\u4f46\u4e0d\u4f1a\u5220\u9664\u6587\u4ef6\u672c\u8eab\u3002"
                    } else {
                        "This removes \"$tag\" from every file, but keeps the files."
                    },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        tag?.let(onDeleteTag)
                        tagPendingDelete = null
                    },
                ) {
                    Text(if (locale == AppLocale.ZhCn) "\u786e\u5b9a\u5220\u9664" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { tagPendingDelete = null }) {
                    Text(if (locale == AppLocale.ZhCn) "\u53d6\u6d88" else "Cancel")
                }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagLibraryRow(
    tag: String,
    onTagClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .wrapContentWidth()
            .padding(top = 2.dp, bottom = 2.dp),
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            shape = RoundedCornerShape(999.dp),
            modifier = Modifier
                .wrapContentWidth()
                .padding(end = 10.dp)
                .clickable(onClick = onTagClick),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = displayText(tag),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 6.dp, y = (-6).dp)
                .size(20.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color(0xFFF1F5F9))
                .clickable(onClick = onDeleteClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "×",
                color = Color(0xFF475569),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun DetailPage(
    appState: FileManagerAppState,
    reference: FileReference?,
    onBackHome: () -> Unit,
    onDeleteReference: () -> Unit,
) {
    val locale = appState.locale

    if (reference == null) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            TopBarCard(
                title = if (locale == AppLocale.ZhCn) "文件详情" else "File details",
                onBack = onBackHome,
            )
            SectionCard(
                title = if (locale == AppLocale.ZhCn) "没有选中文件" else "No file selected",
                subtitle = if (locale == AppLocale.ZhCn) {
                    "先从主页、全部文件页或搜索结果页打开一个文件。"
                } else {
                    "Open a file from the home page, library, or search results first."
                },
            ) {
                EmptyPanel(
                    title = if (locale == AppLocale.ZhCn) "当前没有文件" else "Nothing open",
                    body = if (locale == AppLocale.ZhCn) {
                        "打开一个文件后，这里会显示文件图标、标签和操作按钮。"
                    } else {
                        "Once opened, this page shows the icon, tags, and actions."
                    },
                )
            }
        }
        return
    }

    var showTagDialog by remember(reference.id) { mutableStateOf(false) }
    var newTagDraft by remember(reference.id) { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TopBarCard(
            title = if (locale == AppLocale.ZhCn) "文件详情" else "File details",
            onBack = onBackHome,
        )

        DetailHeroCard(reference = reference)

        SectionCard(
            title = if (locale == AppLocale.ZhCn) "\u6587\u4ef6\u4fe1\u606f" else "File info",
            subtitle = if (locale == AppLocale.ZhCn) {
                "\u8fd9\u91cc\u663e\u793a\u4e0a\u4f20\u65f6\u95f4\u3001\u5927\u5c0f\u548c\u6765\u6e90\u3002"
            } else {
                "Upload time, size, and source are shown here."
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
                    label = if (locale == AppLocale.ZhCn) "\u6765\u6e90" else "Source",
                    value = displayText(reference.source),
                )
            }
        }

        SectionCard(
            title = if (locale == AppLocale.ZhCn) "\u6807\u7b7e" else "Tags",
            subtitle = if (locale == AppLocale.ZhCn) {
                "\u70b9\u51fb\u6807\u7b7e\u53ef\u4ee5\u79fb\u9664\uff0c\u70b9\u53f3\u4fa7\u6309\u94ae\u53ef\u4ee5\u65b0\u589e\u3002"
            } else {
                "Click a tag to remove it, or add a new one on the right."
            },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                            AssistChip(
                                onClick = {
                                    appState.updateReferenceTags(
                                        reference.id,
                                        removeTag(reference.tags, tag),
                                    )
                                },
                                label = { Text(displayText(tag)) },
                            )
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
                                        onClick = { newTagDraft = displayText(tag) },
                                        label = { Text(displayText(tag)) },
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
    title: String,
    onBack: (() -> Unit)? = null,
    trailing: @Composable () -> Unit = {},
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
            if (onBack != null) {
                    TextButton(onClick = onBack) {
                    Text("Back")
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
private fun SortMenuButton(
    sortMode: FileSortMode,
    onSortModeChange: (FileSortMode) -> Unit,
    locale: AppLocale,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(if (locale == AppLocale.ZhCn) "排序" else "Sort")
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
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .aspectRatio(1f)
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
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFE8F1FF), Color(0xFFDDEBFF)),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = reference.fileType.take(1).ifBlank { "F" }.uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D4ED8),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = displayText(reference.title),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = displayText(reference.fileType),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun DetailHeroCard(reference: FileReference) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFE8F1FF), Color(0xFFDDEBFF), Color(0xFFCFE5D9)),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = reference.fileType.take(1).ifBlank { "F" }.uppercase(),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D4ED8),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = displayText(reference.title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = displayText(reference.fileType),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                )
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
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (appState.locale == AppLocale.ZhCn) "添加文件" else "Add file") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
        FileSortMode.RecentAdded -> if (locale == AppLocale.ZhCn) "最近添加" else "Recent added"
        FileSortMode.Name -> if (locale == AppLocale.ZhCn) "文件名" else "Name"
        FileSortMode.Type -> if (locale == AppLocale.ZhCn) "文件类型" else "Type"
        FileSortMode.Tags -> if (locale == AppLocale.ZhCn) "标签" else "Tags"
        FileSortMode.RecentOpened -> if (locale == AppLocale.ZhCn) "最近打开" else "Recent opened"
    }

private fun resolveRecommendedReferences(appState: FileManagerAppState): List<FileReference> {
    val byTitle = appState.allReferences
        .groupBy { normalize(it.title) }
        .mapValues { (_, items) -> items.first() }

    val fromSuggestions = appState.recommendations
        .asSequence()
        .filter { it.kind == SuggestionKind.File }
        .mapNotNull { suggestion -> byTitle[normalize(suggestion.label)] }
        .distinctBy { it.id }
        .take(6)
        .toList()

    return if (fromSuggestions.isNotEmpty()) {
        fromSuggestions
    } else {
        appState.recentReferences.take(6)
    }
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

private fun clearDraftFields(appState: FileManagerAppState) {
    appState.draftTitle = ""
    appState.draftSource = ""
    appState.draftType = ""
    appState.draftFileSizeBytes = null
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

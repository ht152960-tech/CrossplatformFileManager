package com.example.cross_platformfilemanager

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.zIndex
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.graphics.painter.Painter
import org.jetbrains.compose.resources.DrawableResource
import taggo.composeapp.generated.resources.TaggoLogoBig2048
import taggo.composeapp.generated.resources.Res
import taggo.composeapp.generated.resources.taggo_hero_folder
import taggo.composeapp.generated.resources.taggo_hero_trash
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import com.example.cross_platformfilemanager.ThumbnailStatus
import com.example.cross_platformfilemanager.ui.adaptive.AdaptiveAppScaffold
import com.example.cross_platformfilemanager.ui.adaptive.AdaptiveNavigationItem
import com.example.cross_platformfilemanager.ui.adaptive.LocalTaggoWindowSizeClass
import com.example.cross_platformfilemanager.ui.adaptive.TaggoBottomNavigation
import com.example.cross_platformfilemanager.ui.adaptive.TaggoNavigationRail
import com.example.cross_platformfilemanager.ui.adaptive.TaggoSidebarNavigation
import com.example.cross_platformfilemanager.ui.adaptive.TaggoWindowSizeClass
import com.example.cross_platformfilemanager.ui.components.TaggoAddTagCandidateChip
import com.example.cross_platformfilemanager.ui.theme.TaggoTheme
import com.example.cross_platformfilemanager.ui.theme.TaggoThemeTokens.HomeWide
import com.example.cross_platformfilemanager.ui.theme.ProvideTaggoTheme
import com.example.cross_platformfilemanager.ui.components.EmptyPanel
import com.example.cross_platformfilemanager.ui.components.FileCoverArtFrame
import com.example.cross_platformfilemanager.ui.components.FileTypeVisuals
import com.example.cross_platformfilemanager.ui.components.InfoRow
import com.example.cross_platformfilemanager.ui.components.SearchEmptyState
import com.example.cross_platformfilemanager.ui.components.SectionCard
import com.example.cross_platformfilemanager.ui.components.SortChip
import com.example.cross_platformfilemanager.ui.components.TaggoEmptyState
import com.example.cross_platformfilemanager.ui.components.TaggoIconActionButton
import com.example.cross_platformfilemanager.ui.components.TaggoListItemSurface
import com.example.cross_platformfilemanager.ui.components.TaggoMoreButton
import com.example.cross_platformfilemanager.ui.components.TaggoSectionCard
import com.example.cross_platformfilemanager.ui.components.TaggoOpenButton
import com.example.cross_platformfilemanager.ui.components.TaggoTagRow
import com.example.cross_platformfilemanager.ui.components.TaggoFileTagChip
import com.example.cross_platformfilemanager.ui.components.TaggoSearchConditionChip
import com.example.cross_platformfilemanager.ui.components.TaggoTagDeleteChip
import com.example.cross_platformfilemanager.FileTypeClassifier
import com.example.cross_platformfilemanager.rememberThumbnailPainter
import com.example.cross_platformfilemanager.ui.components.fileTypeIconStyle
import com.example.cross_platformfilemanager.ui.components.operableTagChipSpacing
import com.example.cross_platformfilemanager.ui.theme.TaggoGlobalAlpha
import com.example.cross_platformfilemanager.ui.theme.TaggoGlobalColors
import com.example.cross_platformfilemanager.ui.theme.TaggoGlobalRadius
import com.example.cross_platformfilemanager.ui.theme.TaggoGlobalSpacing
import com.example.cross_platformfilemanager.ui.theme.TaggoGlobalTypography
import com.example.cross_platformfilemanager.ui.theme.TaggoCompactTokens
import com.example.cross_platformfilemanager.ui.theme.TaggoFileCoverTokens
import com.example.cross_platformfilemanager.ui.theme.TaggoFileTypeColorTokens

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
    Audio,
    Document,
    Spreadsheet,
    Presentation,
    Other,
}

private fun Modifier.compactHomeAmbientBackground(): Modifier =
    background(TaggoCompactTokens.AmbientBaseBackground)
        .drawBehind {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        TaggoCompactTokens.AmbientGlowPurple,
                        Color.Transparent,
                    ),
                    center = Offset(size.width * 0.86f, size.height * 0.10f),
                    radius = size.maxDimension * 0.42f,
                ),
            )
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        TaggoCompactTokens.AmbientGlowBluePurple,
                        Color.Transparent,
                    ),
                    center = Offset(size.width * 0.22f, size.height * 0.46f),
                    radius = size.maxDimension * 0.48f,
                ),
            )
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        TaggoCompactTokens.AmbientGlowCyanBlue,
                        Color.Transparent,
                    ),
                    center = Offset(size.width * 0.78f, size.height * 0.58f),
                    radius = size.maxDimension * 0.36f,
                ),
            )
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        TaggoCompactTokens.AmbientGlowBottomPurple,
                        Color.Transparent,
                    ),
                    center = Offset(size.width * 0.50f, size.height * 1.18f),
                    radius = size.maxDimension * 0.64f,
                ),
            )
        }

private fun snapshotFailureDescription(error: Throwable): String =
    generateSequence(error) { it.cause }
        .joinToString(separator = " <- ") { it.toString() }

private fun String?.thumbnailPathSummaryForLog(): String =
    if (isNullOrBlank()) "false" else "true:${takeLast(24)}"

private fun String.thumbnailLogValue(): String =
    replace('\n', ' ').replace('\r', ' ').take(64)

@Composable
private fun SidebarUploadButton(
    label: String,
    onClick: () -> Unit,
) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp),
        shape = RoundedCornerShape(13.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF8B68FF).copy(alpha = 0.64f),
            contentColor = TaggoTheme.colors.textPrimary,
        ),
        onClick = onClick,
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SidebarBrandHeader(
    label: String,
    fontFamily: FontFamily,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .padding(horizontal = 0.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(TaggoLogoBig2048),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(25.dp),
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .widthIn(min = 92.dp),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            label.forEach { character ->
                Text(
                    text = character.toString(),
                    color = TaggoTheme.colors.textPrimary,
                    fontSize = 20.sp,
                    lineHeight = 25.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    fontFamily = fontFamily,
                )
            }
        }
    }
}

@Composable
private fun MediumNavigationSidebar(
    items: List<AdaptiveNavigationItem<AppPage>>,
    selectedPage: AppPage,
    brandFontFamily: FontFamily,
    uploadLabel: String,
    onPageSelected: (AppPage) -> Unit,
    onUpload: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(MediumHomeMetrics.SidebarWidth)
            .border(
                width = 1.dp,
                color = TaggoGlobalColors.Border,
                shape = RoundedCornerShape(
                    topEnd = TaggoGlobalRadius.Card,
                    bottomEnd = TaggoGlobalRadius.Card,
                ),
            ),
        color = TaggoGlobalColors.PanelBackgroundSoft,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = MediumHomeMetrics.SidebarHorizontalPadding,
                    vertical = MediumHomeMetrics.SidebarVerticalPadding,
                ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MediumHomeMetrics.SidebarBrandHeight),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(TaggoLogoBig2048),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(MediumHomeMetrics.SidebarLogoSize),
                )
            Text(
                    text = "Taggo",
                    color = TaggoTheme.colors.textPrimary,
                    fontSize = MediumHomeMetrics.SidebarBrandFontSize,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    fontFamily = brandFontFamily,
                )
            }
            Spacer(modifier = Modifier.height(MediumHomeMetrics.SidebarBrandNavigationGap))
            Column(
                verticalArrangement = Arrangement.spacedBy(MediumHomeMetrics.SidebarNavigationItemGap),
            ) {
                items.forEach { item ->
                    val selected = item.page == selectedPage
                    Surface(
                        onClick = { onPageSelected(item.page) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(MediumHomeMetrics.SidebarNavigationItemHeight),
                        shape = RoundedCornerShape(TaggoGlobalRadius.Item),
                        color = if (selected) {
                            TaggoGlobalColors.PrimaryAccentSoft.copy(
                                alpha = MediumHomeMetrics.SidebarSelectedBackgroundAlpha,
                            )
                        } else {
                            Color.Transparent
                        },
                        contentColor = if (selected) {
                            TaggoGlobalColors.TextPrimary.copy(
                                alpha = MediumHomeMetrics.SidebarSelectedContentAlpha,
                            )
                        } else {
                            TaggoGlobalColors.TextMuted.copy(
                                alpha = MediumHomeMetrics.SidebarUnselectedTextAlpha,
                            )
                        },
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = MediumHomeMetrics.SidebarNavigationHorizontalPadding),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            item.icon?.let { icon ->
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (selected) {
                                        TaggoGlobalColors.PrimaryAccent.copy(
                                            alpha = MediumHomeMetrics.SidebarSelectedIconAlpha,
                                        )
                                    } else {
                                        TaggoGlobalColors.TextMuted.copy(
                                            alpha = MediumHomeMetrics.SidebarUnselectedIconAlpha,
                                        )
                                    },
                                    modifier = Modifier.size(MediumHomeMetrics.SidebarNavigationIconSize),
                                )
                            }
                            Text(
                                text = item.label,
                                fontSize = MediumHomeMetrics.SidebarNavigationFontSize,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            val uploadShape = RoundedCornerShape(MediumHomeMetrics.SidebarUploadCornerRadius)
            Button(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(MediumHomeMetrics.SidebarUploadWidth)
                    .height(MediumHomeMetrics.SidebarUploadHeight),
                shape = uploadShape,
                border = BorderStroke(
                    width = 1.dp,
                    color = TaggoGlobalColors.PrimaryAccent.copy(
                        alpha = MediumHomeMetrics.SidebarUploadBorderAlpha,
                    ),
                ),
                contentPadding = PaddingValues(horizontal = 10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B68FF).copy(
                        alpha = MediumHomeMetrics.SidebarUploadBackgroundAlpha,
                    ),
                    contentColor = TaggoGlobalColors.TextPrimary.copy(
                        alpha = MediumHomeMetrics.SidebarUploadContentAlpha,
                    ),
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    focusedElevation = 0.dp,
                    hoveredElevation = 0.dp,
                    disabledElevation = 0.dp,
                ),
                onClick = onUpload,
            ) {
                Text(
                    text = uploadLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
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
    val snapshotSnackbarHostState = remember { SnackbarHostState() }
    val pageScrollState = rememberScrollState()
    val fontLoadState = rememberAppFontLoadState()
    val fullCjkFontLoadState = rememberFullCjkFontLoadState()
    val appStartMillis = remember { nowMillis() }

    var currentPage by remember { mutableStateOf(AppPage.Home) }
    var selectedNavigationPage by remember { mutableStateOf(AppPage.Home) }
    var detailBackTarget by remember { mutableStateOf<AppPage?>(AppPage.Home) }
    var searchBackTarget by remember { mutableStateOf<AppPage?>(AppPage.Home) }
    var searchDraft by remember { mutableStateOf("") }
    var sortMode by remember { mutableStateOf(FileSortMode.RecentOpened) }
    var allFilesTypeFilter by remember { mutableStateOf(AllFilesTypeFilter.All) }
    var showManualAddDialog by remember { mutableStateOf(false) }
    var manualAddNotice by remember { mutableStateOf<String?>(null) }
    var referenceEditorMode by remember { mutableStateOf(ReferenceEditorMode.Add) }
    var referenceEditorTargetId by remember { mutableStateOf<String?>(null) }
    var draftFileSizeText by remember { mutableStateOf("") }
    var manualAddErrorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showSideMenu by remember { mutableStateOf(false) }
    var snapshotReady by remember { mutableStateOf(false) }
    var snapshotSaveFailureEventId by remember { mutableStateOf(0) }
    var snapshotSaveFailureShownEventId by remember { mutableStateOf(0) }
    var searchFeedbackMessage by remember { mutableStateOf<String?>(null) }
    val refreshedReadyThumbnailIds = remember { mutableSetOf<String>() }
    val failedRetryThumbnailIds = remember { mutableSetOf<String>() }

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
            try {
                snapshotStore?.save(appState.exportSnapshot())
                snapshotSaveFailureShownEventId = snapshotSaveFailureEventId
                snapshotSnackbarHostState.currentSnackbarData?.dismiss()
            } catch (error: Exception) {
                debugLog("TaggoSnapshot", "save failed: ${snapshotFailureDescription(error)}")
                snapshotSaveFailureEventId++
            }
        }
    }

    LaunchedEffect(appState.snapshotVersion, snapshotReady, thumbnailGenerator) {
        if (snapshotReady && thumbnailGenerator != null) {
            appState.allReferences.forEach { reference ->
                val thumbnailPath = reference.thumbnailPath
                if (
                    reference.thumbnailStatus == ThumbnailStatus.READY &&
                    !thumbnailPath.isNullOrBlank() &&
                    refreshedReadyThumbnailIds.add(reference.id)
                ) {
                    debugLog(
                        "TaggoThumbnailState",
                        "force refresh id=${reference.id} alreadyForce=false path=${thumbnailPath.thumbnailPathSummaryForLog()}"
                    )
                    appState.generateThumbnailForReference(reference.id, force = true)
                } else if (
                    reference.thumbnailStatus == ThumbnailStatus.READY &&
                    !thumbnailPath.isNullOrBlank() &&
                    reference.id in refreshedReadyThumbnailIds
                ) {
                    debugLog("TaggoThumbnailState", "force refresh skipped duplicate id=${reference.id}")
                }
            }
        }
    }

    LaunchedEffect(snapshotSaveFailureEventId) {
        if (snapshotSaveFailureEventId > snapshotSaveFailureShownEventId) {
            snapshotSaveFailureShownEventId = snapshotSaveFailureEventId
            snapshotSnackbarHostState.showSnackbar(
                message = "\u4fdd\u5b58\u5931\u8d25\uff0c\u4fee\u6539\u53ef\u80fd\u4e0d\u4f1a\u4fdd\u7559",
                actionLabel = "\u77e5\u9053\u4e86",
                duration = SnackbarDuration.Indefinite,
            )
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

    fun openFileDirectly(reference: FileReference) {
        coroutineScope.launch {
            val needsRefresh =
                reference.sourceKind == FileSourceKind.BrowserHandle &&
                    reference.source.startsWith("browser-handle:", ignoreCase = true)
            if (needsRefresh) {
                appState.refreshReference(reference.id)
            }
            val openableReference = appState.activeReference?.takeIf { it.id == reference.id } ?: reference
            val openResult = openReferenceExternallyWithResult(openableReference)
            if (openResult.opened) {
                appState.openReference(openableReference.id)
            } else {
                snapshotSnackbarHostState.showSnackbar(
                    message = openResult.message?.let { localizeWebOpenMessage(it, appState.locale) }
                        ?: openFileFailureMessage(openableReference, appState.locale),
                    actionLabel = if (appState.locale == AppLocale.ZhCn) "\u77e5\u9053\u4e86" else "OK",
                    duration = SnackbarDuration.Short,
                )
            }
        }
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

    fun navigateBackFromSearch() {
        val target = searchBackTarget
        currentPage = when (target) {
            AppPage.Home,
            AppPage.Tags,
            AppPage.AllFiles,
            -> target

            else -> selectedNavigationPage
        }
        searchBackTarget = currentPage
    }

    fun enterSearchPage() {
        if (currentPage != AppPage.Search) {
            searchBackTarget = currentPage
        }
        currentPage = AppPage.Search
    }

    fun startSearchFromHome() {
        appState.resetSearchSession()
        val validationMessage = searchValidationMessage(searchDraft, appState.locale)
        if (validationMessage != null) {
            searchFeedbackMessage = validationMessage
            enterSearchPage()
            return
        }
        if (appState.submitSearch(searchDraft)) {
            searchDraft = ""
            searchFeedbackMessage = null
        }
        enterSearchPage()
    }

    fun startSearchFromSearchPage() {
        val validationMessage = searchValidationMessage(searchDraft, appState.locale)
        if (validationMessage != null) {
            searchFeedbackMessage = validationMessage
            enterSearchPage()
            return
        }
        if (appState.submitSearch(searchDraft)) {
            searchDraft = ""
            searchFeedbackMessage = null
        }
        enterSearchPage()
    }

    fun startRecentSearch(query: String) {
        appState.resetSearchSession()
        if (appState.submitSearch(query)) {
            searchDraft = ""
            searchFeedbackMessage = null
        } else {
            searchDraft = query
        }
        enterSearchPage()
    }

    fun addPickedReference(draft: BrowserReferenceDraft) {
        val tagValidationMessage = formalTagsLengthValidationMessage(appState.draftTags)
        if (tagValidationMessage != null) {
            manualAddErrorMessage = tagValidationMessage
            showManualAddDialog = true
            return
        }
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
        enterSearchPage()
    }

    fun showAddReferenceDialog(notice: String? = null) {
        clearDraftFields(appState)
        draftFileSizeText = ""
        manualAddErrorMessage = null
        referenceEditorMode = ReferenceEditorMode.Add
        referenceEditorTargetId = null
        manualAddNotice = notice
        showManualAddDialog = true
    }

    fun pickFileFromExistingEntry() {
        if (browserReferencePicker == null) {
            showAddReferenceDialog(browserPickerUnavailableMessage(appState.locale))
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

    TaggoBackHandler(enabled = currentPage == AppPage.Detail || currentPage == AppPage.Search) {
        when (currentPage) {
            AppPage.Detail -> navigateBackFromDetail()
            AppPage.Search -> navigateBackFromSearch()
            else -> Unit
        }
    }

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
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val useMediumHomeNavigation = currentPage == AppPage.Home &&
                        maxHeight <= MediumHomeMetrics.MaxDashboardHeight
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        AdaptiveAppScaffold(
                            currentPage = currentPage,
                            selectedNavigationPage = selectedNavigationPage,
                            navigationItems = navigationItems,
                            onPageSelected = { currentPage = it },
                            modifier = Modifier.fillMaxSize(),
                            sideBar = { actualWindowSizeClass ->
                                if (
                                    currentPage == AppPage.Home &&
                                    (
                                        actualWindowSizeClass == TaggoWindowSizeClass.Medium ||
                                            (
                                                actualWindowSizeClass == TaggoWindowSizeClass.Expanded &&
                                                    useMediumHomeNavigation
                                                )
                                            )
                                ) {
                                    MediumNavigationSidebar(
                                        items = navigationItems,
                                        selectedPage = selectedNavigationPage,
                                        brandFontFamily = uiFontFamily,
                                        uploadLabel = if (displayLocale == AppLocale.ZhCn) "\u4e0a\u4f20\u6587\u4ef6" else "Upload file",
                                        onPageSelected = { currentPage = it },
                                        onUpload = ::pickFileFromExistingEntry,
                                    )
                                } else if (actualWindowSizeClass == TaggoWindowSizeClass.Expanded) {
                                    TaggoSidebarNavigation(
                                        items = navigationItems,
                                        selectedPage = selectedNavigationPage,
                                        onPageSelected = { currentPage = it },
                                        footer = {
                                            SidebarUploadButton(
                                                label = if (displayLocale == AppLocale.ZhCn) "\u4e0a\u4f20\u6587\u4ef6" else "Upload file",
                                                onClick = ::pickFileFromExistingEntry,
                                            )
                                        },
                                    )
                                } else {
                                    TaggoNavigationRail(
                                        items = navigationItems,
                                        selectedPage = selectedNavigationPage,
                                        onPageSelected = { currentPage = it },
                                    )
                                }
                            },
                            bottomBar = { actualWindowSizeClass ->
                                if (
                                    currentPage != AppPage.Home ||
                                    actualWindowSizeClass != TaggoWindowSizeClass.Compact
                                ) {
                                    TaggoBottomNavigation(
                                        items = navigationItems,
                                        selectedPage = selectedNavigationPage,
                                        onPageSelected = { currentPage = it },
                                        floating = actualWindowSizeClass == TaggoWindowSizeClass.Compact,
                                    )
                                }
                            },
                            sideBarFooter = {
                                SidebarUploadButton(
                                    label = if (displayLocale == AppLocale.ZhCn) "\u4e0a\u4f20\u6587\u4ef6" else "Upload file",
                                    onClick = ::pickFileFromExistingEntry,
                                )
                            },
                        ) { actualWindowSizeClass ->
                            val homeCompactLayout = currentPage == AppPage.Home &&
                                actualWindowSizeClass == TaggoWindowSizeClass.Compact
                            val searchCompactLayout = currentPage == AppPage.Search &&
                                actualWindowSizeClass == TaggoWindowSizeClass.Compact
                            val homeWideOuterLayout = currentPage == AppPage.Home && !homeCompactLayout
                            val pagePadding = when {
                                homeWideOuterLayout -> PaddingValues(0.dp)
                                homeCompactLayout -> PaddingValues(
                                    start = CompactHomeMetrics.PageHorizontalPadding,
                                    top = CompactHomeMetrics.PageTopPadding,
                                    end = CompactHomeMetrics.PageHorizontalPadding,
                                )
                                else -> PaddingValues(20.dp)
                            }
            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .then(
                                        if (
                                            homeCompactLayout ||
                                            searchCompactLayout ||
                                            (
                                                currentPage == AppPage.AllFiles &&
                                                    actualWindowSizeClass == TaggoWindowSizeClass.Compact
                                            ) ||
                                            (
                                                currentPage == AppPage.Tags &&
                                                    actualWindowSizeClass == TaggoWindowSizeClass.Compact
                                            )
                                        ) {
                                            Modifier.compactHomeAmbientBackground()
                                        } else {
                                            Modifier.background(TaggoTheme.colors.backgroundBrush)
                                        },
                                    ),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(pagePadding)
                                        .then(
                                            if (homeWideOuterLayout || homeCompactLayout) {
                                                Modifier
                                            } else {
                                                Modifier.verticalScroll(pageScrollState)
                                            },
                                        ),
                                    verticalArrangement = Arrangement.spacedBy(if (homeWideOuterLayout) 0.dp else 18.dp),
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
                                            onSearch = ::startSearchFromHome,
                                            onOpenReference = ::openReference,
                                            onOpenFile = ::openFileDirectly,
                                            onOpenTags = { currentPage = AppPage.Tags },
                                            onOpenAllFiles = {
                                                allFilesTypeFilter = AllFilesTypeFilter.All
                                                currentPage = AppPage.AllFiles
                                            },
                                            onOpenTypeFilter = { filter ->
                                                allFilesTypeFilter = filter
                                                currentPage = AppPage.AllFiles
                                            },
                                            onTagClick = ::openTagSearch,
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
                                        typeFilter = allFilesTypeFilter,
                                        onSortModeChange = { sortMode = it },
                                        onTypeFilterChange = { allFilesTypeFilter = it },
                                        onOpenReference = ::openReference,
                                    )

                                    AppPage.Detail -> DetailPage(
                                        appState = appState,
                                        browserReferencePicker = browserReferencePicker,
                                        browserReferenceResolver = browserReferenceResolver,
                                        failedRetryThumbnailIds = failedRetryThumbnailIds,
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
                                        onBackHome = ::navigateBackFromSearch,
                                        onOpenReference = ::openReference,
                                        onRecentSearchClick = ::startRecentSearch,
                                    )
                                }
                            }

                            if (homeCompactLayout) {
                                TaggoBottomNavigation(
                                    items = navigationItems,
                                    selectedPage = selectedNavigationPage,
                                    onPageSelected = { currentPage = it },
                                    modifier = Modifier.align(Alignment.BottomCenter),
                                    floating = true,
                                )
                                CompactUploadFab(
                                    onClick = ::pickFileFromExistingEntry,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .zIndex(2f)
                                        .padding(
                                            end = CompactHomeMetrics.FabEndPadding,
                                            bottom = CompactHomeMetrics.FabBottomPadding,
                                        ),
                                )
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
                                    errorMessage = manualAddErrorMessage,
                                    onDraftTagsChange = { text ->
                                        appState.draftTags = text
                                        manualAddErrorMessage = null
                                    },
                                    onDismiss = {
                                        manualAddNotice = null
                                        manualAddErrorMessage = null
                                        showManualAddDialog = false
                                        referenceEditorMode = ReferenceEditorMode.Add
                                        referenceEditorTargetId = null
                                        draftFileSizeText = ""
                                    },
                                    onConfirm = {
                                        var shouldCloseDialog = true
                                        when (referenceEditorMode) {
                                            ReferenceEditorMode.Add -> {
                                                val tagValidationMessage = formalTagsLengthValidationMessage(appState.draftTags)
                                                if (tagValidationMessage != null) {
                                                    manualAddErrorMessage = tagValidationMessage
                                                    shouldCloseDialog = false
                                                } else {
                                                    val saved = appState.addDraftReference()
                                                    coroutineScope.launch {
                                                        appState.generateThumbnailForReference(saved.id)
                                                    }
                                                    currentPage = AppPage.Home
                                                }
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
                                        if (shouldCloseDialog) {
                                            clearDraftFields(appState)
                                            draftFileSizeText = ""
                                            manualAddNotice = null
                                            manualAddErrorMessage = null
                                            showManualAddDialog = false
                                            referenceEditorMode = ReferenceEditorMode.Add
                                            referenceEditorTargetId = null
                                        }
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
                                            color = TaggoGlobalColors.TextPrimary,
                                        )
                                    },
                                    text = {
                                        Text(
                                            text = displayTextForUi(reference?.title.orEmpty(), true),
                                            color = TaggoGlobalColors.TextSecondary,
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

                            SnackbarHost(
                                hostState = snapshotSnackbarHostState,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .zIndex(4f)
                                    .padding(
                                        start = TaggoGlobalSpacing.Md,
                                        end = TaggoGlobalSpacing.Md,
                                        bottom = 0.dp,
                                    ),
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
    onSearch: () -> Unit,
    onOpenReference: (FileReference) -> Unit,
    onOpenFile: (FileReference) -> Unit,
    onOpenTags: () -> Unit,
    onOpenAllFiles: () -> Unit,
    onOpenTypeFilter: (AllFilesTypeFilter) -> Unit,
    onTagClick: (String) -> Unit,
) {
    LaunchedEffect(Unit) {
        reportStartupTimeline("Home first composition")
    }

    // 首页推荐面板只消费只读推荐结果，不在 UI 层直接拼装推荐算法输入。
    val recommendedReferences = resolveRecommendedReferences(appState)
    val scoredRecommendedReferences = appState.scoredRecommendedReferences
    val windowSizeClass = LocalTaggoWindowSizeClass.current
    val compactLayout = windowSizeClass == TaggoWindowSizeClass.Compact
    val expandedLayout = windowSizeClass == TaggoWindowSizeClass.Expanded
    val compactScrollState = rememberScrollState()
    val homeSpacing = if (compactLayout) TaggoGlobalSpacing.Sm else 12.dp

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentAlignment = Alignment.TopCenter,
    ) {
        val showExpandedDashboard = expandedLayout && maxHeight > MediumHomeMetrics.MaxDashboardHeight

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (showExpandedDashboard) {
                        Modifier
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF090810),
                                        Color(0xFF140D24),
                                        Color(0xFF201038),
                                        Color(0xFF130D26),
                                        Color(0xFF07070D),
                                    ),
                                ),
                            )
                    } else if (compactLayout) {
                        Modifier.background(TaggoTheme.colors.backgroundBrush)
                    } else {
                        Modifier
                    },
                ),
            contentAlignment = Alignment.TopCenter,
        ) {
            LaunchedEffect(appState.allReferences.size, recommendedReferences.size) {
                reportStartupTimeline(
                    "Home key content visible files=${appState.allReferences.size} recommended=${recommendedReferences.size}"
                )
            }
            Column(
                modifier = expandedPageContentModifier()
                    .then(
                        when {
                            showExpandedDashboard -> Modifier
                                .fillMaxHeight()
                                .padding(start = 24.dp, top = 20.dp, end = 20.dp, bottom = 14.dp)

                            compactLayout -> Modifier
                                .fillMaxHeight()
                                .verticalScroll(compactScrollState)

                            else -> Modifier
                        },
                    ),
                verticalArrangement = Arrangement.spacedBy(homeSpacing),
            ) {
                if (compactLayout) {
                    CompactHomeTopBar(
                        locale = locale,
                        searchDraft = searchDraft,
                        searchPlaceholder = if (locale == AppLocale.ZhCn) "\u641c\u7d22\u6587\u4ef6\u6216\u6807\u7b7e..." else "Search files or tags...",
                        onSearch = onSearch,
                        onOpenMenu = onOpenMenu,
                    )
                }

                if (showExpandedDashboard) {
                    HomeExpandedDashboard(
                        appState = appState,
                        locale = locale,
                        fullCjkFontReady = fullCjkFontReady,
                        fullCjkTextStyle = fullCjkTextStyle,
                        fullCjkFontFamily = fullCjkFontFamily,
                        searchDraft = searchDraft,
                        recommendedReferences = recommendedReferences,
                        onSearchDraftChange = onSearchDraftChange,
                        onSearch = onSearch,
                        onOpenReference = onOpenReference,
                        onOpenTags = onOpenTags,
                        onOpenAllFiles = onOpenAllFiles,
                        onOpenTypeFilter = onOpenTypeFilter,
                        onTagClick = onTagClick,
                    )
                } else if (compactLayout) {
                    HomeCompactDashboard(
                        appState = appState,
                        locale = locale,
                        fullCjkFontReady = fullCjkFontReady,
                        fullCjkFontFamily = fullCjkFontFamily,
                        scoredRecommendedReferences = scoredRecommendedReferences.take(10),
                        onOpenReference = onOpenReference,
                        onOpenFile = onOpenFile,
                        onOpenTags = onOpenTags,
                        onOpenAllFiles = onOpenAllFiles,
                        onOpenTypeFilter = onOpenTypeFilter,
                        onTagClick = onTagClick,
                    )
                } else {
                    HomeMediumDashboard(
                        appState = appState,
                        locale = locale,
                        fullCjkFontReady = fullCjkFontReady,
                        fullCjkTextStyle = fullCjkTextStyle,
                        fullCjkFontFamily = fullCjkFontFamily,
                        searchDraft = searchDraft,
                        scoredRecommendedReferences = scoredRecommendedReferences,
                        onSearchDraftChange = onSearchDraftChange,
                        onOpenMenu = onOpenMenu,
                        onSearch = onSearch,
                        onOpenReference = onOpenReference,
                        onOpenFile = onOpenFile,
                        onOpenTags = onOpenTags,
                        onOpenAllFiles = onOpenAllFiles,
                        onOpenTypeFilter = onOpenTypeFilter,
                        onTagClick = onTagClick,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeExpandedDashboard(
    appState: FileManagerAppState,
    locale: AppLocale,
    fullCjkFontReady: Boolean,
    fullCjkTextStyle: androidx.compose.ui.text.TextStyle,
    fullCjkFontFamily: FontFamily,
    searchDraft: String,
    recommendedReferences: List<FileReference>,
    onSearchDraftChange: (String) -> Unit,
    onSearch: () -> Unit,
    onOpenReference: (FileReference) -> Unit,
    onOpenTags: () -> Unit,
    onOpenAllFiles: () -> Unit,
    onOpenTypeFilter: (AllFilesTypeFilter) -> Unit,
    onTagClick: (String) -> Unit,
) {
    val recentItems = appState.recentAddedReferences.take(3)
    val tagSummaries = remember(appState.snapshotVersion, appState.topTags) {
        resolveDashboardTagSummaries(appState.allReferences, appState.topTags)
    }
    val typeSummaries = remember(appState.snapshotVersion) {
        resolveDashboardTypeSummaries(appState.allReferences)
    }
    val entryTags = tagSummaries.take(6)
    val entryTypes = typeSummaries.take(3)
    val recommendationScrollState = rememberScrollState()
    val recentCountLabel = if (locale == AppLocale.ZhCn) {
        "${appState.recentAddedReferences.size} 个文件"
    } else {
        "${appState.recentAddedReferences.size} files"
    }
    val tagCountLabel = if (locale == AppLocale.ZhCn) {
        "${tagSummaries.size} 个标签"
    } else {
        "${tagSummaries.size} tags"
    }
    val typeCountLabel = if (locale == AppLocale.ZhCn) {
        "${typeSummaries.size} 种类型"
    } else {
        "${typeSummaries.size} types"
    }
    val entryPanelHeight = HomeWide.Size.PanelHeight
    val recommendationPanelMinHeight = HomeWide.Size.PanelHeight

    CompositionLocalProvider(
        LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = fullCjkFontFamily),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(HomeWide.Spacing.PanelGap),
        ) {
            DashboardHeroHeader(
                locale = locale,
                searchDraft = searchDraft,
                searchPlaceholder = appState.searchPlaceholder,
                textStyle = fullCjkTextStyle,
                onSearchDraftChange = onSearchDraftChange,
                onSearch = onSearch,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(HomeWide.Spacing.PanelGap),
                verticalAlignment = Alignment.Top,
            ) {
                TaggoSectionCard(
                    title = appState.recentlyAdded,
                    meta = recentCountLabel,
                    modifier = Modifier
                        .weight(1f)
                        .height(entryPanelHeight),
                    topEntryPanel = true,
                    footer = {
                        TaggoMoreButton(
                            label = if (locale == AppLocale.ZhCn) "查看全部 \u2192" else "View all \u2192",
                            onClick = onOpenAllFiles,
                        )
                    },
                ) {
                    if (recentItems.isEmpty()) {
                        TaggoEmptyState(
                            title = if (locale == AppLocale.ZhCn) "\u8fd8\u6ca1\u6709\u65b0\u6587\u4ef6" else "No recent files yet",
                            description = if (locale == AppLocale.ZhCn) "\u4e0a\u4f20\u540e\u4f1a\u5728\u8fd9\u91cc\u663e\u793a" else "Uploads will appear here",
                            compact = true,
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(HomeWide.Spacing.RecentListGap)) {
                            recentItems.forEach { reference ->
                                DashboardRecentFileRow(
                                    reference = reference,
                                    fullCjkFontReady = fullCjkFontReady,
                                    fullCjkFontFamily = fullCjkFontFamily,
                                    onOpen = { onOpenReference(reference) },
                                )
                            }
                        }
                    }
                }

                TaggoSectionCard(
                    title = if (locale == AppLocale.ZhCn) "标签" else "Tags",
                    meta = tagCountLabel,
                    modifier = Modifier
                        .weight(1f)
                        .height(entryPanelHeight),
                    topEntryPanel = true,
                    footer = {
                        TaggoMoreButton(
                            label = if (locale == AppLocale.ZhCn) "查看全部 \u2192" else "View all \u2192",
                            onClick = onOpenTags,
                        )
                    },
                ) {
                    if (entryTags.isEmpty()) {
                        TaggoEmptyState(
                            title = if (locale == AppLocale.ZhCn) "暂无标签" else "No tags yet",
                            description = if (locale == AppLocale.ZhCn) "添加标签后形成两列入口" else "Tagged files form two-column entries",
                            compact = true,
                        )
                    } else {
                        DashboardTagGrid(
                            tags = entryTags,
                            fullCjkFontReady = fullCjkFontReady,
                            fullCjkFontFamily = fullCjkFontFamily,
                            onTagClick = onTagClick,
                        )
                    }
                }

                TaggoSectionCard(
                    title = if (locale == AppLocale.ZhCn) "文件类型" else "File types",
                    meta = typeCountLabel,
                    modifier = Modifier
                        .weight(1f)
                        .height(entryPanelHeight),
                    topEntryPanel = true,
                    footer = {
                        TaggoMoreButton(
                            label = if (locale == AppLocale.ZhCn) "查看全部 \u2192" else "View all \u2192",
                            onClick = onOpenAllFiles,
                        )
                    },
                ) {
                    if (entryTypes.isEmpty()) {
                        TaggoEmptyState(
                            title = if (locale == AppLocale.ZhCn) "暂无文件类型" else "No file types yet",
                            description = if (locale == AppLocale.ZhCn) "导入文件后保留比例条" else "Type bars appear after import",
                            compact = true,
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(HomeWide.Spacing.TagGridGap)) {
                            entryTypes.forEach { summary ->
                                DashboardTypeRow(
                                    summary = summary,
                                    locale = locale,
                                    onClick = { onOpenTypeFilter(summary.filter) },
                                )
                            }
                        }
                    }
                }
            }

            TaggoSectionCard(
                title = if (locale == AppLocale.ZhCn) "智能推荐" else "Recommended files",
                meta = if (locale == AppLocale.ZhCn) {
                    "算法会综合标签匹配、历史行为、最近检索和标签共现。"
                } else {
                    "Recommendations combine tag matching, history, recent search, and tag co-occurrence."
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .heightIn(min = recommendationPanelMinHeight),
                prominent = true,
                trailing = {
                    DashboardCountBadge(
                        label = if (locale == AppLocale.ZhCn) {
                            "${recommendedReferences.size} 个推荐"
                        } else {
                            "${recommendedReferences.size} recommendations"
                        },
                    )
                },
            ) {
                if (recommendedReferences.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        TaggoEmptyState(
                            title = if (locale == AppLocale.ZhCn) "暂无推荐" else "No recommendations",
                            description = if (locale == AppLocale.ZhCn) "积累打开记录后生成" else "Open history will create signals",
                            compact = true,
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(recommendationScrollState),
                    ) {
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

private object MediumHomeMetrics {
    val MaxDashboardHeight = 560.dp
    val SidebarWidth = 128.dp
    val SidebarHorizontalPadding = 12.dp
    val SidebarVerticalPadding = 12.dp
    val SidebarBrandHeight = 40.dp
    val SidebarBrandNavigationGap = 32.dp
    val SidebarLogoSize = 24.dp
    val SidebarBrandFontSize = 18.sp
    val SidebarNavigationItemHeight = 40.dp
    val SidebarNavigationItemGap = 10.dp
    val SidebarNavigationHorizontalPadding = 12.dp
    val SidebarNavigationIconSize = 20.dp
    val SidebarNavigationFontSize = 13.sp
    val SidebarUploadWidth = 84.dp
    val SidebarUploadHeight = 34.dp
    val SidebarUploadCornerRadius = 10.dp
    const val SidebarUploadBackgroundAlpha = 0.08f
    const val SidebarUploadBorderAlpha = 0.14f
    const val SidebarUploadContentAlpha = 0.78f
    const val SidebarSelectedBackgroundAlpha = 0.78f
    const val SidebarSelectedContentAlpha = 0.88f
    const val SidebarSelectedIconAlpha = 0.82f
    const val SidebarUnselectedTextAlpha = 0.72f
    const val SidebarUnselectedIconAlpha = 0.68f
    val PageHorizontalPadding = 12.dp
    val PageVerticalPadding = 22.dp
    val SectionGap = 14.dp
    val ColumnGap = 14.dp
    val HeaderHeight = 34.dp
    val HeaderTitleGap = 2.dp
    val HeaderTitleWidth = 166.dp
    val HeaderSearchGap = 28.dp
    val AccountSize = 32.dp
    val AccountIconSize = 18.dp
    val SearchHeight = 32.dp
    val SearchWidthCompact = 250.dp
    val SearchWidthRegular = 290.dp
    val SearchWidthLarge = 310.dp
    val SupportCardGap = 10.dp
    val SupportColumnWidthCompact = 260.dp
    val SupportColumnWidthRegular = 280.dp
    val SupportColumnWidthLarge = 300.dp
    val FooterHeight = 32.dp
    val FooterTopSpacing = 6.dp
    val RecommendationHorizontalPadding = 18.dp
    val RecommendationTopPadding = 16.dp
    val RecommendationBottomPadding = 14.dp
    val RecommendationHeaderHeight = 30.dp
    val RecommendationListTopSpacing = 10.dp
    val RecommendationRowHeight = 56.dp
    val RecommendationRowGap = 7.dp
    val RecommendationIconSize = TaggoFileCoverTokens.MediumRecommendationCoverSize
    val RecommendationRowPadding = 10.dp
    val RecommendationRowVerticalPadding = 6.dp
    val RecommendationBadgeIconSize = 12.dp
    val CoverOverlaySize = 14.dp
    val CoverOverlayIconSize = 9.dp
    val CoverOverlayEdgePadding = 4.dp
    val RecentRowHeight = 26.dp
    val RecentIconSize = 24.dp
    val RecentTimeWidth = 48.dp
    val RecentRowGap = 6.dp
    val TagCountWidth = 20.dp
    val TagItemHeight = 30.dp
    val TagItemHorizontalPadding = 10.dp
    val TagGridStartPadding = 8.dp
    val TagGridHorizontalGap = 8.dp
    val TagGridVerticalGap = 8.dp
    val TagVisibleItemCount = 6
    val TypeVisibleItemCount = 4
    val TypeRowHeight = 22.dp
    val TypeNameWidth = 42.dp
    val TypeCountWidth = 24.dp
    val TypeBarHeight = 4.dp
    val TypeRowGap = 4.dp
    val PageTitleFontSize = 16.sp
    val PageTitleLineHeight = 20.sp
    val PageSubtitleFontSize = 11.sp
    val SectionTitleFontSize = 14.sp
    val SectionTitleLineHeight = 18.sp
    val SectionTitleHeight = 28.dp
    val RecommendationSectionTitleFontSize = 14.sp
    val RecommendationTitleFontSize = 15.sp
    val RecentTitleFontSize = 12.sp
    val AuxiliaryFontSize = 10.sp
    val RecommendationTagChipHeight = 20.dp
    val RecommendationTagChipMaxWidth = 76.dp
    val RecommendationTagChipHorizontalPadding = 6.dp
    val RecommendationTagChipGap = 4.dp
    val RecommendationTagChipFontSize = 10.sp
    val TypeIconSize = 18.dp
    val TypeNameFontSize = 11.sp
    val CountFontSize = 10.sp
    val RecommendationBadgeHorizontalPadding = 6.dp
    val RecommendationBadgeVerticalPadding = 2.dp
    const val RecommendationBadgeBackgroundAlpha = 0.4f
    const val RecommendationBadgeIconAlpha = 0.50f
    const val RecommendationCardBackgroundAlpha = 0.82f
    const val RecommendationCardBorderAlpha = 0.58f
    const val RecommendationItemBackgroundAlpha = 0.42f
    const val RecommendationItemBorderAlpha = 0.38f
    const val SupportCardBackgroundAlpha = 0.70f
    const val SupportCardBorderAlpha = 0.46f
    const val SupportTitleAlpha = 0.90f
    const val SupportFileNameAlpha = 0.88f
    const val SupportLabelAlpha = 0.82f
    const val ViewAllAlpha = 0.80f
    const val CoverOverlayBackgroundAlpha = 0.58f
    const val CoverOverlayBorderAlpha = 0.46f
    const val RecommendationTagChipBackgroundAlpha = 0.46f
    const val TagItemBackgroundAlpha = 0.34f
    const val TagItemBorderAlpha = 0.42f
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeMediumDashboard(
    appState: FileManagerAppState,
    locale: AppLocale,
    fullCjkFontReady: Boolean,
    fullCjkTextStyle: androidx.compose.ui.text.TextStyle,
    fullCjkFontFamily: FontFamily,
    searchDraft: String,
    scoredRecommendedReferences: List<ScoredRecommendation>,
    onSearchDraftChange: (String) -> Unit,
    onOpenMenu: () -> Unit,
    onSearch: () -> Unit,
    onOpenReference: (FileReference) -> Unit,
    onOpenFile: (FileReference) -> Unit,
    onOpenTags: () -> Unit,
    onOpenAllFiles: () -> Unit,
    onOpenTypeFilter: (AllFilesTypeFilter) -> Unit,
    onTagClick: (String) -> Unit,
) {
    val tagSummaries = remember(appState.snapshotVersion, appState.topTags) {
        resolveDashboardTagSummaries(appState.allReferences, appState.topTags)
    }
    val typeSummaries = remember(appState.snapshotVersion) {
        resolveDashboardTypeSummaries(appState.allReferences)
    }
    CompositionLocalProvider(
        LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = fullCjkFontFamily),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val recentItemLimit = when {
                maxHeight <= 390.dp -> 2
                maxHeight <= 430.dp -> 3
                else -> 4
            }
            val supportColumnWidth = when {
                maxWidth <= 720.dp -> MediumHomeMetrics.SupportColumnWidthCompact
                maxWidth <= 800.dp -> MediumHomeMetrics.SupportColumnWidthRegular
                else -> MediumHomeMetrics.SupportColumnWidthLarge
            }
            val mediumRecommendedForUi = if (scoredRecommendedReferences.isNotEmpty()) {
                scoredRecommendedReferences
            } else {
                appState.recentAddedReferences.map { reference ->
                    ScoredRecommendation(
                        file = reference,
                        intervalScore = 0.0,
                        transitionScore = 0.0,
                        recencyScore = 0.0,
                        finalScore = 0.0,
                    )
                }
            }
            val pageScrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(pageScrollState)
                    .padding(
                        horizontal = MediumHomeMetrics.PageHorizontalPadding,
                        vertical = MediumHomeMetrics.PageVerticalPadding,
                    ),
                verticalArrangement = Arrangement.spacedBy(MediumHomeMetrics.SectionGap),
            ) {
                MediumHomeHeader(
                    locale = locale,
                    searchDraft = searchDraft,
                    searchPlaceholder = appState.searchPlaceholder,
                    textStyle = fullCjkTextStyle,
                    onSearchDraftChange = onSearchDraftChange,
                    onSearch = onSearch,
                    onOpenMenu = onOpenMenu,
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 420.dp),
                    horizontalArrangement = Arrangement.spacedBy(MediumHomeMetrics.ColumnGap),
                    verticalAlignment = Alignment.Top,
                ) {
                    MediumSmartRecommendationCard(
                        recommendations = mediumRecommendedForUi,
                        locale = locale,
                        fullCjkFontReady = fullCjkFontReady,
                        fullCjkFontFamily = fullCjkFontFamily,
                        onOpenFile = onOpenFile,
                        onOpenReference = onOpenReference,
                        modifier = Modifier
                            .weight(1f),
                    )

                    Column(
                        modifier = Modifier.width(supportColumnWidth),
                        verticalArrangement = Arrangement.spacedBy(MediumHomeMetrics.SupportCardGap),
                    ) {
                        MediumRecentCard(
                            title = appState.recentlyAdded,
                            items = appState.recentAddedReferences,
                            itemLimit = recentItemLimit,
                            locale = locale,
                            fullCjkFontReady = fullCjkFontReady,
                            fullCjkFontFamily = fullCjkFontFamily,
                            onOpenAllFiles = onOpenAllFiles,
                            onOpenFile = onOpenFile,
                            onOpenReference = onOpenReference,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        MediumTagsCard(
                            tags = tagSummaries,
                            locale = locale,
                            fullCjkFontReady = fullCjkFontReady,
                            fullCjkFontFamily = fullCjkFontFamily,
                            onOpenTags = onOpenTags,
                            onTagClick = onTagClick,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        MediumFileTypesCard(
                            types = typeSummaries,
                            locale = locale,
                            onOpenAllFiles = onOpenAllFiles,
                            onOpenTypeFilter = onOpenTypeFilter,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MediumCardFooter(
    label: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Spacer(modifier = Modifier.height(MediumHomeMetrics.FooterTopSpacing))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(MediumHomeMetrics.FooterHeight),
            contentAlignment = Alignment.CenterEnd,
        ) {
            TaggoMoreButton(
                label = label,
                onClick = onClick,
                compact = true,
            )
        }
    }
}

@Composable
private fun MediumSupportCard(
    title: String,
    actionLabel: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)
    Card(
        colors = CardDefaults.cardColors(
            containerColor = TaggoGlobalColors.PanelBackgroundSoft.copy(
                alpha = MediumHomeMetrics.SupportCardBackgroundAlpha,
            ),
        ),
        shape = shape,
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                TaggoGlobalColors.Border.copy(alpha = MediumHomeMetrics.SupportCardBorderAlpha),
                shape,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 14.dp,
                    top = 12.dp,
                    end = 14.dp,
                    bottom = 10.dp,
                ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MediumHomeMetrics.SectionTitleHeight),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    color = TaggoGlobalColors.TextPrimary.copy(
                        alpha = MediumHomeMetrics.SupportTitleAlpha,
                    ),
                    fontSize = MediumHomeMetrics.SectionTitleFontSize,
                    lineHeight = MediumHomeMetrics.SectionTitleLineHeight,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                TaggoMoreButton(
                    label = actionLabel,
                    onClick = onActionClick,
                    modifier = Modifier.alpha(MediumHomeMetrics.ViewAllAlpha),
                    compact = true,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopStart,
            ) {
                content()
            }
        }
    }
}

@Composable
private fun MediumRecentCard(
    title: String,
    items: List<FileReference>,
    itemLimit: Int,
    locale: AppLocale,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    onOpenAllFiles: () -> Unit,
    onOpenFile: (FileReference) -> Unit,
    onOpenReference: (FileReference) -> Unit,
    modifier: Modifier = Modifier,
) {
    val visibleItems = items.take(itemLimit)
    MediumSupportCard(
        title = title,
        modifier = modifier,
        actionLabel = if (locale == AppLocale.ZhCn) "\u67e5\u770b\u5168\u90e8" else "View all",
        onActionClick = onOpenAllFiles,
    ) {
        if (visibleItems.isEmpty()) {
            TaggoEmptyState(
                title = if (locale == AppLocale.ZhCn) "\u8fd8\u6ca1\u6709\u65b0\u6587\u4ef6" else "No recent files yet",
                description = if (locale == AppLocale.ZhCn) "\u5bfc\u5165\u540e\u4f1a\u5728\u8fd9\u91cc\u663e\u793a\u3002" else "Imported files will appear here.",
                compact = true,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(MediumHomeMetrics.RecentRowGap)) {
                visibleItems.forEach { reference ->
                    MediumRecentFileRow(
                        reference = reference,
                        locale = locale,
                        fullCjkFontReady = fullCjkFontReady,
                        fullCjkFontFamily = fullCjkFontFamily,
                        onOpenFile = { onOpenFile(reference) },
                        onViewDetails = { onOpenReference(reference) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MediumTagsCard(
    tags: List<DashboardTagSummary>,
    locale: AppLocale,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    onOpenTags: () -> Unit,
    onTagClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val visibleTags = tags.take(MediumHomeMetrics.TagVisibleItemCount)
    MediumSupportCard(
        title = if (locale == AppLocale.ZhCn) "\u5e38\u7528\u6807\u7b7e" else "Tags",
        modifier = modifier,
        actionLabel = if (locale == AppLocale.ZhCn) "\u67e5\u770b\u5168\u90e8" else "View all",
        onActionClick = onOpenTags,
    ) {
        if (visibleTags.isEmpty()) {
            TaggoEmptyState(
                title = if (locale == AppLocale.ZhCn) "\u6682\u65e0\u6807\u7b7e" else "No tags yet",
                description = if (locale == AppLocale.ZhCn) "\u6dfb\u52a0\u6807\u7b7e\u540e\u4f1a\u5728\u8fd9\u91cc\u663e\u793a\u3002" else "Tagged files will appear here.",
                compact = true,
            )
        } else {
            MediumTagGrid(
                tags = visibleTags,
                fullCjkFontReady = fullCjkFontReady,
                fullCjkFontFamily = fullCjkFontFamily,
                onTagClick = onTagClick,
            )
        }
    }
}

@Composable
private fun MediumFileTypesCard(
    types: List<DashboardTypeSummary>,
    locale: AppLocale,
    onOpenAllFiles: () -> Unit,
    onOpenTypeFilter: (AllFilesTypeFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val visibleTypes = types
        .filter { it.count > 0 }
        .take(MediumHomeMetrics.TypeVisibleItemCount)
    MediumSupportCard(
        title = if (locale == AppLocale.ZhCn) "\u6587\u4ef6\u7c7b\u578b" else "File types",
        modifier = modifier,
        actionLabel = if (locale == AppLocale.ZhCn) "\u67e5\u770b\u5168\u90e8" else "View all",
        onActionClick = onOpenAllFiles,
    ) {
        if (visibleTypes.isEmpty()) {
            TaggoEmptyState(
                title = if (locale == AppLocale.ZhCn) "\u6682\u65e0\u6587\u4ef6\u7c7b\u578b" else "No file types yet",
                description = if (locale == AppLocale.ZhCn) "\u5bfc\u5165\u6587\u4ef6\u540e\u4f1a\u663e\u793a\u7edf\u8ba1\u3002" else "Type statistics appear after import.",
                compact = true,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(MediumHomeMetrics.TypeRowGap)) {
                visibleTypes.forEach { summary ->
                    MediumFileTypeRow(
                        summary = summary,
                        locale = locale,
                        onClick = { onOpenTypeFilter(summary.filter) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MediumSmartRecommendationCard(
    recommendations: List<ScoredRecommendation>,
    locale: AppLocale,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    onOpenFile: (FileReference) -> Unit,
    onOpenReference: (FileReference) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(18.dp)
    Card(
        colors = CardDefaults.cardColors(
            containerColor = TaggoGlobalColors.PanelBackgroundSoft.copy(
                alpha = MediumHomeMetrics.RecommendationCardBackgroundAlpha,
            ),
        ),
        shape = shape,
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                TaggoGlobalColors.Border.copy(alpha = MediumHomeMetrics.RecommendationCardBorderAlpha),
                shape,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = MediumHomeMetrics.RecommendationHorizontalPadding,
                    top = MediumHomeMetrics.RecommendationTopPadding,
                    end = MediumHomeMetrics.RecommendationHorizontalPadding,
                    bottom = MediumHomeMetrics.RecommendationBottomPadding,
                ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MediumHomeMetrics.RecommendationHeaderHeight),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (locale == AppLocale.ZhCn) "\u667a\u80fd\u63a8\u8350" else "Recommended files",
                    modifier = Modifier.weight(1f),
                    color = TaggoGlobalColors.TextPrimary,
                    fontSize = MediumHomeMetrics.RecommendationSectionTitleFontSize,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                MediumCountBadge(
                    label = if (locale == AppLocale.ZhCn) {
                        "${recommendations.size} \u4e2a\u63a8\u8350"
                    } else {
                        "${recommendations.size} recommendations"
                    },
                )
            }

            Spacer(modifier = Modifier.height(MediumHomeMetrics.RecommendationListTopSpacing))

            if (recommendations.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    TaggoEmptyState(
                        title = if (locale == AppLocale.ZhCn) "\u6682\u65e0\u63a8\u8350" else "No recommendations",
                        description = if (locale == AppLocale.ZhCn) "\u79ef\u7d2f\u6253\u5f00\u8bb0\u5f55\u540e\u4f1a\u751f\u6210\u3002" else "Open history will create signals.",
                        compact = true,
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(MediumHomeMetrics.RecommendationRowGap),
                ) {
                    recommendations.forEach { recommendation ->
                        MediumRecommendedFileRow(
                            recommendation = recommendation,
                            locale = locale,
                            fullCjkFontReady = fullCjkFontReady,
                            fullCjkFontFamily = fullCjkFontFamily,
                            onOpenFile = { onOpenFile(recommendation.file) },
                            onViewDetails = { onOpenReference(recommendation.file) },
                        )
                    }
                }
            }
    }
}
}

@Composable
private fun MediumHomeHeader(
    locale: AppLocale,
    searchDraft: String,
    searchPlaceholder: String,
    textStyle: androidx.compose.ui.text.TextStyle,
    onSearchDraftChange: (String) -> Unit,
    onSearch: () -> Unit,
    onOpenMenu: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val searchWidth = when {
            maxWidth <= 720.dp -> MediumHomeMetrics.SearchWidthCompact
            maxWidth <= 800.dp -> MediumHomeMetrics.SearchWidthRegular
            else -> MediumHomeMetrics.SearchWidthLarge
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(MediumHomeMetrics.HeaderHeight),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (locale == AppLocale.ZhCn) "\u63a8\u8350" else "Recommended",
                modifier = Modifier.width(MediumHomeMetrics.HeaderTitleWidth),
                color = TaggoGlobalColors.TextPrimary,
                fontSize = MediumHomeMetrics.PageTitleFontSize,
                lineHeight = MediumHomeMetrics.PageTitleLineHeight,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.width(MediumHomeMetrics.HeaderSearchGap))
            MediumHomeSearchField(
                query = searchDraft,
                placeholder = searchPlaceholder,
                textStyle = textStyle,
                onQueryChange = onSearchDraftChange,
                onSearch = onSearch,
                modifier = Modifier.width(searchWidth),
            )
            Spacer(modifier = Modifier.weight(1f))
            MediumAccountButton(
                locale = locale,
                onClick = onOpenMenu,
            )
        }
    }
}

@Composable
private fun MediumHomeSearchField(
    query: String,
    placeholder: String,
    textStyle: androidx.compose.ui.text.TextStyle,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }
    val searchShape = RoundedCornerShape(TaggoGlobalRadius.Search)
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .height(MediumHomeMetrics.SearchHeight)
            .clip(searchShape)
            .background(TaggoGlobalColors.PanelBackgroundSoft)
            .border(
                width = 1.dp,
                color = if (focused) {
                    TaggoGlobalColors.PrimaryAccent
                } else {
                    TaggoGlobalColors.BorderStrong.copy(alpha = 0.66f)
                },
                shape = searchShape,
            )
            .onFocusChanged { focused = it.isFocused },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        textStyle = textStyle.copy(
            color = TaggoGlobalColors.TextPrimary,
            fontSize = 11.sp,
            lineHeight = 16.sp,
        ),
        cursorBrush = SolidColor(TaggoGlobalColors.PrimaryAccent),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = if (query.isBlank()) placeholder else query,
                    tint = TaggoGlobalColors.TextSecondary,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable(onClick = onSearch),
                )
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (query.isBlank()) {
                        Text(
                            text = placeholder,
                            color = TaggoGlobalColors.TextMuted.copy(alpha = 0.88f),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    innerTextField()
                }
            }
        },
    )
}

@Composable
private fun MediumAccountButton(
    locale: AppLocale,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(TaggoGlobalRadius.Badge),
        color = TaggoGlobalColors.PanelBackgroundSoft,
        contentColor = TaggoGlobalColors.TextPrimary,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.size(MediumHomeMetrics.AccountSize),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 1.dp,
                    color = TaggoGlobalColors.BorderStrong,
                    shape = RoundedCornerShape(TaggoGlobalRadius.Badge),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.AccountCircle,
                contentDescription = if (locale == AppLocale.ZhCn) "\u8d26\u53f7" else "Account",
                tint = TaggoGlobalColors.TextPrimary,
                modifier = Modifier.size(MediumHomeMetrics.AccountIconSize),
            )
        }
    }
}

@Composable
private fun MediumRecommendedFileRow(
    recommendation: ScoredRecommendation,
    locale: AppLocale,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    onOpenFile: () -> Unit,
    onViewDetails: () -> Unit,
) {
    val reference = recommendation.file
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(MediumHomeMetrics.RecommendationRowHeight)
            .clip(shape)
            .background(
                TaggoGlobalColors.ItemBackground.copy(
                    alpha = if (hovered) {
                        MediumHomeMetrics.RecommendationItemBackgroundAlpha * 1.08f
                    } else {
                        MediumHomeMetrics.RecommendationItemBackgroundAlpha
                    },
                ),
            )
            .border(
                1.dp,
                TaggoGlobalColors.Border.copy(
                    alpha = MediumHomeMetrics.RecommendationItemBorderAlpha,
                ),
                shape,
            )
            .hoverable(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onViewDetails,
            )
            .padding(
                horizontal = MediumHomeMetrics.RecommendationRowPadding,
                vertical = MediumHomeMetrics.RecommendationRowVerticalPadding,
            ),
        horizontalArrangement = Arrangement.spacedBy(TaggoGlobalSpacing.Sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MediumOpenableCover(
            reference = reference,
            fullCjkFontReady = fullCjkFontReady,
            fullCjkFontFamily = fullCjkFontFamily,
            modifier = Modifier.size(MediumHomeMetrics.RecommendationIconSize),
            cornerRadius = 10.dp,
            iconSize = TaggoFileCoverTokens.MediumRecommendationIconSize,
            overlayContainerSize = MediumHomeMetrics.CoverOverlaySize,
            overlayIconSize = MediumHomeMetrics.CoverOverlayIconSize,
            overlayBackgroundAlpha = 0.12f,
            overlayIconAlpha = 0.42f,
            onOpenFile = onOpenFile,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .height(MediumHomeMetrics.RecommendationIconSize),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = displayTextForUi(reference.title, fullCjkFontReady),
                    modifier = Modifier.weight(1f, fill = false),
                    color = TaggoGlobalColors.TextPrimary,
                    fontSize = MediumHomeMetrics.RecommendationTitleFontSize,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = fullCjkFontFamily,
                )
                Spacer(modifier = Modifier.width(5.dp))
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = TaggoGlobalColors.PrimaryAccent.copy(
                        alpha = MediumHomeMetrics.RecommendationBadgeIconAlpha,
                    ),
                    modifier = Modifier.size(MediumHomeMetrics.RecommendationBadgeIconSize),
                )
            }
            TaggoInlineTagSummary(
                tags = reference.tags,
                fullCjkFontReady = fullCjkFontReady,
                fullCjkFontFamily = fullCjkFontFamily,
                locale = locale,
                modifier = Modifier.fillMaxWidth(),
                mode = TaggoInlineTagSummaryMode.Recommendation(
                    style = androidx.compose.ui.text.TextStyle(
                        color = TaggoGlobalColors.TextSecondary.copy(alpha = 0.90f),
                        fontSize = 12.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = fullCjkFontFamily,
                    ),
                ),
            )
        }
    }
}

@Composable
private fun MediumRecentFileRow(
    reference: FileReference,
    locale: AppLocale,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    onOpenFile: () -> Unit,
    onViewDetails: () -> Unit,
) {
    val addedTime = remember(reference.createdAtMillis, locale) {
        compactRelativeTimeLabel(reference.createdAtMillis, locale)
    }
    TaggoListItemSurface(
        shape = RoundedCornerShape(TaggoGlobalRadius.Item),
        backgroundColor = TaggoGlobalColors.ItemBackground.copy(alpha = 0.28f),
        borderColor = Color.Transparent,
        height = MediumHomeMetrics.RecentRowHeight,
        borderWidth = 0.dp,
        contentPadding = PaddingValues(horizontal = TaggoGlobalSpacing.Sm),
        horizontalArrangement = Arrangement.spacedBy(TaggoGlobalSpacing.Sm),
        onClick = onViewDetails,
    ) {
        MediumOpenableCover(
            reference = reference,
            fullCjkFontReady = fullCjkFontReady,
            fullCjkFontFamily = fullCjkFontFamily,
            modifier = Modifier.size(MediumHomeMetrics.RecentIconSize),
            cornerRadius = 8.dp,
            iconSize = 18.dp,
            onOpenFile = onOpenFile,
        )
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(TaggoGlobalSpacing.Sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = displayTextForUi(reference.title, fullCjkFontReady),
                modifier = Modifier.weight(1f),
                color = TaggoGlobalColors.TextPrimary.copy(
                    alpha = MediumHomeMetrics.SupportFileNameAlpha,
                ),
                fontSize = MediumHomeMetrics.RecentTitleFontSize,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontFamily = fullCjkFontFamily,
            )
            MediumFileTypeBadge(
                label = compactFileTypeLabel(reference),
                height = 22.dp,
            )
        }
        Text(
            text = addedTime,
            modifier = Modifier.width(MediumHomeMetrics.RecentTimeWidth),
            color = TaggoGlobalColors.TextMuted.copy(alpha = 0.72f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun MediumOpenableCover(
    reference: FileReference,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    cornerRadius: Dp,
    iconSize: Dp,
    overlayContainerSize: Dp = 20.dp,
    overlayIconSize: Dp = 12.dp,
    overlayBackgroundAlpha: Float = 0.14f,
    overlayIconAlpha: Float = 0.46f,
    onOpenFile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconStyle = fileTypeIconStyle(reference)
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .clip(shape)
            .clickable(onClick = onOpenFile),
        contentAlignment = Alignment.Center,
    ) {
        FileCoverArtFrame(
            reference = reference,
            iconStyle = iconStyle,
            fullCjkFontReady = fullCjkFontReady,
            fullCjkFontFamily = fullCjkFontFamily,
            modifier = Modifier.fillMaxSize(),
            cornerShape = shape,
            iconSize = iconSize,
            overlayContainerSize = overlayContainerSize,
            overlayIconSize = overlayIconSize,
            overlayBackgroundAlpha = overlayBackgroundAlpha,
            overlayIconAlpha = overlayIconAlpha,
        )
    }
}

@Composable
private fun MediumCountBadge(label: String) {
    Surface(
        shape = RoundedCornerShape(TaggoGlobalRadius.Badge),
        color = TaggoGlobalColors.PrimaryAccentSoft.copy(
            alpha = MediumHomeMetrics.RecommendationBadgeBackgroundAlpha,
        ),
        contentColor = TaggoGlobalColors.TextSecondary,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(
                horizontal = MediumHomeMetrics.RecommendationBadgeHorizontalPadding,
                vertical = MediumHomeMetrics.RecommendationBadgeVerticalPadding,
            ),
            fontSize = MediumHomeMetrics.CountFontSize,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun MediumFileTypeBadge(
    label: String,
    height: Dp = 30.dp,
) {
    Surface(
        modifier = Modifier
            .widthIn(min = 34.dp)
            .height(height),
        shape = RoundedCornerShape(TaggoGlobalRadius.Badge),
        color = TaggoGlobalColors.SurfaceVariant.copy(alpha = 0.72f),
        contentColor = TaggoGlobalColors.TextSecondary,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MediumTagGrid(
    tags: List<DashboardTagSummary>,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    onTagClick: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MediumHomeMetrics.TagGridVerticalGap),
    ) {
        tags.chunked(2).forEach { rowTags ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MediumHomeMetrics.TagGridHorizontalGap),
            ) {
                rowTags.forEach { summary ->
                    TaggoTagEntryItem(
                        label = summary.tag,
                        count = summary.count,
                        fullCjkFontReady = fullCjkFontReady,
                        fullCjkFontFamily = fullCjkFontFamily,
                        onClick = { onTagClick(summary.tag) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowTags.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MediumFileTypeRow(
    summary: DashboardTypeSummary,
    locale: AppLocale,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = accentForTypeFilter(summary.filter)
    TaggoListItemSurface(
        modifier = modifier,
        shape = RoundedCornerShape(TaggoGlobalRadius.Item),
        backgroundColor = Color.Transparent,
        borderColor = Color.Transparent,
        height = MediumHomeMetrics.TypeRowHeight,
        borderWidth = 0.dp,
        contentPadding = PaddingValues(horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(iconForTypeFilter(summary.filter)),
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(MediumHomeMetrics.TypeIconSize),
        )
            Text(
            text = typeFilterLabel(summary.filter, locale),
            modifier = Modifier.width(MediumHomeMetrics.TypeNameWidth),
            color = TaggoGlobalColors.TextPrimary.copy(
                alpha = MediumHomeMetrics.SupportLabelAlpha,
            ),
            fontSize = MediumHomeMetrics.TypeNameFontSize,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(MediumHomeMetrics.TypeBarHeight)
                .clip(RoundedCornerShape(TaggoGlobalRadius.Badge))
                .background(TaggoGlobalColors.TextMuted.copy(alpha = 0.22f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(summary.share.coerceIn(0.05f, 1f))
                    .fillMaxHeight()
                    .background(accent),
            )
        }
        Text(
            text = summary.count.toString(),
            modifier = Modifier.width(MediumHomeMetrics.TypeCountWidth),
            color = TaggoGlobalColors.TextMuted,
            fontSize = MediumHomeMetrics.CountFontSize,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            textAlign = TextAlign.End,
        )
    }
}

private object CompactHomeMetrics {
    val PageHorizontalPadding = 16.dp
    val PageTopPadding = 16.dp
    val SectionGap = 14.dp
    val BrandFontSize = 18.sp
    val TopBarHeight = 46.dp
    val BrandLogoSize = 22.dp
    val SearchIconSize = 17.dp
    val SearchHeight = 46.dp
    val AccountSize = 46.dp
    val AccountIconSize = 27.dp
    val PageTitleFontSize = 18.sp
    val PageTitleGap = 6.dp
    val TitleUnderlineWidth = 32.dp
    val TitleUnderlineHeight = 3.dp
    val CardPadding = TaggoGlobalSpacing.Sm
    val CardContentGap = TaggoGlobalSpacing.Xs
    val CardTitleFontSize = TaggoGlobalTypography.TitleMedium
    val RecentCardHeight = 184.dp
    val RecentEmptyCardHeight = 170.dp
    val MiddleCardGap = TaggoGlobalSpacing.Md
    val MiddleCardHeaderHeight = 48.dp
    val MiddleCardMinHeight = 132.dp
    val MiddleCardMaxHeight = 168.dp
    val RecentRowHeight = 42.dp
    val RecentIconSize = 32.dp
    val RecentTimeWidth = 56.dp
    val RecentListGap = 0.dp
    val TagChipHeight = 30.dp
    val TagGridTopPadding = 2.dp
    val TagGridBottomPadding = TaggoGlobalSpacing.Sm
    val TagGridGap = TaggoGlobalSpacing.Sm
    val TagItemHorizontalPadding = 10.dp
    val TagCountWidth = 20.dp
    val TypeRowHeight = 26.dp
    val TypeIconSize = 18.dp
    val TypeNameWidth = 30.dp
    val TypeProgressHeight = 5.dp
    val TypeCountWidth = 22.dp
    val RecommendationEmptyCardHeight = 160.dp
    val RecommendationRowHeight = 82.dp
    val RecommendationCoverSize = TaggoFileCoverTokens.CompactRecommendationCoverSize
    val RecommendationRowVerticalPadding = 14.dp
    val RecommendationBadgeIconSize = 12.dp
    val RecommendationTitleBadgeGap = 5.dp
    val RecommendationTagSummaryFontSize = 13.sp
    val RecommendationTagSummaryLineHeight = 16.sp
    val FabSize = 62.dp
    val FabIconSize = 30.dp
    val FabEndPadding = 24.dp
    val RecommendationFabClearance = FabSize + TaggoGlobalSpacing.Md
    val BottomNavigationHeight = 58.dp
    val BottomNavigationBottomMargin = 10.dp
    val FabNavigationGap = 18.dp
    val FabBottomPadding = BottomNavigationHeight + BottomNavigationBottomMargin + FabNavigationGap
    val BottomContentPadding =
        FabBottomPadding + FabSize + TaggoGlobalSpacing.Md
    const val TagItemBackgroundAlpha = 0.34f
    const val TagItemBorderAlpha = 0.42f
    const val TagLabelAlpha = 0.82f
    const val RecommendationItemBackgroundAlpha = 0.42f
    const val RecommendationItemBorderAlpha = 0.38f
    const val RecommendationBadgeIconAlpha = 0.50f
    const val RecommendationTagSummaryAlpha = 0.90f
}

@Composable
private fun CompactHomeTopBar(
    locale: AppLocale,
    searchDraft: String,
    searchPlaceholder: String,
    onSearch: () -> Unit,
    onOpenMenu: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(CompactHomeMetrics.TopBarHeight),
        horizontalArrangement = Arrangement.spacedBy(TaggoGlobalSpacing.Md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(TaggoGlobalSpacing.Sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(TaggoLogoBig2048),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(CompactHomeMetrics.BrandLogoSize),
            )
            Text(
                text = "Taggo",
                color = TaggoGlobalColors.TextPrimary,
                fontSize = CompactHomeMetrics.BrandFontSize,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        CompactSearchEntry(
            query = searchDraft,
            placeholder = searchPlaceholder,
            onClick = onSearch,
            modifier = Modifier.weight(1f),
        )

        CompactAccountButton(
            locale = locale,
            onClick = onOpenMenu,
        )
    }
}

@Composable
private fun CompactSearchEntry(
    query: String,
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val searchShape = RoundedCornerShape(TaggoGlobalRadius.Search)
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(CompactHomeMetrics.SearchHeight)
            .clip(searchShape),
        color = Color.Transparent,
        contentColor = TaggoCompactTokens.Search.Placeholder,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(TaggoCompactTokens.Search.BackgroundBrush, searchShape)
                .border(1.dp, TaggoCompactTokens.Search.Border, searchShape)
                .drawBehind {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                TaggoCompactTokens.Search.InnerHighlight,
                                Color.Transparent,
                            ),
                            center = Offset(size.width * 0.12f, size.height * 0.16f),
                            radius = size.maxDimension * 0.95f,
                        ),
                    )
                }
                .padding(horizontal = TaggoGlobalSpacing.Md),
            horizontalArrangement = Arrangement.spacedBy(TaggoGlobalSpacing.Sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = TaggoCompactTokens.Search.Icon,
                modifier = Modifier.size(CompactHomeMetrics.SearchIconSize),
            )
            Text(
                text = if (query.isBlank()) placeholder else query,
                color = if (query.isBlank()) TaggoCompactTokens.Search.Placeholder else TaggoGlobalColors.TextPrimary,
                fontSize = TaggoGlobalTypography.Body,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CompactAccountButton(
    locale: AppLocale,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(TaggoGlobalRadius.Badge),
        color = TaggoGlobalColors.PanelBackgroundSoft.copy(alpha = TaggoGlobalAlpha.Strong),
        contentColor = TaggoGlobalColors.TextPrimary,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier
            .size(CompactHomeMetrics.AccountSize),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, TaggoGlobalColors.BorderStrong, RoundedCornerShape(TaggoGlobalRadius.Badge)),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.AccountCircle,
                contentDescription = if (locale == AppLocale.ZhCn) "\u8d26\u53f7" else "Account",
                tint = TaggoGlobalColors.TextPrimary,
                modifier = Modifier.size(CompactHomeMetrics.AccountIconSize),
            )
        }
    }
}

@Composable
private fun CompactUploadFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(TaggoGlobalRadius.Badge),
        color = Color.Transparent,
        contentColor = TaggoCompactTokens.FabIcon,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = modifier
            .size(CompactHomeMetrics.FabSize)
            .drawBehind {
                drawCircle(
                    color = TaggoCompactTokens.FabGlow,
                    radius = size.minDimension * 0.58f,
                    center = center,
                )
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(TaggoGlobalRadius.Badge))
                .background(TaggoCompactTokens.fabGradient())
                .border(1.dp, TaggoCompactTokens.FabBorder, RoundedCornerShape(TaggoGlobalRadius.Badge)),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(0.62f)
                    .height(1.dp)
                    .background(TaggoCompactTokens.FabHighlight),
            )
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
                tint = TaggoCompactTokens.FabIcon,
                modifier = Modifier.size(CompactHomeMetrics.FabIconSize),
            )
        }
    }
}

@Composable
private fun HomeCompactDashboard(
    appState: FileManagerAppState,
    locale: AppLocale,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    scoredRecommendedReferences: List<ScoredRecommendation>,
    onOpenReference: (FileReference) -> Unit,
    onOpenFile: (FileReference) -> Unit,
    onOpenTags: () -> Unit,
    onOpenAllFiles: () -> Unit,
    onOpenTypeFilter: (AllFilesTypeFilter) -> Unit,
    onTagClick: (String) -> Unit,
) {
    val recentItems = appState.recentAddedReferences.take(3)
    val tagSummaries = remember(appState.snapshotVersion, appState.topTags) {
        resolveDashboardTagSummaries(appState.allReferences, appState.topTags)
    }
    val typeSummaries = remember(appState.snapshotVersion) {
        resolveDashboardTypeSummaries(appState.allReferences)
    }
    val entryTags = tagSummaries.take(6)
    val entryTypes = typeSummaries.take(5)
    val tagRowCount = ((entryTags.size + 1) / 2).coerceIn(1, 3)
    val typeRowCount = entryTypes.size.coerceAtLeast(1)
    val tagContentHeight = (
        tagRowCount * CompactHomeMetrics.TagChipHeight.value.toInt() +
            (tagRowCount - 1) * CompactHomeMetrics.TagGridGap.value.toInt()
        ).dp
    val typeContentHeight = (
        typeRowCount * CompactHomeMetrics.TypeRowHeight.value.toInt() +
            (typeRowCount - 1) * TaggoGlobalSpacing.Xs.value.toInt()
        ).dp
    val middleCardHeight = (
        CompactHomeMetrics.MiddleCardHeaderHeight + maxOf(tagContentHeight, typeContentHeight)
        ).coerceIn(
        CompactHomeMetrics.MiddleCardMinHeight,
        CompactHomeMetrics.MiddleCardMaxHeight,
    )

    Column(verticalArrangement = Arrangement.spacedBy(CompactHomeMetrics.SectionGap)) {
        Column(verticalArrangement = Arrangement.spacedBy(CompactHomeMetrics.PageTitleGap)) {
            Text(
                text = if (locale == AppLocale.ZhCn) "\u63a8\u8350" else "Recommended",
                color = TaggoGlobalColors.TextPrimary,
                fontSize = CompactHomeMetrics.PageTitleFontSize,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Box(
                modifier = Modifier
                    .width(CompactHomeMetrics.TitleUnderlineWidth)
                    .height(CompactHomeMetrics.TitleUnderlineHeight)
                    .clip(RoundedCornerShape(TaggoGlobalRadius.Badge))
                    .background(TaggoGlobalColors.PrimaryAccent),
            )
        }

        TaggoSectionCard(
            title = appState.recentlyAdded,
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    if (recentItems.isEmpty()) {
                        CompactHomeMetrics.RecentEmptyCardHeight
                    } else {
                        CompactHomeMetrics.RecentCardHeight
                    },
                ),
            compact = true,
            compactPadding = CompactHomeMetrics.CardPadding,
            compactContentGap = CompactHomeMetrics.CardContentGap,
            compactTitleFontSize = CompactHomeMetrics.CardTitleFontSize,
            trailing = {
                TaggoMoreButton(
                    label = if (locale == AppLocale.ZhCn) "\u67e5\u770b\u5168\u90e8 >" else "View all >",
                    onClick = onOpenAllFiles,
                    compact = true,
                )
            },
        ) {
            if (recentItems.isEmpty()) {
                TaggoEmptyState(
                    title = if (locale == AppLocale.ZhCn) "\u8fd8\u6ca1\u6709\u65b0\u6587\u4ef6" else "No recent files yet",
                    description = if (locale == AppLocale.ZhCn) "\u9009\u62e9\u4e00\u4e2a\u6587\u4ef6\u540e\uff0c\u5b83\u4f1a\u51fa\u73b0\u5728\u8fd9\u91cc\u3002" else "Pick a file and it will appear here.",
                    compact = true,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(CompactHomeMetrics.RecentListGap)) {
                    recentItems.forEach { reference ->
                        DashboardRecentFileRow(
                            reference = reference,
                            fullCjkFontReady = fullCjkFontReady,
                            fullCjkFontFamily = fullCjkFontFamily,
                            locale = locale,
                            onOpen = { onOpenReference(reference) },
                            onOpenFile = { onOpenFile(reference) },
                            onViewDetails = { onOpenReference(reference) },
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CompactHomeMetrics.MiddleCardGap),
            verticalAlignment = Alignment.Top,
        ) {
            TaggoSectionCard(
                title = if (locale == AppLocale.ZhCn) "\u5e38\u7528\u6807\u7b7e" else "Tags",
                modifier = Modifier
                    .weight(1f)
                    .height(middleCardHeight),
                compact = true,
                compactPadding = CompactHomeMetrics.CardPadding,
                compactContentGap = CompactHomeMetrics.CardContentGap,
                compactTitleFontSize = CompactHomeMetrics.CardTitleFontSize,
                trailing = {
                    TaggoMoreButton(
                        label = if (locale == AppLocale.ZhCn) "\u67e5\u770b\u5168\u90e8 >" else "View all >",
                        onClick = onOpenTags,
                        compact = true,
                    )
                },
            ) {
                if (entryTags.isEmpty()) {
                    TaggoEmptyState(
                        title = if (locale == AppLocale.ZhCn) "\u6682\u65e0\u6807\u7b7e" else "No tags yet",
                        description = if (locale == AppLocale.ZhCn) "\u6dfb\u52a0\u6807\u7b7e\u540e\u4f1a\u5728\u8fd9\u91cc\u663e\u793a\u3002" else "Tagged files appear here.",
                        compact = true,
                    )
                } else {
                    CompactTagGrid(
                        tags = entryTags,
                        fullCjkFontReady = fullCjkFontReady,
                        fullCjkFontFamily = fullCjkFontFamily,
                        onTagClick = onTagClick,
                    )
                }
            }

            TaggoSectionCard(
                title = if (locale == AppLocale.ZhCn) "\u6587\u4ef6\u7c7b\u578b" else "File types",
                modifier = Modifier
                    .weight(1f)
                    .height(middleCardHeight),
                compact = true,
                compactPadding = CompactHomeMetrics.CardPadding,
                compactContentGap = CompactHomeMetrics.CardContentGap,
                compactTitleFontSize = CompactHomeMetrics.CardTitleFontSize,
                trailing = {
                    TaggoMoreButton(
                        label = if (locale == AppLocale.ZhCn) "\u67e5\u770b\u5168\u90e8 >" else "View all >",
                        onClick = onOpenAllFiles,
                        compact = true,
                    )
                },
            ) {
                if (entryTypes.isEmpty()) {
                    TaggoEmptyState(
                        title = if (locale == AppLocale.ZhCn) "\u6682\u65e0\u6587\u4ef6\u7c7b\u578b" else "No file types yet",
                        description = if (locale == AppLocale.ZhCn) "\u5bfc\u5165\u6587\u4ef6\u540e\u4f1a\u663e\u793a\u7edf\u8ba1\u3002" else "Type bars appear after import.",
                        compact = true,
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(TaggoGlobalSpacing.Xs)) {
                        entryTypes.forEach { summary ->
                            CompactFileTypeRow(
                                summary = summary,
                                locale = locale,
                                onClick = { onOpenTypeFilter(summary.filter) },
                            )
                        }
                    }
                }
            }
        }

        TaggoSectionCard(
            title = if (locale == AppLocale.ZhCn) "\u667a\u80fd\u63a8\u8350" else "Recommended files",
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (scoredRecommendedReferences.isEmpty()) {
                        Modifier.height(CompactHomeMetrics.RecommendationEmptyCardHeight)
                    } else {
                        Modifier
                    },
                ),
            compact = true,
            compactPadding = CompactHomeMetrics.CardPadding,
            compactContentGap = CompactHomeMetrics.CardContentGap,
            compactTitleFontSize = CompactHomeMetrics.CardTitleFontSize,

        ) {
            if (scoredRecommendedReferences.isEmpty()) {
                TaggoEmptyState(
                    title = if (locale == AppLocale.ZhCn) "\u6682\u65e0\u63a8\u8350" else "No recommendations",
                    description = if (locale == AppLocale.ZhCn) "\u79ef\u7d2f\u6253\u5f00\u8bb0\u5f55\u540e\u4f1a\u751f\u6210\u3002" else "Open history will create signals.",
                    modifier = Modifier.padding(end = CompactHomeMetrics.RecommendationFabClearance),
                    compact = true,
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(TaggoGlobalSpacing.Sm),
                ) {
                    val visibleRecommendations = scoredRecommendedReferences.take(5)
                    visibleRecommendations.forEach { recommendation ->
                        DashboardCompactRecommendedFileRow(
                            reference = recommendation.file,
                            scoredRecommendation = recommendation,
                            fullCjkFontReady = fullCjkFontReady,
                            fullCjkFontFamily = fullCjkFontFamily,
                            locale = locale,
                            onOpenFile = { onOpenFile(recommendation.file) },
                            onViewDetails = { onOpenReference(recommendation.file) },
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(CompactHomeMetrics.BottomContentPadding))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CompactTagGrid(
    tags: List<DashboardTagSummary>,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    onTagClick: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = CompactHomeMetrics.TagGridTopPadding,
                bottom = CompactHomeMetrics.TagGridBottomPadding,
            ),
        verticalArrangement = Arrangement.spacedBy(CompactHomeMetrics.TagGridGap),
    ) {
        tags.chunked(2).forEach { rowTags ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CompactHomeMetrics.TagGridGap),
            ) {
                rowTags.forEach { summary ->
                    TaggoTagEntryItem(
                        label = summary.tag,
                        count = summary.count,
                        fullCjkFontReady = fullCjkFontReady,
                        fullCjkFontFamily = fullCjkFontFamily,
                        onClick = { onTagClick(summary.tag) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowTags.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CompactFileTypeRow(
    summary: DashboardTypeSummary,
    locale: AppLocale,
    onClick: () -> Unit,
) {
    val typeColors = colorTokensForTypeFilter(summary.filter)
    TaggoListItemSurface(
        shape = RoundedCornerShape(TaggoGlobalRadius.Item),
        backgroundColor = Color.Transparent,
        backgroundBrush = TaggoCompactTokens.glassListItemBackgroundBrush(),
        borderColor = TaggoCompactTokens.GlassListItemBorder,
        height = CompactHomeMetrics.TypeRowHeight,
        contentPadding = PaddingValues(0.dp),
        horizontalArrangement = Arrangement.spacedBy(TaggoGlobalSpacing.Sm),
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(iconForTypeFilter(summary.filter)),
            contentDescription = null,
            tint = typeColors.iconColor,
            modifier = Modifier.size(CompactHomeMetrics.TypeIconSize),
        )
            Text(
            text = typeFilterLabel(summary.filter, locale),
            modifier = Modifier.width(CompactHomeMetrics.TypeNameWidth),
            color = TaggoGlobalColors.TextPrimary,
            fontSize = TaggoGlobalTypography.BodySmall,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(CompactHomeMetrics.TypeProgressHeight)
                .clip(RoundedCornerShape(TaggoGlobalRadius.Badge))
                .background(TaggoGlobalColors.TextMuted.copy(alpha = TaggoGlobalAlpha.Subtle)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(summary.share.coerceIn(0.08f, 1f))
                    .fillMaxHeight()
                    .background(typeColors.progressColor),
            )
        }
        Text(
            text = summary.count.toString(),
            modifier = Modifier.width(CompactHomeMetrics.TypeCountWidth),
            color = TaggoGlobalColors.TextSecondary,
            fontSize = TaggoGlobalTypography.BodySmall,
            maxLines = 1,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun DashboardHeroHeader(
    locale: AppLocale,
    searchDraft: String,
    searchPlaceholder: String,
    textStyle: androidx.compose.ui.text.TextStyle,
    onSearchDraftChange: (String) -> Unit,
    onSearch: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = HomeWide.Size.HeroHeaderMinHeight)
            .padding(top = HomeWide.Spacing.HeroTop, bottom = HomeWide.Spacing.HeroBottom),
        horizontalArrangement = Arrangement.spacedBy(HomeWide.Spacing.HeroHorizontal),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(HomeWide.Spacing.HeroTitleGap),
        ) {
            Text(
                text = if (locale == AppLocale.ZhCn) "\u63a8\u8350" else "Recommended",
                color = HomeWide.Colors.TextPrimary,
                fontSize = HomeWide.Typography.HeroTitle,
                lineHeight = HomeWide.Typography.HeroTitleLineHeight,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (locale == AppLocale.ZhCn) {
                    "\u5feb\u901f\u8bbf\u95ee\u548c\u6700\u8fd1\u4f7f\u7528\u7684\u6587\u4ef6"
                } else {
                    "Quick access and recently used files"
                },
                color = HomeWide.Colors.TextSecondary,
                fontSize = HomeWide.Typography.HeroSubtitle,
                lineHeight = HomeWide.Typography.HeroSubtitleLineHeight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        HomeDashboardSearchBar(
            query = searchDraft,
            onQueryChange = onSearchDraftChange,
            onSearch = onSearch,
            placeholder = searchPlaceholder,
            buttonLabel = if (locale == AppLocale.ZhCn) "\u641c\u7d22" else "Search",
            textStyle = textStyle,
            modifier = Modifier.widthIn(min = HomeWide.Size.HeroSearchMinWidth, max = HomeWide.Size.HeroSearchMaxWidth),
        )

    }
}

@Composable
private fun HomeDashboardSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    placeholder: String,
    buttonLabel: String,
    textStyle: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }
    val searchShape = RoundedCornerShape(HomeWide.Radius.HeroSearch)
    val searchBorderColor = HomeWide.searchBorderColor(focused)
    val searchTextStyle = textStyle.copy(
        color = HomeWide.Colors.TextPrimary,
        fontSize = HomeWide.Typography.SearchText,
        lineHeight = HomeWide.Typography.SearchLineHeight,
    )
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(HomeWide.Spacing.SearchBarGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            modifier = Modifier
                .weight(1f)
                .height(HomeWide.Size.SearchBarHeight)
                .clip(searchShape)
                .background(HomeWide.searchFieldBackground())
                .border(1.dp, searchBorderColor, searchShape)
                .onFocusChanged { focused = it.isFocused },
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            textStyle = searchTextStyle,
            cursorBrush = SolidColor(HomeWide.Colors.PrimaryAccent),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = HomeWide.Spacing.SearchFieldPaddingX),
                    horizontalArrangement = Arrangement.spacedBy(HomeWide.Spacing.SearchFieldGap),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = HomeWide.Colors.TextSecondary,
                        modifier = Modifier.size(HomeWide.Size.SearchIcon),
                    )
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (query.isEmpty()) {
                            Text(
                                text = placeholder,
                                color = HomeWide.Colors.TextMuted,
                                fontSize = HomeWide.Typography.SearchPlaceholder,
                                lineHeight = HomeWide.Typography.SearchLineHeight,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        innerTextField()
                    }
                }
            }
        )
        DashboardToolButton(
            label = buttonLabel,
            onClick = onSearch,
            primary = false,
        )
    }
}

@Composable
private fun DashboardToolButton(
    label: String,
    onClick: () -> Unit,
    primary: Boolean,
    modifier: Modifier = Modifier,
) {
    val containerColor = HomeWide.toolButtonContainerColor(primary)
    val contentColor = if (primary) HomeWide.Colors.TextPrimary else HomeWide.Colors.TextSecondary
    Button(
        modifier = modifier.height(HomeWide.Size.ToolButtonHeight),
        shape = RoundedCornerShape(HomeWide.Radius.ToolButton),
        contentPadding = PaddingValues(horizontal = HomeWide.Spacing.ToolButtonPaddingX, vertical = 0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = HomeWide.Colors.PanelBackgroundSoft.copy(alpha = HomeWide.Alpha.ButtonDisabled),
            disabledContentColor = HomeWide.Colors.TextMuted,
        ),
        onClick = onClick,
    ) {
        Text(
            text = label,
            fontSize = HomeWide.Typography.ToolButton,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun dashboardSearchTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = HomeWide.searchFieldContainerColor(focused = true),
    unfocusedContainerColor = HomeWide.searchFieldContainerColor(focused = false),
    disabledContainerColor = HomeWide.Colors.SurfaceVariant,
    focusedTextColor = HomeWide.Colors.TextPrimary,
    unfocusedTextColor = HomeWide.Colors.TextPrimary,
    disabledTextColor = HomeWide.Colors.TextMuted,
    focusedLabelColor = HomeWide.Colors.TextPrimary,
    unfocusedLabelColor = HomeWide.Colors.TextSecondary,
    disabledLabelColor = HomeWide.Colors.TextMuted,
    focusedIndicatorColor = HomeWide.searchFieldIndicatorColor(focused = true),
    unfocusedIndicatorColor = HomeWide.searchFieldIndicatorColor(focused = false),
    disabledIndicatorColor = HomeWide.Colors.Border,
    focusedPlaceholderColor = HomeWide.Colors.TextMuted,
    unfocusedPlaceholderColor = HomeWide.Colors.TextMuted,
    focusedSupportingTextColor = HomeWide.Colors.TextSecondary,
    unfocusedSupportingTextColor = HomeWide.Colors.TextSecondary,
    disabledSupportingTextColor = HomeWide.Colors.TextMuted,
    cursorColor = HomeWide.Colors.PrimaryAccent,
)

@Composable
private fun DashboardRecentFileRow(
    reference: FileReference,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    locale: AppLocale = AppLocale.EnUs,
    onOpen: () -> Unit,
    onOpenFile: () -> Unit = onOpen,
    onViewDetails: () -> Unit = onOpen,
) {
    val iconStyle = fileTypeIconStyle(reference)
    val compactLayout = LocalTaggoWindowSizeClass.current == TaggoWindowSizeClass.Compact
    val addedTime = remember(reference.createdAtMillis, locale, compactLayout) {
        if (compactLayout) {
            compactRelativeTimeLabel(reference.createdAtMillis, locale)
        } else {
            formatRelativeTime(reference.createdAtMillis)
        }
    }
    if (compactLayout) {
        TaggoListItemSurface(
            shape = RoundedCornerShape(8.dp),
            backgroundColor = Color.Transparent,
            backgroundBrush = TaggoCompactTokens.glassListItemBackgroundBrush(),
            borderColor = TaggoCompactTokens.GlassListItemBorder,
            height = CompactHomeMetrics.RecentRowHeight,
            contentPadding = PaddingValues(
                horizontal = TaggoGlobalSpacing.Sm,
                vertical = 4.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(TaggoGlobalSpacing.Sm),
            onClick = onViewDetails,
        ) {
            FileCoverArtFrame(
                reference = reference,
                iconStyle = iconStyle,
                fullCjkFontReady = fullCjkFontReady,
                fullCjkFontFamily = fullCjkFontFamily,
                modifier = Modifier
                    .size(CompactHomeMetrics.RecentIconSize)
                    .clickable(onClick = onOpenFile),
                cornerShape = RoundedCornerShape(8.dp),
                iconSize = 18.dp,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(TaggoGlobalSpacing.Sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = displayTextForUi(reference.title, fullCjkFontReady),
                        color = TaggoGlobalColors.TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = fullCjkFontFamily,
                        modifier = Modifier.weight(1f),
                    )
                    CompactFileTypeBadge(
                        label = compactFileTypeLabel(reference),
                        reference = reference,
                    )
                }
            }
            Text(
                text = addedTime,
                modifier = Modifier.width(CompactHomeMetrics.RecentTimeWidth),
                color = TaggoGlobalColors.TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
            )
        }
    } else {
        val rowShape = RoundedCornerShape(HomeWide.Radius.RecentRow)
        val meta = remember(reference, fullCjkFontReady) {
            buildList {
                add(displayTextForUi(reference.fileType, fullCjkFontReady).ifBlank { "file" })
                add(formatFileSize(reference.fileSizeBytes ?: guessFileSizeFromNotes(reference.notes)))
            }.joinToString(" · ")
        }
        TaggoListItemSurface(
            shape = rowShape,
            backgroundColor = HomeWide.Colors.DashboardItemBackground,
            borderColor = HomeWide.Colors.DashboardItemBorder,
            height = HomeWide.Size.RecentRowHeight,
            contentPadding = PaddingValues(
                horizontal = HomeWide.Spacing.RecentRowPaddingX,
                vertical = HomeWide.Spacing.RecentRowPaddingY,
            ),
            horizontalArrangement = Arrangement.spacedBy(HomeWide.Spacing.RecentRowGap),
            onClick = onOpen,
        ) {
            FileCoverArtFrame(
                reference = reference,
                iconStyle = iconStyle,
                fullCjkFontReady = fullCjkFontReady,
                fullCjkFontFamily = fullCjkFontFamily,
                modifier = Modifier.size(HomeWide.Size.RecentIconSize),
                cornerShape = RoundedCornerShape(HomeWide.Radius.RecentIcon),
                iconSize = HomeWide.Size.RecentIconInner,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(HomeWide.Spacing.RecentTextGap),
            ) {
                Text(
                    text = displayTextForUi(reference.title, fullCjkFontReady),
                    color = HomeWide.Colors.TextPrimary,
                    fontSize = HomeWide.Typography.RecentTitle,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = fullCjkFontFamily,
                )
            Text(
                    text = meta,
                    color = HomeWide.Colors.TextSecondary.copy(alpha = HomeWide.Alpha.RecentMeta),
                    fontSize = HomeWide.Typography.RecentMeta,
                    lineHeight = HomeWide.Typography.RecentMetaLineHeight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = addedTime,
                modifier = Modifier.width(HomeWide.Size.RecentTimeWidth),
                color = HomeWide.Colors.TextSecondary.copy(alpha = HomeWide.Alpha.RecentMeta),
                fontSize = HomeWide.Typography.RecentTime,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
            )
        }
    }
}

private fun compactFileTypeLabel(reference: FileReference): String {
    val extension = reference.title
        .substringAfterLast('.', missingDelimiterValue = "")
        .trim()
        .takeIf { value -> value.length in 1..5 && value.all { it.isLetterOrDigit() } }
    if (extension != null) return extension.uppercase()

    return when (val type = reference.fileType.trim().uppercase()) {
        "APPLICATION", "EXECUTABLE" -> "EXE"
        "DOCUMENT", "WORD", "WORD DOCUMENT" -> "DOCX"
        "IMAGE" -> "IMG"
        "VIDEO" -> "MP4"
        "TEXT", "PLAIN TEXT" -> "TXT"
        else -> type.ifBlank { "FILE" }.take(5)
    }
}

@Composable
private fun CompactFileTypeBadge(
    label: String,
    reference: FileReference,
) {
    val typeColors = TaggoFileTypeColorTokens.forCategory(FileTypeClassifier.classify(reference))
    Surface(
        shape = RoundedCornerShape(TaggoGlobalRadius.Badge),
        color = typeColors.badgeBackground,
        contentColor = typeColors.badgeContentColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
        )
    }
}

private fun compactRelativeTimeLabel(
    millis: Long,
    locale: AppLocale,
): String {
    val relativeTime = formatRelativeTime(millis)
    if (locale != AppLocale.ZhCn) return relativeTime

    return when {
        relativeTime == "never" -> "从未"
        relativeTime == "just now" -> "刚刚"
        relativeTime.endsWith("m ago") -> "${relativeTime.removeSuffix("m ago")}分钟前"
        relativeTime.endsWith("h ago") -> "${relativeTime.removeSuffix("h ago")}小时前"
        relativeTime.endsWith("d ago") -> "${relativeTime.removeSuffix("d ago")}天前"
        else -> relativeTime
    }
}

private fun displayFormalTagForUi(
    tag: String,
    fullCjkFontReady: Boolean,
): String {
    val displayText = displayTextForUi(tag, fullCjkFontReady)
    return if (displayText.startsWith("#")) displayText else "#$displayText"
}

@Composable
private fun TaggoInlineTagSummary(
    tags: List<String>,
    locale: AppLocale,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    modifier: Modifier = Modifier,
    mode: TaggoInlineTagSummaryMode = TaggoInlineTagSummaryMode.File,
) {
    when (mode) {
        TaggoInlineTagSummaryMode.File -> {
            TaggoTagRow(
                tags = tags,
                fullCjkFontReady = fullCjkFontReady,
                fullCjkFontFamily = fullCjkFontFamily,
                modifier = modifier,
            )
        }

        is TaggoInlineTagSummaryMode.Recommendation -> {
            val textMeasurer = rememberTextMeasurer()
            val density = LocalDensity.current
            val style = mode.style
            BoxWithConstraints(modifier = modifier) {
                val availableWidthPx = with(density) { (maxWidth - 12.dp).coerceAtLeast(0.dp).toPx() }
                val summary = remember(tags, locale, fullCjkFontReady, availableWidthPx, style) {
                    measuredRecommendationTagSummary(
                        tags = tags,
                        locale = locale,
                        fullCjkFontReady = fullCjkFontReady,
                        availableWidthPx = availableWidthPx,
                        measure = { text -> textMeasurer.measure(text = text, style = style).size.width },
                    )
                }
                Text(
                    text = summary,
                    color = style.color,
                    fontSize = style.fontSize,
                    lineHeight = style.lineHeight,
                    fontWeight = style.fontWeight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = style.fontFamily ?: fullCjkFontFamily,
                )
            }
        }
    }
}

private sealed interface TaggoInlineTagSummaryMode {
    data object File : TaggoInlineTagSummaryMode

    data class Recommendation(
        val style: androidx.compose.ui.text.TextStyle,
    ) : TaggoInlineTagSummaryMode
}

private fun measuredRecommendationTagSummary(
    tags: List<String>,
    locale: AppLocale,
    fullCjkFontReady: Boolean,
    availableWidthPx: Float,
    measure: (String) -> Int,
): String {
    if (tags.isEmpty()) {
        return if (locale == AppLocale.ZhCn) "\u65e0\u6807\u7b7e" else "No tags"
    }

    val displayedTags = tags.map { displayFormalTagForUi(it, fullCjkFontReady) }
    for (visibleCount in displayedTags.size downTo 1) {
        val remainingCount = displayedTags.size - visibleCount
        val text = buildString {
            append(displayedTags.take(visibleCount).joinToString("  "))
            if (remainingCount > 0) {
                append("  +")
                append(remainingCount)
            }
        }
        if (measure(text) <= availableWidthPx) return text
    }

    return buildString {
        append(displayedTags.first())
        val remainingCount = displayedTags.size - 1
        if (remainingCount > 0) {
            append("  +")
            append(remainingCount)
        }
    }
}

@Composable
private fun DashboardTagGrid(
    tags: List<DashboardTagSummary>,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    onTagClick: (String) -> Unit,
) {
    val windowSizeClass = LocalTaggoWindowSizeClass.current
    val columns = if (windowSizeClass == TaggoWindowSizeClass.Compact) 3 else 2
    val rows = tags.chunked(columns)
    Column(
        verticalArrangement = if (windowSizeClass == TaggoWindowSizeClass.Compact) {
            Arrangement.spacedBy(HomeWide.Spacing.TagGridGap)
        } else if (rows.size <= 2) {
            Arrangement.SpaceEvenly
        } else {
            Arrangement.spacedBy(HomeWide.Spacing.TagGridGap)
        },
    ) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(HomeWide.Spacing.TagGridGap),
            ) {
                rowItems.forEach { summary ->
                    TaggoTagEntryItem(
                        label = summary.tag,
                        count = summary.count,
                        fullCjkFontReady = fullCjkFontReady,
                        fullCjkFontFamily = fullCjkFontFamily,
                        onClick = { onTagClick(summary.tag) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowItems.size < columns) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TaggoTagEntryItem(
    label: String,
    count: Int,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val windowSizeClass = LocalTaggoWindowSizeClass.current
    when (windowSizeClass) {
        TaggoWindowSizeClass.Compact -> {
            Box(modifier = modifier) {
                TaggoListItemSurface(
                    shape = RoundedCornerShape(TaggoGlobalRadius.Item),
                    backgroundColor = Color.Transparent,
                    backgroundBrush = TaggoCompactTokens.glassListItemBackgroundBrush(),
                    borderColor = TaggoCompactTokens.GlassListItemBorder,
                    height = CompactHomeMetrics.TagChipHeight,
                    contentPadding = PaddingValues(
                        horizontal = CompactHomeMetrics.TagItemHorizontalPadding,
                    ),
                    onClick = onClick,
                ) {
                    Text(
                        text = displayFormalTagForUi(label, fullCjkFontReady),
                        modifier = Modifier.weight(1f),
                        color = TaggoGlobalColors.TextPrimary.copy(
                            alpha = CompactHomeMetrics.TagLabelAlpha,
                        ),
                        fontSize = TaggoGlobalTypography.Button,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = fullCjkFontFamily,
                    )
            Text(
                        text = count.toString(),
                        modifier = Modifier.width(CompactHomeMetrics.TagCountWidth),
                        color = TaggoGlobalColors.TextMuted,
                        fontSize = TaggoGlobalTypography.Caption,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End,
                    )
                }
            }
        }

        TaggoWindowSizeClass.Medium -> {
            TaggoListItemSurface(
                modifier = modifier,
                shape = RoundedCornerShape(TaggoGlobalRadius.Item),
                backgroundColor = TaggoGlobalColors.ItemBackground.copy(
                    alpha = MediumHomeMetrics.TagItemBackgroundAlpha,
                ),
                borderColor = TaggoGlobalColors.Border.copy(alpha = MediumHomeMetrics.TagItemBorderAlpha),
                height = MediumHomeMetrics.TagItemHeight,
                contentPadding = PaddingValues(horizontal = MediumHomeMetrics.TagItemHorizontalPadding),
                onClick = onClick,
            ) {
                Text(
                    text = displayFormalTagForUi(label, fullCjkFontReady),
                    modifier = Modifier.weight(1f),
                    color = TaggoGlobalColors.TextPrimary.copy(
                        alpha = MediumHomeMetrics.SupportLabelAlpha,
                    ),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = fullCjkFontFamily,
                )
            Text(
                    text = count.toString(),
                    modifier = Modifier.width(MediumHomeMetrics.TagCountWidth),
                    color = TaggoGlobalColors.TextMuted,
                    fontSize = MediumHomeMetrics.CountFontSize,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                )
            }
        }

        TaggoWindowSizeClass.Expanded -> {
            val tagShape = RoundedCornerShape(HomeWide.Radius.TagButton)
            TaggoListItemSurface(
                modifier = modifier,
                shape = tagShape,
                backgroundColor = HomeWide.Colors.DashboardItemBackground,
                borderColor = HomeWide.Colors.DashboardItemBorder,
                height = HomeWide.Size.TagButtonHeight,
                contentPadding = PaddingValues(
                    horizontal = HomeWide.Spacing.TagButtonPaddingX,
                    vertical = HomeWide.Spacing.TagButtonPaddingY,
                ),
                horizontalArrangement = Arrangement.spacedBy(HomeWide.Spacing.TagGridGap),
                onClick = onClick,
            ) {
                Text(
                    text = displayFormalTagForUi(label, fullCjkFontReady),
                    modifier = Modifier.weight(1f),
                    color = HomeWide.Colors.TextPrimary,
                    fontSize = HomeWide.Typography.TagButton,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = fullCjkFontFamily,
                )
                DashboardCountBadge(label = count.toString())
            }
        }
    }
}

@Composable
private fun DashboardTypeRow(
    summary: DashboardTypeSummary,
    locale: AppLocale,
    onClick: () -> Unit,
) {
    val icon = iconForTypeFilter(summary.filter)
    val accent = accentForTypeFilter(summary.filter)
    val rowShape = RoundedCornerShape(HomeWide.Radius.TypeRow)
    TaggoListItemSurface(
        shape = rowShape,
        backgroundColor = HomeWide.Colors.DashboardItemBackground,
        borderColor = HomeWide.Colors.DashboardItemBorder,
        height = HomeWide.Size.RecentRowHeight,
        contentPadding = PaddingValues(
            horizontal = HomeWide.Spacing.TypeRowPaddingX,
            vertical = HomeWide.Spacing.TypeRowPaddingY,
        ),
        horizontalArrangement = Arrangement.spacedBy(HomeWide.Spacing.TypeRowGap),
        onClick = onClick,
    ) {
        Box(
            modifier = Modifier
                .size(HomeWide.Size.TypeIconSize)
                .clip(RoundedCornerShape(HomeWide.Radius.TypeIcon))
                .background(HomeWide.dashboardTypeAccentBackground(accent)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(HomeWide.Size.TypeIconInner),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(HomeWide.Spacing.TypeContentGap),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = typeFilterLabel(summary.filter, locale),
                    modifier = Modifier.weight(1f),
                    color = HomeWide.Colors.TextPrimary,
                    fontSize = HomeWide.Typography.TypeName,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            Text(
                    text = summary.count.toString(),
                    color = HomeWide.Colors.TextSecondary,
                    fontSize = HomeWide.Typography.TypeCount,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HomeWide.Spacing.TypeBarHeight)
                    .clip(RoundedCornerShape(100.dp))
                    .background(HomeWide.Colors.DashboardProgressTrack),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(summary.share.coerceIn(0.05f, 1f))
                        .fillMaxHeight()
                        .background(accent),
                )
            }
        }
    }
}

@Composable
private fun DashboardCompactRecommendedFileRow(
    reference: FileReference,
    scoredRecommendation: ScoredRecommendation,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    locale: AppLocale,
    onOpenFile: () -> Unit,
    onViewDetails: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconStyle = fileTypeIconStyle(reference)
    TaggoListItemSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(TaggoGlobalRadius.Item),
        backgroundColor = Color.Transparent,
        backgroundBrush = TaggoCompactTokens.glassListItemBackgroundBrush(),
        borderColor = TaggoCompactTokens.GlassListItemBorder,
        height = CompactHomeMetrics.RecommendationRowHeight,
        contentPadding = PaddingValues(
            horizontal = CompactHomeMetrics.TagItemHorizontalPadding,
            vertical = CompactHomeMetrics.RecommendationRowVerticalPadding,
        ),
        horizontalArrangement = Arrangement.spacedBy(TaggoGlobalSpacing.Sm),
        verticalAlignment = Alignment.CenterVertically,
        onClick = onViewDetails,
    ) {
        FileCoverArtFrame(
            reference = reference,
            iconStyle = iconStyle,
            fullCjkFontReady = fullCjkFontReady,
            fullCjkFontFamily = fullCjkFontFamily,
            modifier = Modifier
                .size(CompactHomeMetrics.RecommendationCoverSize)
                .clickable(onClick = onOpenFile),
            cornerShape = RoundedCornerShape(10.dp),
            iconSize = TaggoFileCoverTokens.CompactRecommendationIconSize,
            overlayContainerSize = 18.dp,
            overlayIconSize = 12.dp,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .height(CompactHomeMetrics.RecommendationCoverSize),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = displayTextForUi(reference.title, fullCjkFontReady),
                    modifier = Modifier.weight(1f),
                    color = TaggoGlobalColors.TextPrimary,
                    fontSize = TaggoGlobalTypography.Button,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = fullCjkFontFamily,
                )
                Spacer(modifier = Modifier.width(CompactHomeMetrics.RecommendationTitleBadgeGap))
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = TaggoGlobalColors.PrimaryAccent.copy(
                        alpha = CompactHomeMetrics.RecommendationBadgeIconAlpha,
                    ),
                    modifier = Modifier.size(CompactHomeMetrics.RecommendationBadgeIconSize),
                )
            }
            TaggoInlineTagSummary(
                tags = reference.tags,
                fullCjkFontReady = fullCjkFontReady,
                fullCjkFontFamily = fullCjkFontFamily,
                locale = locale,
                modifier = Modifier.fillMaxWidth(),
                mode = TaggoInlineTagSummaryMode.Recommendation(
                    style = androidx.compose.ui.text.TextStyle(
                        color = TaggoGlobalColors.TextSecondary.copy(
                            alpha = CompactHomeMetrics.RecommendationTagSummaryAlpha,
                        ),
                        fontSize = CompactHomeMetrics.RecommendationTagSummaryFontSize,
                        lineHeight = CompactHomeMetrics.RecommendationTagSummaryLineHeight,
                        fontWeight = FontWeight.Normal,
                        fontFamily = fullCjkFontFamily,
                    ),
                ),
            )
        }
    }
}

private fun compactRecommendationReason(
    scoredRecommendation: ScoredRecommendation,
    locale: AppLocale,
): String {
    val interval = scoredRecommendation.intervalScore
    val transition = scoredRecommendation.transitionScore
    val recency = scoredRecommendation.recencyScore
    val strongest = maxOf(interval, transition, recency)
    return when {
        strongest == transition && transition >= 0.45 -> if (locale == AppLocale.ZhCn) {
            "常在相邻文件之后接着打开"
        } else {
            "Often follows the previous file"
        }
        strongest == interval && interval >= 0.45 -> if (locale == AppLocale.ZhCn) {
            "与你的打开节奏较匹配"
        } else {
            "Matches your open rhythm"
        }
        else -> if (locale == AppLocale.ZhCn) {
            "最近使用频率较高"
        } else {
            "Recently used more often"
        }
    }
}

@Composable
private fun DashboardCountBadge(label: String) {
    Text(
        text = label,
        color = HomeWide.Colors.DashboardBadgeText,
        fontSize = HomeWide.Typography.Badge,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(HomeWide.Radius.Badge))
            .background(HomeWide.Colors.DashboardBadgeBackground)
            .padding(horizontal = HomeWide.Spacing.BadgePaddingX, vertical = HomeWide.Spacing.BadgePaddingY),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

private data class DashboardTagSummary(
    val tag: String,
    val count: Int,
)

private data class DashboardTypeSummary(
    val filter: AllFilesTypeFilter,
    val count: Int,
    val share: Float,
)

private fun resolveDashboardTagSummaries(
    references: List<FileReference>,
    topTags: List<String>,
): List<DashboardTagSummary> =
    topTags
        .map { tag ->
            DashboardTagSummary(
                tag = tag,
                count = references.count { reference -> reference.tags.any { it.trim() == tag.trim() } },
            )
        }
        .filter { it.tag.isNotBlank() && it.count > 0 }
        .take(8)

private fun resolveDashboardTypeSummaries(references: List<FileReference>): List<DashboardTypeSummary> {
    if (references.isEmpty()) return emptyList()
    val total = references.size.toFloat().coerceAtLeast(1f)
    return references
        .groupingBy(::resolveTypeFilter)
        .eachCount()
        .filterKeys { it != AllFilesTypeFilter.All }
        .entries
        .sortedByDescending { it.value }
        .take(5)
        .map { entry ->
            DashboardTypeSummary(
                filter = entry.key,
                count = entry.value,
                share = entry.value / total,
            )
        }
}

private fun iconForTypeFilter(filter: AllFilesTypeFilter): DrawableResource =
    when (filter) {
        AllFilesTypeFilter.Image -> FileTypeVisuals.iconDrawableForCategory(FileTypeCategory.Image)
        AllFilesTypeFilter.Video -> FileTypeVisuals.iconDrawableForCategory(FileTypeCategory.Video)
        AllFilesTypeFilter.Audio -> FileTypeVisuals.iconDrawableForCategory(FileTypeCategory.Audio)
        AllFilesTypeFilter.Document -> FileTypeVisuals.iconDrawableForCategory(FileTypeCategory.TextDocument)
        AllFilesTypeFilter.Spreadsheet -> FileTypeVisuals.iconDrawableForCategory(FileTypeCategory.Spreadsheet)
        AllFilesTypeFilter.Presentation -> FileTypeVisuals.iconDrawableForCategory(FileTypeCategory.Presentation)
        AllFilesTypeFilter.Other,
        AllFilesTypeFilter.All,
        -> FileTypeVisuals.iconDrawableForCategory(FileTypeCategory.Unknown)
    }

private fun colorTokensForTypeFilter(filter: AllFilesTypeFilter) =
    when (filter) {
        AllFilesTypeFilter.Image -> TaggoFileTypeColorTokens.Image
        AllFilesTypeFilter.Video -> TaggoFileTypeColorTokens.Video
        AllFilesTypeFilter.Audio -> TaggoFileTypeColorTokens.Audio
        AllFilesTypeFilter.Document,
        AllFilesTypeFilter.Spreadsheet,
        AllFilesTypeFilter.Presentation,
        -> TaggoFileTypeColorTokens.Document
        AllFilesTypeFilter.Other,
        AllFilesTypeFilter.All,
        -> TaggoFileTypeColorTokens.Other
    }

@Composable
private fun accentForTypeFilter(filter: AllFilesTypeFilter): Color =
    colorTokensForTypeFilter(filter).progressColor

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
    val compactBottomPadding = if (compactLayout) TaggoCompactTokens.BottomNavigationClearance else 0.dp
    val allTags = appState.allTags
    var pendingDeleteTag by remember { mutableStateOf<String?>(null) }
            Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = expandedPageContentModifier().padding(
                start = if (compactLayout) TaggoCompactTokens.PageHorizontalInsetExtra else 0.dp,
                end = if (compactLayout) TaggoCompactTokens.PageHorizontalInsetExtra else 0.dp,
                bottom = compactBottomPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(
                if (compactLayout) TaggoCompactTokens.PageSectionGap else 14.dp,
            ),
        ) {
            TopBarCard(
                locale = locale,
                title = if (locale == AppLocale.ZhCn) "\u6807\u7b7e" else "Tags",
                compactGlass = compactLayout,
            )

            AllTagsEntrySection(
                allTags = allTags,
                locale = locale,
                fullCjkFontReady = fullCjkFontReady,
                fullCjkFontFamily = fullCjkFontFamily,
                compactLayout = compactLayout,
                onTagClick = onTagClick,
                onRemoveTag = { pendingDeleteTag = it },
            )
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
                Column(verticalArrangement = Arrangement.spacedBy(TaggoGlobalSpacing.Sm)) {
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
                        fontSize = TaggoGlobalTypography.BodySmall,
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
    typeFilter: AllFilesTypeFilter,
    onSortModeChange: (FileSortMode) -> Unit,
    onTypeFilterChange: (AllFilesTypeFilter) -> Unit,
    onOpenReference: (FileReference) -> Unit,
) {
    val windowSizeClass = LocalTaggoWindowSizeClass.current
    val compactLayout = windowSizeClass == TaggoWindowSizeClass.Compact
    val compactBottomPadding = if (compactLayout) TaggoCompactTokens.BottomNavigationClearance else 0.dp
    val availableTypeFilters = remember(appState.snapshotVersion) {
        resolveAvailableTypeFilters(appState.allReferences)
    }
    val filteredFiles = remember(appState.snapshotVersion, typeFilter) {
        filterReferencesByType(appState.allReferences, typeFilter)
    }
    val sortedFiles = remember(filteredFiles, sortMode) {
        sortReferences(filteredFiles, sortMode, defaultSortDirection(sortMode))
    }
            Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = expandedPageContentModifier().padding(
                start = if (compactLayout) TaggoCompactTokens.PageHorizontalInsetExtra else 0.dp,
                end = if (compactLayout) TaggoCompactTokens.PageHorizontalInsetExtra else 0.dp,
                bottom = compactBottomPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(
                if (compactLayout) TaggoCompactTokens.PageSectionGap else 14.dp,
            ),
        ) {
            TopBarCard(
                locale = locale,
                title = if (locale == AppLocale.ZhCn) "\u6587\u4ef6" else "Files",
                compactGlass = compactLayout,
            )

            if (appState.allReferences.isEmpty()) {
                EmptyPanel(
                    title = if (locale == AppLocale.ZhCn) "\u6682\u65e0\u6587\u4ef6" else "No files yet",
                    body = if (locale == AppLocale.ZhCn) {
                        "\u5f53\u524d\u6587\u4ef6\u5217\u8868\u4e3a\u7a7a\u3002"
                    } else {
                        "The file list is currently empty."
                    },
                    compactGlass = compactLayout,
                )
            } else {
                AllFilesControlsRow(
                    sortMode = sortMode,
                    typeFilter = typeFilter,
                    availableTypeFilters = availableTypeFilters,
                    locale = locale,
                    compactLayout = compactLayout,
                    compactVisual = compactLayout,
                    onSortModeChange = onSortModeChange,
                    onTypeFilterChange = onTypeFilterChange,
                )

                if (sortedFiles.isEmpty()) {
                    EmptyPanel(
                        title = if (locale == AppLocale.ZhCn) "\u6682\u65e0\u8be5\u7c7b\u578b\u6587\u4ef6" else "No files in this type",
                        body = if (locale == AppLocale.ZhCn) {
                            "\u5207\u6362\u5230\u5176\u4ed6\u7c7b\u578b\uff0c\u6216\u8005\u4fdd\u6301\u201c\u5168\u90e8\u201d\u67e5\u770b\u6240\u6709\u6587\u4ef6\u3002"
                        } else {
                            "Switch to another type, or keep All selected to browse every file."
                        },
                        compactGlass = compactLayout,
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
private fun AllFilesControlsRow(
    sortMode: FileSortMode,
    typeFilter: AllFilesTypeFilter,
    availableTypeFilters: List<AllFilesTypeFilter>,
    locale: AppLocale,
    compactLayout: Boolean,
    compactVisual: Boolean,
    onSortModeChange: (FileSortMode) -> Unit,
    onTypeFilterChange: (AllFilesTypeFilter) -> Unit,
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var sortAnchorWidth by remember { mutableStateOf<Dp?>(null) }
    var typeAnchorWidth by remember { mutableStateOf<Dp?>(null) }
    val buttonHeight = if (compactVisual) 50.dp else if (compactLayout) TaggoCompactTokens.SearchButtonHeight else 42.dp
    val buttonRadius = if (compactVisual) 18.dp else if (compactLayout) TaggoCompactTokens.ButtonRadius else 12.dp
    val buttonHorizontalPadding = if (compactVisual) {
        16.dp
    } else if (compactLayout) {
        TaggoCompactTokens.FileItemHorizontalPadding
    } else {
        12.dp
    }
    val labelFontSize = if (compactVisual) 15.sp else if (compactLayout) TaggoCompactTokens.Caption else 12.sp

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            if (compactLayout) TaggoCompactTokens.FileItemGap else 10.dp,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f)) {
            AllFilesDropdownButton(
                label = if (locale == AppLocale.ZhCn) {
                    "\u6392\u5e8f\uff1a${sortModeLabel(sortMode, locale)}"
                } else {
                    "Sort: ${sortModeLabel(sortMode, locale)}"
                },
                height = buttonHeight,
                cornerRadius = buttonRadius,
                horizontalPadding = buttonHorizontalPadding,
                fontSize = labelFontSize,
                compactVisual = compactVisual,
                onAnchorWidthChanged = { sortAnchorWidth = it },
                onClick = { sortMenuExpanded = true },
            )
            val sortMenuModifier = if (sortAnchorWidth != null) {
                Modifier.width(sortAnchorWidth!!)
            } else {
                Modifier
            }
            DropdownMenu(
                expanded = sortMenuExpanded,
                onDismissRequest = { sortMenuExpanded = false },
                modifier = sortMenuModifier,
                shape = RoundedCornerShape(buttonRadius),
                containerColor = if (compactVisual) {
                    Color(0xE80E1422)
                } else {
                    TaggoTheme.colors.surface
                },
                tonalElevation = 0.dp,
                shadowElevation = if (compactVisual) 4.dp else 2.dp,
                border = if (compactVisual) {
                    BorderStroke(1.dp, Color(0x2AA9B8FF))
                } else {
                    null
                },
            ) {
                FileSortMode.entries.forEach { mode ->
                    DropdownMenuItem(
                        modifier = Modifier.height(50.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Box(
                                    modifier = Modifier.width(26.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (mode == sortMode) {
                                        Icon(
                                            imageVector = Icons.Outlined.Check,
                                            contentDescription = null,
                                            tint = Color(0xB89C6BFF),
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                                Text(
                                    text = sortModeLabel(mode, locale),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (mode == sortMode) Color(0xE8EEF2FF) else Color(0xD0EEF2FF),
                                )
                            }
                        },
                        onClick = {
                            onSortModeChange(mode)
                            sortMenuExpanded = false
                        },
                    )
                }
            }
        }
            Box(modifier = Modifier.weight(1f)) {
            AllFilesDropdownButton(
                label = if (locale == AppLocale.ZhCn) {
                    "\u7c7b\u578b\uff1a${typeFilterLabel(typeFilter, locale)}"
                } else {
                    "Type: ${typeFilterLabel(typeFilter, locale)}"
                },
                height = buttonHeight,
                cornerRadius = buttonRadius,
                horizontalPadding = buttonHorizontalPadding,
                fontSize = labelFontSize,
                compactVisual = compactVisual,
                onAnchorWidthChanged = { typeAnchorWidth = it },
                onClick = { typeMenuExpanded = true },
            )
            val typeMenuModifier = if (typeAnchorWidth != null) {
                Modifier.width(typeAnchorWidth!!)
            } else {
                Modifier
            }
            DropdownMenu(
                expanded = typeMenuExpanded,
                onDismissRequest = { typeMenuExpanded = false },
                modifier = typeMenuModifier,
                shape = RoundedCornerShape(buttonRadius),
                containerColor = if (compactVisual) {
                    Color(0xE80E1422)
                } else {
                    TaggoTheme.colors.surface
                },
                tonalElevation = 0.dp,
                shadowElevation = if (compactVisual) 4.dp else 2.dp,
                border = if (compactVisual) {
                    BorderStroke(1.dp, Color(0x2AA9B8FF))
                } else {
                    null
                },
            ) {
                availableTypeFilters.forEach { filter ->
                    DropdownMenuItem(
                        modifier = Modifier.height(50.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Box(
                                    modifier = Modifier.width(26.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (filter == typeFilter) {
                                        Icon(
                                            imageVector = Icons.Outlined.Check,
                                            contentDescription = null,
                                            tint = Color(0xB89C6BFF),
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                                Text(
                                    text = typeFilterLabel(filter, locale),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (filter == typeFilter) Color(0xE8EEF2FF) else Color(0xD0EEF2FF),
                                )
                            }
                        },
                        onClick = {
                            onTypeFilterChange(filter)
                            typeMenuExpanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun AllFilesDropdownButton(
    label: String,
    height: Dp,
    cornerRadius: Dp,
    horizontalPadding: Dp,
    fontSize: androidx.compose.ui.unit.TextUnit,
    compactVisual: Boolean,
    onAnchorWidthChanged: (Dp) -> Unit,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    if (compactVisual) {
        val density = LocalDensity.current
        var anchorWidth by remember { mutableStateOf<Dp?>(null) }
        val sizeModifier = if (anchorWidth != null) {
            Modifier.width(anchorWidth!!)
        } else {
            Modifier.fillMaxWidth()
        }
        Surface(
            onClick = onClick,
            modifier = Modifier
                .onGloballyPositioned {
                    val measuredWidth = with(density) { it.size.width.toDp() }
                    if (anchorWidth != measuredWidth) {
                        anchorWidth = measuredWidth
                        onAnchorWidthChanged(measuredWidth)
                    }
                }
                .then(sizeModifier)
                .height(height),
            shape = shape,
            color = Color.Transparent,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x16161B31), shape)
                    .border(1.dp, Color(0x24A9B8FF), shape)
                    .padding(horizontal = horizontalPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    fontSize = fontSize,
                    color = Color(0xE0EEF2FF),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Icon(
                    imageVector = Icons.Outlined.ArrowDropDown,
                    contentDescription = null,
                    tint = Color(0xB8BFC7D8),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
            shape = shape,
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            border = BorderStroke(TaggoCompactTokens.BorderWidth, TaggoTheme.colors.panelBorder),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = TaggoTheme.colors.panelBackgroundSoft,
                contentColor = TaggoTheme.colors.textPrimary,
            ),
        ) {
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                fontSize = fontSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Icon(
                imageVector = Icons.Outlined.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
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
    onRecentSearchClick: (String) -> Unit,
) {
    val windowSizeClass = LocalTaggoWindowSizeClass.current
    val compactLayout = windowSizeClass == TaggoWindowSizeClass.Compact
    val compactBottomPadding = if (compactLayout) TaggoCompactTokens.BottomNavigationClearance else 0.dp
    val activeSearchTags = appState.searchTags.toList()
    val draftHasInput = searchDraft.trim().isNotEmpty()
    val hasSubmittedSearch = activeSearchTags.isNotEmpty()
    val showSearchPrompt = !hasSubmittedSearch && !draftHasInput
    val showPendingDraftState = draftHasInput
    val showStaleResultsHint = hasSubmittedSearch && draftHasInput
    val searchResults = if (hasSubmittedSearch) appState.searchResults else emptyList()
    val recentSearches = appState.recentSearches.take(8)
    val resultStatus = if (showStaleResultsHint) {
        if (locale == AppLocale.ZhCn) {
            "已保留上一次结果，点击搜索可更新"
        } else {
            "Showing the previous results. Tap search to update them."
        }
    } else if (hasSubmittedSearch) {
        val summary = activeSearchTags.joinToString(separator = " ") { "[${displayTextForUi(it.value, fullCjkFontReady)}]" }
        if (locale == AppLocale.ZhCn) {
            "$summary · ${searchResults.size} 个结果"
        } else {
            "$summary · ${searchResults.size} results"
        }
    } else if (draftHasInput) {
        if (locale == AppLocale.ZhCn) {
            "输入完成后点击搜索"
        } else {
            "Finish typing, then tap search."
        }
    } else {
        if (locale == AppLocale.ZhCn) {
            "输入关键词开始搜索"
        } else {
            "Type keywords to start searching"
        }
    }
            Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = expandedPageContentModifier().padding(
                start = if (compactLayout) TaggoCompactTokens.PageHorizontalInsetExtra else 0.dp,
                end = if (compactLayout) TaggoCompactTokens.PageHorizontalInsetExtra else 0.dp,
                bottom = compactBottomPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(
                if (compactLayout) TaggoCompactTokens.PageSectionGap else 14.dp,
            ),
        ) {
            if (compactLayout) {
                SearchPageTopBar(
                    locale = locale,
                    title = appState.searchTitle,
                    onBack = onBackHome,
                )
            } else {
                TopBarCard(
                    locale = locale,
                    title = appState.searchTitle,
                    onBack = onBackHome,
                )
            }

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

            if (showSearchPrompt && recentSearches.isNotEmpty()) {
                RecentSearchesSection(
                    searches = recentSearches,
                    locale = locale,
                    fullCjkFontReady = fullCjkFontReady,
                    onRecentSearchClick = onRecentSearchClick,
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

            if (showPendingDraftState && !hasSubmittedSearch) {
                SearchPageEmptyState(
                    compact = compactLayout,
                    title = if (locale == AppLocale.ZhCn) "输入完成后点击搜索" else "Ready to search",
                    body = if (locale == AppLocale.ZhCn) {
                        "当前只是搜索草稿，还没有开始查找文件。"
                    } else {
                        "This is only a draft query so far. Tap search to start looking for files."
                    },
                )
            } else if (showSearchPrompt && recentSearches.isEmpty()) {
                SearchPageEmptyState(
                    compact = compactLayout,
                    title = if (locale == AppLocale.ZhCn) "还没有搜索标签" else "No search tags yet",
                    body = if (locale == AppLocale.ZhCn) {
                        "输入关键词开始搜索。"
                    } else {
                        "Type keywords to start searching."
                    },
                )
            } else if (showStaleResultsHint && searchResults.isEmpty()) {
                SearchPageEmptyState(
                    compact = compactLayout,
                    title = if (locale == AppLocale.ZhCn) "点击搜索更新结果" else "Tap search to update",
                    body = if (locale == AppLocale.ZhCn) {
                        "你已经修改了搜索草稿，当前没有把新关键词提交执行。"
                    } else {
                        "You changed the draft query, but the new keywords have not been submitted yet."
                    },
                )
            } else if (searchResults.isEmpty()) {
                SearchPageEmptyState(
                    compact = compactLayout,
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

@Composable
private fun SearchPageEmptyState(
    compact: Boolean,
    title: String,
    body: String,
) {
    if (compact) {
        SearchPageCompactEmptyState(
            title = title,
            body = body,
        )
    } else {
        SearchEmptyState(
            title = title,
            body = body,
        )
    }
}

@Composable
private fun SearchPageCompactEmptyState(
    title: String,
    body: String,
) {
    TaggoSectionCard(
        title = title,
        compact = true,
        compactPadding = TaggoCompactTokens.SearchCardPadding,
        compactContentGap = TaggoGlobalSpacing.Sm,
        trailing = {},
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(TaggoGlobalSpacing.Sm),
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(TaggoCompactTokens.glassListItemBackgroundBrush())
                    .border(1.dp, TaggoCompactTokens.GlassListItemBorder, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = TaggoCompactTokens.Search.Icon,
                    modifier = Modifier.size(22.dp),
                )
            }
            Text(
                text = body,
                color = TaggoGlobalColors.TextSecondary.copy(alpha = 0.88f),
                fontSize = TaggoGlobalTypography.Caption,
                lineHeight = 14.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SearchPageTopBar(
    locale: AppLocale,
    title: String,
    onBack: () -> Unit,
) {
    val shape = RoundedCornerShape(TaggoCompactTokens.CardRadius)
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .background(TaggoCompactTokens.glassCardBackgroundBrush(), shape)
            .border(1.dp, TaggoCompactTokens.GlassCardBorder, shape),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = TaggoGlobalSpacing.Sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TaggoIconActionButton(
                icon = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = if (locale == AppLocale.ZhCn) "返回" else "Back",
                onClick = onBack,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecentSearchesSection(
    searches: List<String>,
    locale: AppLocale,
    fullCjkFontReady: Boolean,
    onRecentSearchClick: (String) -> Unit,
) {
    val windowSizeClass = LocalTaggoWindowSizeClass.current
    val compactLayout = windowSizeClass == TaggoWindowSizeClass.Compact
    val historyMetrics = searchHistoryChipMetrics(windowSizeClass)
    if (compactLayout) {
        TaggoSectionCard(
            title = if (locale == AppLocale.ZhCn) "最近搜索" else "Recent searches",
            meta = if (locale == AppLocale.ZhCn) "点击后恢复该搜索" else "Tap to restore a search",
            compact = true,
            compactPadding = TaggoCompactTokens.FileItemHorizontalPadding,
            compactContentGap = TaggoCompactTokens.FileItemGap,
            trailing = {},
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(historyMetrics.horizontalSpacing),
                verticalArrangement = Arrangement.spacedBy(historyMetrics.verticalSpacing),
            ) {
                searches.forEach { query ->
                    TaggoSearchHistoryChip(
                        label = displayTextForUi(query, fullCjkFontReady),
                        onClick = { onRecentSearchClick(query) },
                    )
                }
            }
        }
        return
    }

    SectionCard(
        title = if (locale == AppLocale.ZhCn) "最近搜索" else "Recent searches",
        subtitle = if (locale == AppLocale.ZhCn) "点击后恢复该搜索" else "Tap to restore a search",
        responsiveHeader = true,
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(historyMetrics.horizontalSpacing),
            verticalArrangement = Arrangement.spacedBy(historyMetrics.verticalSpacing),
        ) {
            searches.forEach { query ->
                TaggoSearchHistoryChip(
                    label = displayTextForUi(query, fullCjkFontReady),
                    onClick = { onRecentSearchClick(query) },
                )
            }
        }
    }
}

@Composable
private fun TaggoSearchHistoryChip(
    label: String,
    onClick: () -> Unit,
) {
    val metrics = searchHistoryChipMetrics(LocalTaggoWindowSizeClass.current)
    val shape = RoundedCornerShape(metrics.cornerRadius)
    Surface(
        modifier = Modifier
            .wrapContentWidth()
            .height(metrics.height)
            .widthIn(max = metrics.maxWidth)
            .clip(shape)
            .clickable(onClick = onClick)
            .background(TaggoCompactTokens.glassListItemBackgroundBrush(), shape)
            .border(
                width = 1.dp,
                color = TaggoCompactTokens.GlassListItemBorder,
                shape = shape,
            ),
        shape = shape,
        color = Color.Transparent,
        contentColor = TaggoGlobalColors.TextSecondary.copy(alpha = 0.88f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .height(metrics.height)
                .wrapContentWidth()
                .padding(horizontal = metrics.horizontalPadding),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                fontSize = metrics.fontSize,
                lineHeight = metrics.lineHeight,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private data class SearchHistoryChipMetrics(
    val height: Dp,
    val maxWidth: Dp,
    val horizontalPadding: Dp,
    val cornerRadius: Dp,
    val fontSize: TextUnit,
    val lineHeight: TextUnit,
    val horizontalSpacing: Dp,
    val verticalSpacing: Dp,
)

private fun searchHistoryChipMetrics(windowSizeClass: TaggoWindowSizeClass): SearchHistoryChipMetrics =
    if (windowSizeClass == TaggoWindowSizeClass.Compact) {
        SearchHistoryChipMetrics(
            height = 34.dp,
            maxWidth = 180.dp,
            horizontalPadding = 14.dp,
            cornerRadius = 0.dp,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            horizontalSpacing = 12.dp,
            verticalSpacing = 12.dp,
        )
    } else {
        SearchHistoryChipMetrics(
            height = 32.dp,
            maxWidth = 220.dp,
            horizontalPadding = 13.dp,
            cornerRadius = 0.dp,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            horizontalSpacing = 11.dp,
            verticalSpacing = 11.dp,
        )
    }

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AllTagsEntrySection(
    allTags: List<String>,
    locale: AppLocale,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    compactLayout: Boolean,
    onTagClick: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
) {
    val chipSpacing = operableTagChipSpacing(LocalTaggoWindowSizeClass.current)
    if (compactLayout) {
        TaggoSectionCard(
            title = if (locale == AppLocale.ZhCn) "\u5168\u90e8\u6807\u7b7e" else "All tags",
            meta = if (locale == AppLocale.ZhCn) "\u70b9\u51fb\u8fdb\u5165\u641c\u7d22\uff0c\u53f3\u4e0a\u89d2 \u00d7 \u53ef\u5220\u9664\u6807\u7b7e" else "Click to search. Use the corner \u00d7 to delete a tag.",
            compact = true,
            compactPadding = TaggoCompactTokens.FileItemHorizontalPadding,
            compactContentGap = TaggoCompactTokens.FileItemGap,
            trailing = {},
        ) {
            if (allTags.isEmpty()) {
                EmptyPanel(
                    title = if (locale == AppLocale.ZhCn) "\u6682\u65e0\u6807\u7b7e" else "No tags yet",
                    body = if (locale == AppLocale.ZhCn) "\u5148\u4e3a\u6587\u4ef6\u6dfb\u52a0\u6807\u7b7e\u3002" else "Add tags to files first.",
                    compactGlass = true,
                    compactGlassIcon = Icons.Outlined.LocalOffer,
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(chipSpacing.horizontal),
                    verticalArrangement = Arrangement.spacedBy(chipSpacing.vertical),
                ) {
                    allTags.forEach { tag ->
                        TaggoTagDeleteChip(
                            tag = tag,
                            fullCjkFontReady = fullCjkFontReady,
                            fullCjkFontFamily = fullCjkFontFamily,
                            onClick = { onTagClick(tag) },
                            onDelete = { onRemoveTag(tag) },
                        )
                    }
                }
            }
        }
    } else {
        SectionCard(
            title = if (locale == AppLocale.ZhCn) "\u5168\u90e8\u6807\u7b7e" else "All tags",
            subtitle = if (locale == AppLocale.ZhCn) "\u70b9\u51fb\u8fdb\u5165\u641c\u7d22\uff0c\u53f3\u4e0a\u89d2 \u00d7 \u53ef\u5220\u9664\u6807\u7b7e" else "Click to search. Use the corner \u00d7 to delete a tag.",
        ) {
            if (allTags.isEmpty()) {
                EmptyPanel(
                    title = if (locale == AppLocale.ZhCn) "\u6682\u65e0\u6807\u7b7e" else "No tags yet",
                    body = if (locale == AppLocale.ZhCn) "\u5148\u4e3a\u6587\u4ef6\u6dfb\u52a0\u6807\u7b7e\u3002" else "Add tags to files first.",
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(chipSpacing.horizontal),
                    verticalArrangement = Arrangement.spacedBy(chipSpacing.vertical),
                ) {
                    allTags.forEach { tag ->
                        TaggoTagDeleteChip(
                            tag = tag,
                            fullCjkFontReady = fullCjkFontReady,
                            fullCjkFontFamily = fullCjkFontFamily,
                            onClick = { onTagClick(tag) },
                            onDelete = { onRemoveTag(tag) },
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
    val compactLayout = LocalTaggoWindowSizeClass.current == TaggoWindowSizeClass.Compact
    if (compactLayout) {
        TaggoSectionCard(
            title = if (locale == AppLocale.ZhCn) "当前搜索标签" else "Active search tags",
            meta = if (locale == AppLocale.ZhCn) "删除任意标签后会立刻用剩余标签重新搜索" else "Removing a tag immediately refreshes the results",
            compact = true,
            compactPadding = TaggoCompactTokens.FileItemHorizontalPadding,
            compactContentGap = TaggoCompactTokens.FileItemGap,
            trailing = {},
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TaggoCompactTokens.FileItemGap),
                verticalArrangement = Arrangement.spacedBy(TaggoCompactTokens.FileItemGap),
            ) {
                tags.forEach { tag ->
                    TaggoSearchConditionChip(
                        tag = tag,
                        fullCjkFontReady = fullCjkFontReady,
                        fullCjkFontFamily = fullCjkFontFamily,
                        onRemove = { onRemove(tag.value) },
                    )
                }
            }
        }
        return
    }

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
                TaggoSearchConditionChip(
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
    failedRetryThumbnailIds: MutableSet<String>,
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
            modifier = if (compactLayout) {
                Modifier.padding(horizontal = TaggoCompactTokens.PageHorizontalInsetExtra)
            } else {
                Modifier
            },
            verticalArrangement = Arrangement.spacedBy(
                if (compactLayout) TaggoCompactTokens.PageSectionGap else 14.dp,
            ),
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
    var tagFeedbackIsWarning by remember(reference.id) { mutableStateOf(false) }
    var pendingOpenReplacement by remember(reference.id) { mutableStateOf<BrowserReferenceDraft?>(null) }
    val isBrowserSelectedReference = reference.source.startsWith("browser-", ignoreCase = true)
    val tagCandidates = remember(reference.tags, appState.topTags) {
        resolveDetailTagCandidates(reference.tags, appState.topTags)
    }
    val canUseWebReopenFlow = browserReferencePicker != null &&
        browserReferenceResolver != null &&
        isBrowserSelectedReference
    val canOpenFile = canOpenReferenceExternally(reference) ||
        canUseWebReopenFlow
    val openButtonLabel = if (locale == AppLocale.ZhCn) "\u2197 \u6253\u5f00\u6b64\u6587\u4ef6" else "\u2197 Open this file"
    val detailCategory = FileTypeClassifier.classify(reference)

    LaunchedEffect(reference.id, reference.title, reference.fileType, reference.thumbnailStatus, reference.thumbnailPath) {
        debugLog(
            "TaggoThumbnailUi",
            "detail enter id=${reference.id} fileName=${reference.title.thumbnailLogValue()} " +
                "category=$detailCategory thumbnailStatus=${reference.thumbnailStatus} " +
                "thumbnailPath=${reference.thumbnailPath.thumbnailPathSummaryForLog()}"
        )
    }

    LaunchedEffect(reference.id, reference.source, reference.thumbnailStatus, reference.thumbnailPath) {
        val canRetryFailed = reference.thumbnailStatus == ThumbnailStatus.FAILED &&
            reference.needsThumbnailGeneration() &&
            reference.id !in failedRetryThumbnailIds
        val shouldGenerate = reference.thumbnailStatus == ThumbnailStatus.NONE &&
            reference.needsThumbnailGeneration()
        val shouldForceRetryFailed = canRetryFailed
        val skipReason = when {
            shouldGenerate -> "none"
            shouldForceRetryFailed -> "previous failure retry"
            !reference.needsThumbnailGeneration() -> "unsupported category"
            reference.thumbnailStatus == ThumbnailStatus.GENERATING -> "already generating"
            reference.thumbnailStatus == ThumbnailStatus.READY &&
                !reference.thumbnailPath.isNullOrBlank() -> "ready with thumbnail path"
            reference.thumbnailStatus == ThumbnailStatus.READY -> "ready without thumbnail path"
            reference.thumbnailStatus == ThumbnailStatus.FAILED &&
                reference.id in failedRetryThumbnailIds -> "previous failure already retried"
            reference.thumbnailStatus == ThumbnailStatus.FAILED -> "previous failure"
            reference.thumbnailStatus == ThumbnailStatus.UNSUPPORTED -> "unsupported status"
            else -> "unknown"
        }
        debugLog(
            "TaggoThumbnailState",
            "generation decision id=${reference.id} fileName=${reference.title.thumbnailLogValue()} " +
                "category=$detailCategory thumbnailStatus=${reference.thumbnailStatus} " +
                "trigger=${shouldGenerate || shouldForceRetryFailed} force=$shouldForceRetryFailed " +
                "skipReason=${skipReason.thumbnailLogValue()}"
        )
        if (shouldGenerate) {
            appState.generateThumbnailForReference(reference.id)
        } else if (shouldForceRetryFailed) {
            failedRetryThumbnailIds.add(reference.id)
            appState.generateThumbnailForReference(reference.id, force = true)
        }
    }

    fun clearDuplicateTagWarning() {
        if (tagFeedbackIsWarning) {
            tagFeedbackMessage = null
            tagFeedbackIsWarning = false
        }
    }

    fun addTagToCurrentReference(rawTag: String): Boolean {
        val cleaned = rawTag.trim()
        if (cleaned.isBlank()) {
            tagFeedbackMessage = null
            tagFeedbackIsWarning = false
            return false
        }
        val existingTag = resolveExistingTagExact(tagCandidates, cleaned)
        if (existingTag == null && !isFormalTagLengthValid(cleaned)) {
            tagFeedbackMessage = FORMAL_TAG_LENGTH_LIMIT_MESSAGE
            tagFeedbackIsWarning = true
            return false
        }

        val tagToAdd = existingTag ?: cleaned
        if (reference.tags.any { it.trim() == tagToAdd }) {
            tagFeedbackMessage = if (locale == AppLocale.ZhCn) {
                "\u8be5\u6587\u4ef6\u5df2\u5305\u542b\u6b64\u6807\u7b7e"
            } else {
                "This file already has that tag."
            }
            tagFeedbackIsWarning = true
            return false
        }

        appState.updateReferenceTags(
            reference.id,
            reference.tags.plus(tagToAdd).joinToString(", "),
        )
        tagFeedbackMessage = null
        tagFeedbackIsWarning = false
        newTagDraft = ""
        return true
    }

    fun removeTagFromCurrentReference(tag: String) {
        appState.updateReferenceTags(
            reference.id,
            removeTagExact(reference.tags, tag),
        )
        tagFeedbackMessage = null
        tagFeedbackIsWarning = false
    }

    LaunchedEffect(reference.id, reference.source, reference.sourceKind) {
        // 鏂囦欢鏉ユ簮涓€鏃﹀彉鍖栵紝灏辨妸涓婁竴娆＄殑鎵撳紑澶辫触鎻愮ず娓呮帀锛岄伩鍏嶆棫鐘舵€佽瀵肩敤鎴枫€?        openFileMessage = null
        tagFeedbackMessage = null
        tagFeedbackIsWarning = false
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
        modifier = if (compactLayout) {
            Modifier.padding(horizontal = TaggoCompactTokens.PageHorizontalInsetExtra)
        } else {
            Modifier
        },
        verticalArrangement = Arrangement.spacedBy(
            if (compactLayout) TaggoCompactTokens.PageSectionGap else 14.dp,
        ),
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

        val detailInfoTitle = if (locale == AppLocale.ZhCn) "\u6587\u4ef6\u4fe1\u606f" else "File info"
        val detailInfoSubtitle: String? = null
        val detailInfoContent: @Composable () -> Unit = if (compactLayout) {
            {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val fileNameValue = displayTextForUi(reference.title, fullCjkFontReady)
                    val fileTypeValue = displayTextForUi(reference.fileType, fullCjkFontReady).ifBlank { "file" }
                    val coverArtSourceValue = displayTextForUi(reference.coverArtSource.orEmpty(), fullCjkFontReady)
                    val fileNameFitsInline = rememberCompactDetailFieldFitsInline(
                        value = fileNameValue,
                        availableWidth = maxWidth,
                        fullCjkFontReady = fullCjkFontReady,
                        widthFraction = 0.42f,
                    )
                    val coverArtFitsInline = reference.coverArtSource?.isNotBlank() == true &&
                        rememberCompactDetailFieldFitsInline(
                            value = coverArtSourceValue,
                            availableWidth = maxWidth,
                            fullCjkFontReady = fullCjkFontReady,
                            valueFontFamily = fullCjkFontFamily,
                            widthFraction = 0.42f,
                        )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(TaggoTheme.spacing.md),
                    ) {
                        if (fileNameFitsInline) {
                            CompactDetailShortField(
                                label = if (locale == AppLocale.ZhCn) "\u6587\u4ef6\u540d" else "File name",
                                value = fileNameValue,
                            )
                        } else {
                            CompactDetailLongField(
                                label = if (locale == AppLocale.ZhCn) "\u6587\u4ef6\u540d" else "File name",
                                value = fileNameValue,
                                valueFontFamily = fullCjkFontFamily,
                            )
                        }
                        CompactDetailShortField(
                            label = appState.createdAtLabel,
                            value = formatRelativeTime(reference.createdAtMillis),
                        )
                        CompactDetailShortField(
                            label = if (locale == AppLocale.ZhCn) "\u6587\u4ef6\u5927\u5c0f" else "File size",
                            value = formatFileSize(reference.fileSizeBytes ?: guessFileSizeFromNotes(reference.notes)),
                        )
                        CompactDetailShortField(
                            label = if (locale == AppLocale.ZhCn) "\u6587\u4ef6\u7c7b\u578b" else "File type",
                            value = fileTypeValue,
                        )
                        if (reference.coverArtSource?.isNotBlank() == true) {
                            if (coverArtFitsInline) {
                                CompactDetailShortField(
                                    label = if (locale == AppLocale.ZhCn) "\u5c01\u9762\u6765\u6e90" else "Cover art source",
                                    value = coverArtSourceValue,
                                )
                            } else {
                                CompactDetailLongField(
                                    label = if (locale == AppLocale.ZhCn) "\u5c01\u9762\u6765\u6e90" else "Cover art source",
                                    value = coverArtSourceValue,
                                    valueFontFamily = fullCjkFontFamily,
                                )
                            }
                        }
                        CompactDetailLongField(
                            label = if (locale == AppLocale.ZhCn) "\u6587\u4ef6\u8def\u5f84" else "File path",
                            value = summarizeReferenceSource(reference, locale, fullCjkFontReady).ifBlank {
                                if (locale == AppLocale.ZhCn) "\u6682\u672a\u4fdd\u5b58\u8def\u5f84" else "No file path saved"
                            },
                            valueFontFamily = fullCjkFontFamily,
                        )
                        if (shouldShowThumbnailStatusForDetail(reference)) {
                            CompactDetailShortField(
                                label = if (locale == AppLocale.ZhCn) "\u7f29\u7565\u56fe\u7f13\u5b58" else "Thumbnail cache",
                                value = thumbnailStatusLabel(reference, locale),
                            )
                        }
                        if (openFileMessage != null) {
                            Text(
                                text = openFileMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = TaggoCompactTokens.Caption,
                            )
                        } else if (canUseWebReopenFlow && !canOpenReferenceExternally(reference)) {
                            Text(
                                text = webFileReselectionMessage(locale),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = TaggoCompactTokens.Caption,
                            )
                        } else if (!canOpenFile) {
                            Text(
                                text = if (locale == AppLocale.ZhCn) {
                                    "\u8fd9\u4e2a\u6587\u4ef6\u8fd8\u6ca1\u6709\u53ef\u76f4\u63a5\u6253\u5f00\u7684\u672c\u5730\u8def\u5f84\u3002"
                                } else {
                                    "This file does not have a directly openable local path."
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = TaggoCompactTokens.Caption,
                            )
                        }
                    }
                }
            }
        } else {
            {
                Column(
                    verticalArrangement = Arrangement.spacedBy(
                        if (compactLayout) TaggoCompactTokens.FileItemGap else 8.dp,
                    ),
                ) {
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
                    if (shouldShowThumbnailStatusForDetail(reference)) {
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
                            fontSize = if (compactLayout) TaggoCompactTokens.Caption else 12.sp,
                        )
                    } else if (canUseWebReopenFlow && !canOpenReferenceExternally(reference)) {
                        Text(
                            text = webFileReselectionMessage(locale),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = if (compactLayout) TaggoCompactTokens.Caption else 12.sp,
                        )
                    } else if (!canOpenFile) {
                        Text(
                            text = if (locale == AppLocale.ZhCn) {
                                "\u8fd9\u4e2a\u6587\u4ef6\u8fd8\u6ca1\u6709\u53ef\u76f4\u63a5\u6253\u5f00\u7684\u672c\u5730\u8def\u5f84\u3002"
                            } else {
                                "This file does not have a directly openable local path."
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = if (compactLayout) TaggoCompactTokens.Caption else 12.sp,
                        )
                    }
                }
            }
        }
        if (compactLayout) {
            DetailCompactSectionCard(
                title = detailInfoTitle,
                content = detailInfoContent,
            )
        } else {
            SectionCard(
                title = detailInfoTitle,
                subtitle = detailInfoSubtitle ?: "",
                content = detailInfoContent,
            )
        }

        val detailTagsTitle = if (locale == AppLocale.ZhCn) "\u6807\u7b7e" else "Tags"
        val detailTagsSubtitle: String? = null
        val detailTagsContent: @Composable () -> Unit = {
            Column(
                verticalArrangement = Arrangement.spacedBy(
                    if (compactLayout) {
                        TaggoCompactTokens.FileItemHorizontalPadding
                    } else {
                        10.dp
                    },
                ),
            ) {
                if (reference.tags.isEmpty()) {
                    Text(
                        text = if (locale == AppLocale.ZhCn) "\u6682\u65e0\u6807\u7b7e" else "No tags yet",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = if (compactLayout) TaggoCompactTokens.Body else 13.sp,
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(
                            if (compactLayout) TaggoCompactTokens.FileItemGap else 8.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(
                            if (compactLayout) TaggoCompactTokens.FileItemGap else 8.dp,
                        ),
                    ) {
                        reference.tags.forEach { tag ->
                            TaggoFileTagChip(
                                tag = tag,
                                fullCjkFontReady = fullCjkFontReady,
                                fullCjkFontFamily = fullCjkFontFamily,
                                onRemove = { removeTagFromCurrentReference(tag) },
                            )
                        }
                    }
                }

                if (!tagFeedbackMessage.isNullOrBlank() && !(showTagDialog && tagFeedbackIsWarning)) {
                    Text(
                        text = tagFeedbackMessage.orEmpty(),
                        color = if (tagFeedbackIsWarning) TaggoTheme.colors.warning else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = if (compactLayout) TaggoCompactTokens.Caption else 12.sp,
                        fontWeight = if (tagFeedbackIsWarning) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }

                TextButton(onClick = { showTagDialog = true }) {
                    Text(if (locale == AppLocale.ZhCn) "+ \u6dfb\u52a0\u6807\u7b7e" else "+ Add tag")
                }
            }
        }
        if (compactLayout) {
            DetailCompactSectionCard(
                title = detailTagsTitle,
                content = detailTagsContent,
            )
        } else {
            SectionCard(
                title = detailTagsTitle,
                subtitle = detailTagsSubtitle ?: "",
                content = detailTagsContent,
            )
        }

        val detailActionsTitle = if (locale == AppLocale.ZhCn) "\u6587\u4ef6\u64cd\u4f5c" else "File actions"
        val detailActionsSubtitle: String? = null
        val detailActionsContent: @Composable () -> Unit = {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onReplaceReference(reference) },
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DetailActionLeadingIcon(
                        painter = painterResource(Res.drawable.taggo_hero_folder),
                        tint = TaggoTheme.colors.textPrimary,
                    )
            Text(if (locale == AppLocale.ZhCn) "\u66f4\u6362\u6587\u4ef6\u8def\u5f84" else "Change file path")
                }
            }
        }
        if (compactLayout) {
            DetailCompactSectionCard(
                title = detailActionsTitle,
                content = detailActionsContent,
            )
        } else {
            SectionCard(
                title = detailActionsTitle,
                subtitle = detailActionsSubtitle ?: "",
                content = detailActionsContent,
            )
        }

        val dangerZoneTitle = if (locale == AppLocale.ZhCn) "\u5371\u9669\u64cd\u4f5c" else "Danger zone"
        val dangerZoneSubtitle: String? = null
        val dangerZoneContent: @Composable () -> Unit = {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onDeleteReference,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TaggoTheme.colors.danger,
                ),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DetailActionLeadingIcon(
                        painter = painterResource(Res.drawable.taggo_hero_trash),
                        tint = TaggoTheme.colors.danger,
                    )
            Text(if (locale == AppLocale.ZhCn) "\u5220\u9664\u6587\u4ef6\u6761\u76ee" else "Delete file entry")
                }
            }
        }
        if (compactLayout) {
            DetailCompactSectionCard(
                title = dangerZoneTitle,
                content = dangerZoneContent,
            )
        } else {
            SectionCard(
                title = dangerZoneTitle,
                subtitle = dangerZoneSubtitle ?: "",
                content = dangerZoneContent,
            )
        }
    }

    if (showTagDialog) {
        AlertDialog(
            onDismissRequest = {
                clearDuplicateTagWarning()
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
                            color = if (tagFeedbackIsWarning) TaggoTheme.colors.warning else TaggoTheme.colors.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (tagFeedbackIsWarning) FontWeight.SemiBold else FontWeight.Normal,
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
                                    TaggoAddTagCandidateChip(
                                        tag = tag,
                                        fullCjkFontReady = fullCjkFontReady,
                                        fullCjkFontFamily = fullCjkFontFamily,
                                        onAdd = {
                                            if (addTagToCurrentReference(tag)) {
                                                newTagDraft = ""
                                                showTagDialog = false
                                            }
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
                        clearDuplicateTagWarning()
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
                Column(
                    verticalArrangement = Arrangement.spacedBy(
                        TaggoCompactTokens.FileItemHorizontalPadding,
                    ),
                ) {
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
                        fontSize = TaggoGlobalTypography.BodySmall,
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
    compactGlass: Boolean = false,
) {
    val windowSizeClass = LocalTaggoWindowSizeClass.current
    val compactLayout = windowSizeClass == TaggoWindowSizeClass.Compact
    val cardRadius = if (compactLayout) TaggoCompactTokens.CardRadius else 20.dp
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (compactGlass && compactLayout) {
                Color.Transparent
            } else {
                TaggoTheme.colors.panelBackground
            },
        ),
        shape = RoundedCornerShape(cardRadius),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (compactGlass && compactLayout) {
                    Modifier
                        .background(TaggoCompactTokens.glassCardBackgroundBrush(), RoundedCornerShape(cardRadius))
                        .border(1.dp, TaggoCompactTokens.GlassCardBorder, RoundedCornerShape(cardRadius))
                } else {
                    Modifier.border(
                        if (compactLayout) TaggoCompactTokens.BorderWidth else 1.dp,
                        TaggoTheme.colors.panelBorder,
                        RoundedCornerShape(cardRadius),
                    )
                },
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 14.dp,
                    vertical = if (compactLayout) TaggoGlobalSpacing.Sm else 11.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(if (compactLayout) 5.dp else 0.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onMenuClick != null) {
                    TaggoIconActionButton(
                        icon = Icons.Outlined.Menu,
                        contentDescription = if (locale == AppLocale.ZhCn) "\u83dc\u5355" else "Menu",
                        onClick = onMenuClick,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                if (onBack != null) {
                    TaggoIconActionButton(
                        icon = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = if (locale == AppLocale.ZhCn) "\u8fd4\u56de" else "Back",
                        onClick = onBack,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                leading()

                if (compactLayout) {
                    if (title.isNotBlank()) {
                        Text(
                            text = title,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    trailing()
                } else {
                    Text(
                        text = title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
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
                        text = if (locale == AppLocale.ZhCn) "\u83dc\u5355" else "Menu",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    TextButton(onClick = onClose) {
                        Text(if (locale == AppLocale.ZhCn) "\u5173\u95ed" else "Close")
                    }
                }

                Text(
                    text = if (locale == AppLocale.ZhCn) {
                        "\u5207\u6362\u5e94\u7528\u663e\u793a\u8bed\u8a00\u3002"
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
private fun ToolButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    primary: Boolean = true,
    preferredHeight: Dp? = null,
    horizontalPadding: Dp? = null,
) {
    val windowSizeClass = LocalTaggoWindowSizeClass.current
    val compactLayout = windowSizeClass == TaggoWindowSizeClass.Compact
    val buttonHeight = preferredHeight ?: if (compactLayout) 42.dp else 38.dp
    val buttonHorizontalPadding = horizontalPadding ?: if (compactLayout) {
        14.dp
    } else {
        14.dp
    }
    val containerColor = if (primary) {
        TaggoTheme.colors.primaryAccent.copy(
            alpha = if (compactLayout) TaggoCompactTokens.PrimaryButtonAlpha else 0.84f,
        )
    } else {
        TaggoTheme.colors.panelBackgroundSoft
    }
    val contentColor = if (primary) TaggoTheme.colors.textPrimary else TaggoTheme.colors.textSecondary

    Button(
        modifier = modifier.height(buttonHeight),
        shape = RoundedCornerShape(if (compactLayout) TaggoCompactTokens.ButtonRadius else 11.dp),
        contentPadding = PaddingValues(horizontal = buttonHorizontalPadding, vertical = 0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = TaggoTheme.colors.surfaceVariant.copy(
                alpha = if (compactLayout) TaggoCompactTokens.DisabledSurfaceAlpha else 0.55f,
            ),
            disabledContentColor = TaggoTheme.colors.textMuted,
        ),
        onClick = onClick,
        enabled = enabled,
    ) {
        Text(
            text = label,
            fontSize = if (compactLayout) TaggoCompactTokens.Caption else 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ResponsiveSearchControls(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    placeholder: String,
    buttonLabel: String,
    textStyle: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier,
) {
    val windowSizeClass = LocalTaggoWindowSizeClass.current
    val compactLayout = windowSizeClass == TaggoWindowSizeClass.Compact
    val searchHeight = if (compactLayout) TaggoCompactTokens.SearchFieldHeight else 58.dp
    val inputTextStyle = textStyle.copy(
        color = TaggoTheme.colors.textPrimary,
        fontSize = if (compactLayout) TaggoCompactTokens.Body else 14.sp,
        lineHeight = if (compactLayout) TaggoCompactTokens.SearchLineHeight else 20.sp,
    )
    val placeholderTextStyle = inputTextStyle.copy(
        color = TaggoTheme.colors.textMuted,
        fontSize = if (compactLayout) TaggoCompactTokens.Placeholder else 13.sp,
    )

    BoxWithConstraints(modifier = modifier) {
        val inlineSearch = !compactLayout || maxWidth >= TaggoCompactTokens.SearchInlineThreshold
        if (inlineSearch) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .weight(1f)
                        .height(searchHeight),
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = {
                        Text(
                            text = placeholder,
                            style = placeholderTextStyle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                    textStyle = inputTextStyle,
                    colors = if (compactLayout) {
                        taggoCompactSearchTextFieldColors()
                    } else {
                        taggoOutlinedTextFieldColors()
                    },
                )
                ToolButton(
                    label = buttonLabel,
                    onClick = onSearch,
                    modifier = Modifier.widthIn(
                        min = if (compactLayout) TaggoCompactTokens.SearchButtonMinWidth else 82.dp,
                    ),
                    preferredHeight = if (compactLayout) TaggoCompactTokens.SearchButtonHeight else null,
                    horizontalPadding = if (compactLayout) {
                        TaggoCompactTokens.SearchButtonHorizontalPadding
                    } else {
                        null
                    },
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(searchHeight),
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = {
                        Text(
                            text = placeholder,
                            style = placeholderTextStyle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                    textStyle = inputTextStyle,
                    colors = if (compactLayout) {
                        taggoCompactSearchTextFieldColors()
                    } else {
                        taggoOutlinedTextFieldColors()
                    },
                )
                ToolButton(
                    label = buttonLabel,
                    onClick = onSearch,
                    modifier = Modifier.wrapContentWidth(Alignment.End),
                    preferredHeight = if (compactLayout) TaggoCompactTokens.SearchButtonHeight else null,
                    horizontalPadding = if (compactLayout) {
                        TaggoCompactTokens.SearchButtonHorizontalPadding
                    } else {
                        null
                    },
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
    textStyle: androidx.compose.ui.text.TextStyle,
    supportingText: String? = null,
) {
    val compactLayout = LocalTaggoWindowSizeClass.current == TaggoWindowSizeClass.Compact
    val cardRadius = if (compactLayout) TaggoCompactTokens.CardRadius else 20.dp
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (compactLayout) Color.Transparent else TaggoTheme.colors.panelBackground,
        ),
        shape = RoundedCornerShape(cardRadius),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (compactLayout) {
                    Modifier
                        .clip(RoundedCornerShape(cardRadius))
                        .background(TaggoCompactTokens.Search.BackgroundBrush, RoundedCornerShape(cardRadius))
                        .border(
                            TaggoCompactTokens.BorderWidth,
                            TaggoCompactTokens.Search.Border,
                            RoundedCornerShape(cardRadius),
                        )
                        .drawBehind {
                            drawRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        TaggoCompactTokens.Search.InnerHighlight,
                                        Color.Transparent,
                                    ),
                                    center = Offset(size.width * 0.12f, size.height * 0.16f),
                                    radius = size.maxDimension * 0.95f,
                                ),
                            )
                        }
                } else {
                    Modifier.border(
                        1.dp,
                        TaggoTheme.colors.panelBorder,
                        RoundedCornerShape(cardRadius),
                    )
                }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(
                if (compactLayout) TaggoCompactTokens.SearchCardPadding else 13.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(
                if (compactLayout) TaggoGlobalSpacing.Sm else 8.dp,
            ),
        ) {
            ResponsiveSearchControls(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = onSearch,
                placeholder = placeholder,
                buttonLabel = buttonLabel,
                textStyle = textStyle,
            )
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
    val containerModifier = when (windowSizeClass) {
        TaggoWindowSizeClass.Expanded -> Modifier.widthIn(min = 280.dp, max = 520.dp)
        TaggoWindowSizeClass.Medium -> Modifier.fillMaxWidth().widthIn(max = 360.dp)
        TaggoWindowSizeClass.Compact -> Modifier.fillMaxWidth()
    }

    ResponsiveSearchControls(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = onSearch,
        placeholder = placeholder,
        buttonLabel = buttonLabel,
        textStyle = textStyle,
        modifier = containerModifier,
    )
}

@Composable
private fun taggoOutlinedTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = TaggoTheme.colors.panelBackgroundSoft,
    unfocusedContainerColor = TaggoTheme.colors.panelBackgroundSoft,
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
private fun taggoCompactSearchTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    focusedTextColor = TaggoGlobalColors.TextPrimary,
    unfocusedTextColor = TaggoGlobalColors.TextPrimary,
    disabledTextColor = TaggoGlobalColors.TextMuted,
    focusedLabelColor = TaggoCompactTokens.Search.Placeholder,
    unfocusedLabelColor = TaggoCompactTokens.Search.Placeholder,
    disabledLabelColor = TaggoCompactTokens.Search.Placeholder,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
    focusedPlaceholderColor = TaggoCompactTokens.Search.Placeholder,
    unfocusedPlaceholderColor = TaggoCompactTokens.Search.Placeholder,
    focusedSupportingTextColor = TaggoGlobalColors.TextSecondary,
    unfocusedSupportingTextColor = TaggoGlobalColors.TextSecondary,
    disabledSupportingTextColor = TaggoGlobalColors.TextMuted,
    cursorColor = TaggoGlobalColors.PrimaryAccent,
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
                maxWidth >= 760.dp -> 3
                maxWidth >= 420.dp -> 2
                else -> 1
            }
        }
        val spacing = if (windowSizeClass == TaggoWindowSizeClass.Compact) {
            TaggoCompactTokens.FileItemGap
        } else {
            10.dp
        }
        val tileWidth = (maxWidth - spacing * (columns - 1)) / columns
        val tileHeight = when (windowSizeClass) {
            TaggoWindowSizeClass.Compact -> TaggoCompactTokens.FileItemHeight
            TaggoWindowSizeClass.Medium -> 104.dp
            TaggoWindowSizeClass.Expanded -> 216.dp
        }

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
                            modifier = Modifier
                                .width(tileWidth)
                                .height(tileHeight),
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
    val mediumLayout = windowSizeClass == TaggoWindowSizeClass.Medium
    val desktopThumbnailSize = if (mediumLayout) TaggoFileCoverTokens.MediumFileTileCoverSize else 72.dp
    val metaLine = remember(reference, fullCjkFontReady) {
        buildList {
            add(formatRelativeTime(reference.lastOpenedAtMillis))
            add(formatFileSize(reference.fileSizeBytes ?: guessFileSizeFromNotes(reference.notes)))
        }.joinToString(separator = " • ")
    }
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (compactLayout) Color.Transparent else TaggoTheme.colors.panelBackgroundSoft,
        ),
        shape = RoundedCornerShape(
            if (compactLayout) TaggoCompactTokens.ItemRadius else 16.dp,
        ),
        modifier = modifier
            .then(
                if (compactLayout) {
                    Modifier
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = TaggoTheme.colors.panelBorder,
                        shape = RoundedCornerShape(16.dp),
                    )
                },
            )
            .then(if (compactLayout) Modifier else Modifier.clickable(onClick = onOpen)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        if (compactLayout) {
            TaggoListItemSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(TaggoCompactTokens.ItemRadius),
                backgroundBrush = TaggoCompactTokens.glassListItemBackgroundBrush(),
                backgroundColor = TaggoTheme.colors.panelBackgroundSoft,
                borderColor = Color(0x24A9B8FF),
                height = TaggoCompactTokens.FileItemHeight,
                borderWidth = TaggoCompactTokens.BorderWidth,
                contentPadding = PaddingValues(
                    horizontal = TaggoCompactTokens.FileItemHorizontalPadding,
                    vertical = TaggoCompactTokens.FileItemVerticalPadding,
                ),
                horizontalArrangement = Arrangement.spacedBy(
                    TaggoCompactTokens.FileItemHorizontalPadding,
                ),
                verticalAlignment = Alignment.CenterVertically,
                onClick = onOpen,
            ) {
                FileCoverArtFrame(
                    reference = reference,
                    iconStyle = iconStyle,
                    fullCjkFontReady = fullCjkFontReady,
                    fullCjkFontFamily = fullCjkFontFamily,
                    modifier = Modifier.size(TaggoCompactTokens.FileCoverSize),
                    iconSize = TaggoCompactTokens.FileIconSize,
                    overlayContainerSize = 18.dp,
                    overlayIconSize = 12.dp,
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(TaggoCompactTokens.FileCoverSize),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = displayTextForUi(reference.title, fullCjkFontReady),
                        color = TaggoTheme.colors.textPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        minLines = 1,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = fullCjkFontFamily,
                    )
            Text(
                        text = metaLine,
                        color = TaggoTheme.colors.textMuted,
                        fontSize = 13.sp,
                        lineHeight = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        } else if (mediumLayout) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 9.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FileCoverArtFrame(
                    reference = reference,
                    iconStyle = iconStyle,
                    fullCjkFontReady = fullCjkFontReady,
                    fullCjkFontFamily = fullCjkFontFamily,
                    modifier = Modifier.size(desktopThumbnailSize),
                    iconSize = if (windowSizeClass == TaggoWindowSizeClass.Medium) {
                        TaggoFileCoverTokens.MediumFileTileIconSize
                    } else {
                        42.dp
                    },
                    overlayContainerSize = 18.dp,
                    overlayIconSize = 11.dp,
                    overlayBackgroundAlpha = 0.12f,
                    overlayIconAlpha = 0.42f,
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(desktopThumbnailSize),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = displayTextForUi(reference.title, fullCjkFontReady),
                        modifier = Modifier.fillMaxWidth(),
                        color = TaggoTheme.colors.textPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 17.sp,
                        lineHeight = 24.sp,
                        minLines = 1,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = fullCjkFontFamily,
                    )
            Text(
                        text = metaLine,
                        modifier = Modifier.fillMaxWidth(),
                        color = TaggoTheme.colors.textMuted,
                        fontSize = 13.sp,
                        lineHeight = 17.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(TaggoTheme.colors.surfaceVariant),
                ) {
                    FileCoverArtFrame(
                        reference = reference,
                        iconStyle = iconStyle,
                        fullCjkFontReady = fullCjkFontReady,
                        fullCjkFontFamily = fullCjkFontFamily,
                        modifier = Modifier.fillMaxSize(),
                        cornerShape = RoundedCornerShape(14.dp),
                        iconSize = 42.dp,
                        overlayContainerSize = 18.dp,
                        overlayIconSize = 11.dp,
                        overlayBackgroundAlpha = 0.12f,
                        overlayIconAlpha = 0.42f,
                    )
                }
            Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    TaggoTheme.colors.panelBackground.copy(alpha = 0.82f),
                                    TaggoTheme.colors.panelBackground.copy(alpha = 0.96f),
                                ),
                            ),
                        ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 9.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            text = displayTextForUi(reference.title, fullCjkFontReady),
                            modifier = Modifier.fillMaxWidth(),
                            color = TaggoTheme.colors.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            minLines = 1,
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
                            TaggoInlineTagSummary(
                                tags = reference.tags,
                                locale = locale,
                                fullCjkFontReady = fullCjkFontReady,
                                fullCjkFontFamily = fullCjkFontFamily,
                                mode = TaggoInlineTagSummaryMode.File,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverflowTagHint(
    count: Int,
    locale: AppLocale,
) {
    Text(
        text = if (locale == AppLocale.ZhCn) "\u8fd8\u6709$count\u4e2a" else "+$count",
        color = TaggoTheme.colors.textMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
    )
}

private fun resolveVisibleCardTags(
    tags: List<String>,
    windowSizeClass: TaggoWindowSizeClass,
): List<String> {
    if (tags.isEmpty()) return emptyList()
    val maxVisibleCount = when (windowSizeClass) {
        TaggoWindowSizeClass.Expanded -> 3
        TaggoWindowSizeClass.Medium -> 3
        TaggoWindowSizeClass.Compact -> 3
    }
    val characterBudget = when (windowSizeClass) {
        TaggoWindowSizeClass.Expanded -> 30
        TaggoWindowSizeClass.Medium -> 22
        TaggoWindowSizeClass.Compact -> 20
    }
    val visible = mutableListOf<String>()
    var usedCharacters = 0
    for (tag in tags) {
        if (visible.size >= maxVisibleCount) break
        val normalizedLength = tag.trim().length.coerceAtMost(10)
        val nextCost = if (visible.isEmpty()) normalizedLength else normalizedLength + 2
        val remainingAfterThis = tags.size - (visible.size + 1)
        val overflowReserve = if (remainingAfterThis > 0) 6 else 0
        if (visible.isNotEmpty() && usedCharacters + nextCost + overflowReserve > characterBudget) break
        visible += tag
        usedCharacters += nextCost
    }
    return if (visible.isEmpty()) listOf(tags.first()) else visible
}

@Composable
private fun DetailCompactSectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(TaggoGlobalRadius.Card)
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .background(TaggoCompactTokens.glassCardBackgroundBrush(), shape)
            .border(1.dp, TaggoCompactTokens.GlassCardBorder, shape),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TaggoCompactTokens.FileItemHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(TaggoCompactTokens.FileItemGap),
        ) {
            Text(
                text = title,
                color = TaggoTheme.colors.textPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 26.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            content()
        }
    }
}

@Composable
private fun CompactDetailShortField(
    label: String,
    value: String,
) {
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(TaggoGlobalSpacing.Xxl),
          verticalAlignment = Alignment.CenterVertically,
      ) {
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                color = TaggoTheme.colors.textSecondary,
                fontSize = TaggoGlobalTypography.TitleMedium,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                modifier = Modifier.weight(1f),
                color = TaggoTheme.colors.textPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = TaggoGlobalTypography.TitleMedium,
                lineHeight = 20.sp,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
    }
}

@Composable
private fun CompactDetailLongField(
    label: String,
    value: String,
    valueFontFamily: FontFamily? = null,
) {
      Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(TaggoTheme.spacing.sm),
      ) {
          Text(
              text = label,
              color = TaggoTheme.colors.textSecondary,
              fontSize = TaggoGlobalTypography.TitleMedium,
              fontWeight = FontWeight.Normal,
              lineHeight = 20.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
            Text(
            text = value,
            modifier = Modifier.fillMaxWidth(),
              color = TaggoTheme.colors.textPrimary,
              fontWeight = FontWeight.Medium,
              fontSize = TaggoGlobalTypography.TitleMedium,
              lineHeight = 20.sp,
            fontFamily = valueFontFamily,
            textAlign = TextAlign.Start,
        )
    }
}

@Composable
private fun rememberCompactDetailFieldFitsInline(
    value: String,
    availableWidth: Dp,
    fullCjkFontReady: Boolean,
    valueFontFamily: FontFamily? = null,
    widthFraction: Float = 0.44f,
): Boolean {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val displayValue = displayTextForUi(value, fullCjkFontReady)
    return remember(displayValue, availableWidth, valueFontFamily, widthFraction) {
        if (displayValue.isBlank()) {
            true
        } else {
            val measuredWidth = textMeasurer.measure(
                text = AnnotatedString(displayValue),
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = valueFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp,
                ),
                maxLines = 1,
            ).size.width
            val widthLimitPx = with(density) { availableWidth.toPx() } * widthFraction
            measuredWidth <= widthLimitPx
        }
    }
}

private data class DetailHeroStyle(
    val backgroundGradientColors: List<Color>,
    val accentColor: Color,
    val glowColor: Color,
)

private fun resolveDetailHeroStyle(reference: FileReference): DetailHeroStyle {
    val category = FileTypeClassifier.classify(reference)
    val tokens = TaggoFileTypeColorTokens.forCategory(category)
    val tint = tokens.iconColor
    val baseBackground = when (category) {
        FileTypeCategory.Image -> Color(0xFF07141B)
        FileTypeCategory.Video -> Color(0xFF120B1D)
        FileTypeCategory.Audio -> Color(0xFF08131A)
        FileTypeCategory.TextDocument,
        FileTypeCategory.PdfDocument,
        FileTypeCategory.Spreadsheet,
        FileTypeCategory.Presentation -> Color(0xFF160F0B)
        FileTypeCategory.Archive -> Color(0xFF10121A)
        FileTypeCategory.Code -> Color(0xFF0D1623)
        FileTypeCategory.Folder -> Color(0xFF0E1620)
        else -> Color(0xFF0B111A)
    }
    val middleTint = when (category) {
        FileTypeCategory.Image -> Color(0xFF103448)
        FileTypeCategory.Video -> Color(0xFF2A1338)
        FileTypeCategory.Audio -> Color(0xFF0E3940)
        FileTypeCategory.TextDocument,
        FileTypeCategory.Spreadsheet,
        FileTypeCategory.Presentation -> Color(0xFF392012)
        FileTypeCategory.PdfDocument -> Color(0xFF40240C)
        FileTypeCategory.Archive -> Color(0xFF1A1A2A)
        FileTypeCategory.Code -> Color(0xFF18304A)
        FileTypeCategory.Folder -> Color(0xFF182635)
        else -> Color(0xFF121B26)
    }
    return DetailHeroStyle(
        backgroundGradientColors = listOf(
            baseBackground,
            middleTint,
            Color(0xFF080B11),
        ),
        accentColor = tint.copy(alpha = 0.88f),
        glowColor = tokens.weakGlowColor.copy(alpha = 0.18f),
    )
}

@Composable
private fun DetailHeroPreview(
    reference: FileReference,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    canOpenFile: Boolean,
    openButtonLabel: String,
    onOpenFile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val category = FileTypeClassifier.classify(reference)
    val heroStyle = resolveDetailHeroStyle(reference)
    val thumbnailPainter = rememberThumbnailPainter(reference.thumbnailPath)
    val hasRealPreview = thumbnailPainter != null &&
        reference.thumbnailStatus == ThumbnailStatus.READY &&
        (category == FileTypeCategory.Image || category == FileTypeCategory.Video)
    LaunchedEffect(reference.id, reference.thumbnailStatus, reference.thumbnailPath, thumbnailPainter != null, hasRealPreview) {
        debugLog(
            "TaggoThumbnailUi",
            "ui consume id=${reference.id} thumbnailStatus=${reference.thumbnailStatus} " +
                "thumbnailPainterNull=${thumbnailPainter == null} " +
                "display=${if (hasRealPreview) "thumbnail" else "fallback"} " +
                "thumbnailPath=${reference.thumbnailPath.thumbnailPathSummaryForLog()}"
        )
    }
    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(TaggoCompactTokens.HeroCoverRadius))
            .background(Brush.linearGradient(heroStyle.backgroundGradientColors))
            .then(
                if (canOpenFile) {
                    Modifier.clickable(onClick = onOpenFile)
                } else {
                    Modifier
                },
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val curveColor = heroStyle.accentColor.copy(alpha = 0.06f)
                    val curveStroke = Stroke(width = 1.dp.toPx())
                    val firstCurve = Path().apply {
                        moveTo(size.width * -0.06f, size.height * 0.68f)
                        cubicTo(
                            size.width * 0.28f,
                            size.height * 0.62f,
                            size.width * 0.70f,
                            size.height * 0.64f,
                            size.width * 1.06f,
                            size.height * 0.58f,
                        )
                    }
                    val secondCurve = Path().apply {
                        moveTo(size.width * -0.08f, size.height * 0.82f)
                        cubicTo(
                            size.width * 0.32f,
                            size.height * 0.76f,
                            size.width * 0.68f,
                            size.height * 0.78f,
                            size.width * 1.08f,
                            size.height * 0.72f,
                        )
                    }
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                heroStyle.glowColor,
                                Color.Transparent,
                            ),
                            center = Offset(size.width * 0.50f, size.height * 0.34f),
                            radius = size.minDimension * 0.92f,
                        ),
                    )
                    drawPath(path = firstCurve, color = curveColor, style = curveStroke)
                    drawPath(path = secondCurve, color = curveColor, style = curveStroke)
                },
        ) {
            if (hasRealPreview) {
                Image(
                    painter = thumbnailPainter!!,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(96.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(FileTypeVisuals.heroFallbackDrawableForCategory(category)),
                        contentDescription = null,
                        tint = heroStyle.accentColor.copy(alpha = 0.96f),
                        modifier = Modifier.size(96.dp),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.00f to Color.Transparent,
                                0.28f to Color.Transparent,
                                0.50f to Color.Black.copy(alpha = 0.28f),
                                0.68f to Color.Black.copy(alpha = 0.52f),
                                1.00f to Color.Black.copy(alpha = 0.78f),
                            ),
                        ),
                    ),
            )
            Text(
                text = displayTextForUi(reference.title, fullCjkFontReady),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(
                        start = TaggoTheme.spacing.lg,
                        end = 60.dp,
                        bottom = 18.dp,
                    ),
                color = TaggoTheme.colors.textPrimary.copy(alpha = 0.98f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                lineHeight = 24.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontFamily = fullCjkFontFamily,
            )
            HeroOpenIconButton(
                imageVector = if (category == FileTypeCategory.Audio || category == FileTypeCategory.Video) {
                    Icons.Outlined.PlayArrow
                } else {
                    Icons.AutoMirrored.Outlined.OpenInNew
                },
                contentDescription = openButtonLabel,
                onClick = onOpenFile,
                enabled = canOpenFile,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 18.dp, bottom = TaggoTheme.spacing.md)
                    .size(36.dp),
            )
        }
    }
}

@Composable
private fun HeroOpenIconButton(
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val iconTint = if (enabled) {
        Color.White.copy(alpha = 0.72f)
    } else {
        Color.White.copy(alpha = 0.30f)
    }
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        color = Color.Black.copy(alpha = 0.34f),
        contentColor = iconTint,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun DetailActionLeadingIcon(
    painter: Painter,
    tint: Color,
    iconSize: Dp = 22.dp,
) {
    Box(
        modifier = Modifier.width(26.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painter,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(iconSize),
        )
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
    Card(
        colors = CardDefaults.cardColors(containerColor = TaggoTheme.colors.panelBackground),
        shape = RoundedCornerShape(TaggoCompactTokens.CardRadius),
        modifier = Modifier
            .fillMaxWidth()
            .border(TaggoCompactTokens.BorderWidth, TaggoTheme.colors.panelBorder, RoundedCornerShape(TaggoCompactTokens.CardRadius)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TaggoCompactTokens.DetailHeroPadding),
            verticalArrangement = Arrangement.spacedBy(TaggoCompactTokens.DetailHeroPadding),
        ) {
            DetailHeroPreview(
                reference = reference,
                fullCjkFontReady = fullCjkFontReady,
                fullCjkFontFamily = fullCjkFontFamily,
                canOpenFile = canOpenFile,
                openButtonLabel = openButtonLabel,
                onOpenFile = onOpenFile,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(TaggoCompactTokens.DetailCoverAspectRatio),
            )
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
    errorMessage: String?,
    onDraftTagsChange: (String) -> Unit,
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
                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage,
                        color = TaggoTheme.colors.warning,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
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
                        onValueChange = onDraftTagsChange,
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

private fun typeFilterLabel(filter: AllFilesTypeFilter, locale: AppLocale): String =
    when (filter) {
        AllFilesTypeFilter.All -> if (locale == AppLocale.ZhCn) "\u5168\u90e8" else "All"
        AllFilesTypeFilter.Image -> if (locale == AppLocale.ZhCn) "\u56fe\u7247" else "Images"
        AllFilesTypeFilter.Video -> if (locale == AppLocale.ZhCn) "\u89c6\u9891" else "Videos"
        AllFilesTypeFilter.Audio -> if (locale == AppLocale.ZhCn) "\u97f3\u9891" else "Audio"
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
        FileTypeCategory.Audio -> AllFilesTypeFilter.Audio
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
    return isReferenceExternallyOpenable(reference)
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

private fun shouldShowThumbnailStatusForDetail(reference: FileReference): Boolean =
    when (FileTypeClassifier.classify(reference)) {
        FileTypeCategory.Image,
        FileTypeCategory.Video,
        -> true

        else -> false
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

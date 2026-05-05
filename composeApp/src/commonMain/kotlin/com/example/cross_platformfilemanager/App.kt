package com.example.cross_platformfilemanager

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

@Composable
@Preview
fun App() {
    val appState = remember { FileManagerAppState() }
    val snapshotStore = remember { createAppSnapshotStore() }
    val strings = remember(appState.locale) { AppStrings.forLocale(appState.locale) }

    LaunchedEffect(snapshotStore) {
        snapshotStore?.load()?.let { snapshot ->
            appState.restoreSnapshot(snapshot)
        }
    }

    LaunchedEffect(appState.snapshotVersion, snapshotStore) {
        snapshotStore?.save(appState.exportSnapshot())
    }

    MaterialTheme(colorScheme = appColorScheme()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(appBackgroundBrush())
            ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                    HeaderBar(
                        strings = strings,
                        locale = appState.locale,
                        onLocaleChange = { appState.locale = it },
                        onClearFilters = {
                            appState.query = ""
                            appState.selectedTag = null
                        },
                    )

                    val stats = remember(appState) { derivedStateOf { appState.dashboardStats() } }
                    StatsRow(stats = stats.value, strings = strings)

                    BoxWithConstraints(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        val wideLayout = maxWidth >= 1100.dp

                        if (wideLayout) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Column(
                                    modifier = Modifier.weight(1.15f),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                ) {
                                    QuickAddCard(
                                        strings = strings,
                                        title = appState.draftTitle,
                                        source = appState.draftSource,
                                        typeLabel = appState.draftType,
                                        tags = appState.draftTags,
                                        notes = appState.draftNotes,
                                        onTitleChange = { appState.draftTitle = it },
                                        onSourceChange = { appState.draftSource = it },
                                        onTypeChange = { appState.draftType = it },
                                        onTagsChange = { appState.draftTags = it },
                                        onNotesChange = { appState.draftNotes = it },
                                        onAdd = {
                                            appState.addDraftReference()
                                        },
                                    )

                                    SearchAndResultsPanel(
                                        strings = strings,
                                        query = appState.query,
                                        selectedTag = appState.selectedTag,
                                        querySuggestions = appState.querySuggestions,
                                        fileSuggestions = appState.fileSuggestions,
                                        searchResults = appState.searchResults,
                                        topTags = appState.topTags,
                                        onQueryChange = { appState.query = it },
                                        onSearch = { appState.commitSearch() },
                                        onTagSelected = { appState.toggleTagFilter(it) },
                                        onOpenFile = { appState.openReference(it) },
                                        onFavoriteToggle = { appState.toggleFavorite(it) },
                                        onApplySuggestedQuery = {
                                            appState.query = it
                                            appState.commitSearch()
                                        },
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(0.85f),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                ) {
                                    RecommendationPanel(
                                        strings = strings,
                                        recommendations = appState.recommendations,
                                        recentReferences = appState.recentReferences,
                                        selectedReference = appState.activeReference,
                                        onUseSuggestion = { appState.query = it },
                                        onTagSelected = { appState.toggleTagFilter(it) },
                                        onOpenFile = { appState.openReference(it) },
                                    )
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                QuickAddCard(
                                    strings = strings,
                                    title = appState.draftTitle,
                                    source = appState.draftSource,
                                    typeLabel = appState.draftType,
                                    tags = appState.draftTags,
                                    notes = appState.draftNotes,
                                    onTitleChange = { appState.draftTitle = it },
                                    onSourceChange = { appState.draftSource = it },
                                    onTypeChange = { appState.draftType = it },
                                    onTagsChange = { appState.draftTags = it },
                                    onNotesChange = { appState.draftNotes = it },
                                    onAdd = { appState.addDraftReference() },
                                )

                                SearchAndResultsPanel(
                                    strings = strings,
                                    query = appState.query,
                                    selectedTag = appState.selectedTag,
                                    querySuggestions = appState.querySuggestions,
                                    fileSuggestions = appState.fileSuggestions,
                                    searchResults = appState.searchResults,
                                    topTags = appState.topTags,
                                    onQueryChange = { appState.query = it },
                                    onSearch = { appState.commitSearch() },
                                    onTagSelected = { appState.toggleTagFilter(it) },
                                    onOpenFile = { appState.openReference(it) },
                                    onFavoriteToggle = { appState.toggleFavorite(it) },
                                    onApplySuggestedQuery = {
                                        appState.query = it
                                        appState.commitSearch()
                                    },
                                )

                                RecommendationPanel(
                                    strings = strings,
                                    recommendations = appState.recommendations,
                                    recentReferences = appState.recentReferences,
                                    selectedReference = appState.activeReference,
                                    onUseSuggestion = { appState.query = it },
                                    onTagSelected = { appState.toggleTagFilter(it) },
                                    onOpenFile = { appState.openReference(it) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun appColorScheme() = darkColorScheme(
    primary = Color(0xFF7DD3FC),
    secondary = Color(0xFFA78BFA),
    tertiary = Color(0xFF34D399),
    background = Color(0xFF07111F),
    surface = Color(0xFF0D1727),
    surfaceVariant = Color(0xFF162238),
    onPrimary = Color(0xFF04111C),
    onSecondary = Color.White,
    onTertiary = Color(0xFF04111C),
    onBackground = Color(0xFFE5EEF9),
    onSurface = Color(0xFFE5EEF9),
    onSurfaceVariant = Color(0xFFB9C8DA),
)

private fun appBackgroundBrush() = Brush.linearGradient(
    colors = listOf(
        Color(0xFF07111F),
        Color(0xFF0B1628),
        Color(0xFF101C30),
    )
)

@Composable
private fun HeaderBar(
    strings: UiStrings,
    locale: AppLocale,
    onLocaleChange: (AppLocale) -> Unit,
    onClearFilters: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0x141C2A3E))
            .border(1.dp, Color(0x223D5778), RoundedCornerShape(24.dp))
            .padding(18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = strings.appName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = strings.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusBadge(text = strings.localOnly)
            LocaleSwitcher(
                locale = locale,
                onLocaleChange = onLocaleChange,
            )
            TextButton(onClick = onClearFilters) {
                Text(strings.resetFilters)
            }
        }
    }
}

@Composable
private fun StatusBadge(text: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0x1A34D399))
            .border(1.dp, Color(0x4434D399), RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color(0xFF34D399))
        )
        Spacer(Modifier.width(8.dp))
        Text(text = text, fontSize = 12.sp, color = Color(0xFFB7F7D9))
    }
}

@Composable
private fun LocaleSwitcher(
    locale: AppLocale,
    onLocaleChange: (AppLocale) -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0x171B2A3B))
            .border(1.dp, Color(0x224F6787), RoundedCornerShape(999.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        FilterChip(
            selected = locale == AppLocale.ZhCn,
            onClick = { onLocaleChange(AppLocale.ZhCn) },
            label = { Text("\u4e2d\u6587") },
        )
        FilterChip(
            selected = locale == AppLocale.EnUs,
            onClick = { onLocaleChange(AppLocale.EnUs) },
            label = { Text("EN") },
        )
    }
}

@Composable
private fun StatsRow(
    stats: DashboardStats,
    strings: UiStrings,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatCard(strings.filesTracked, stats.fileCount.toString(), modifier = Modifier.weight(1f))
        StatCard(strings.tagsUsed, stats.tagCount.toString(), modifier = Modifier.weight(1f))
        StatCard(strings.recentSearches, stats.recentSearchCount.toString(), modifier = Modifier.weight(1f))
        StatCard(strings.recommendedNow, stats.recommendationCount.toString(), modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color(0x171F3148),
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(text = title, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            Text(text = value, fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun QuickAddCard(
    strings: UiStrings,
    title: String,
    source: String,
    typeLabel: String,
    tags: String,
    notes: String,
    onTitleChange: (String) -> Unit,
    onSourceChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    onTagsChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onAdd: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0x141D2B3C)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = strings.quickAddTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    text = strings.quickAddSubtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text(strings.referenceTitle) },
                    singleLine = true,
                )
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = source,
                    onValueChange = onSourceChange,
                    label = { Text(strings.referenceLocation) },
                    singleLine = true,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    modifier = Modifier.weight(0.8f),
                    value = typeLabel,
                    onValueChange = onTypeChange,
                    label = { Text(strings.fileType) },
                    singleLine = true,
                )
                OutlinedTextField(
                    modifier = Modifier.weight(1.2f),
                    value = tags,
                    onValueChange = onTagsChange,
                    label = { Text(strings.tagsCommaSeparated) },
                    singleLine = true,
                )
            }

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = notes,
                onValueChange = onNotesChange,
                label = { Text(strings.notes) },
                minLines = 3,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = strings.quickAddHint,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                Button(onClick = onAdd) {
                    Text(strings.addReference)
                }
            }
        }
    }
}

@Composable
private fun SearchAndResultsPanel(
    strings: UiStrings,
    query: String,
    selectedTag: String?,
    querySuggestions: List<Suggestion>,
    fileSuggestions: List<Suggestion>,
    searchResults: List<SearchResult>,
    topTags: List<String>,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onTagSelected: (String) -> Unit,
    onOpenFile: (String) -> Unit,
    onFavoriteToggle: (String) -> Unit,
    onApplySuggestedQuery: (String) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0x141D2B3C)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = strings.searchTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    text = strings.searchSubtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = query,
                onValueChange = onQueryChange,
                label = { Text(strings.searchPlaceholder) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                topTags.take(8).forEach { tag ->
                    AssistChip(
                        onClick = { onTagSelected(tag) },
                        label = { Text(tag) },
                    )
                }
            }

            SuggestionSection(
                title = strings.querySuggestions,
                suggestions = querySuggestions,
                onPick = onApplySuggestedQuery,
            )

            SuggestionSection(
                title = strings.fileSuggestions,
                suggestions = fileSuggestions,
                onPick = onApplySuggestedQuery,
            )

            HorizontalDivider(color = Color(0x224F6787))

            Text(
                text = if (selectedTag == null) strings.allResults else formatTagFilter(strings.filteredByTagLabel, selectedTag),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )

            if (searchResults.isEmpty()) {
                EmptyState(strings = strings)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    searchResults.forEach { result ->
                        ReferenceResultCard(
                            result = result,
                            onOpen = { onOpenFile(result.reference.id) },
                            onFavoriteToggle = { onFavoriteToggle(result.reference.id) },
                            onTagSelected = onTagSelected,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionSection(
    title: String,
    suggestions: List<Suggestion>,
    onPick: (String) -> Unit,
) {
    if (suggestions.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(text = title, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            suggestions.take(6).forEach { suggestion ->
                SuggestionChip(suggestion = suggestion, onPick = onPick)
            }
        }
    }
}

@Composable
private fun SuggestionChip(
    suggestion: Suggestion,
    onPick: (String) -> Unit,
) {
    AssistChip(
        onClick = { onPick(suggestion.label) },
        label = {
            Column {
                Text(suggestion.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    suggestion.reason,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
    )
}

@Composable
private fun ReferenceResultCard(
    result: SearchResult,
    onOpen: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onTagSelected: (String) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0x10182839)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = result.reference.title,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = result.reference.source,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                IconButton(onClick = onFavoriteToggle) {
                    Text(if (result.reference.isFavorite) "★" else "☆")
                }
            }

            Text(
                text = result.reason,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )

            TagRow(tags = result.reference.tags, onTagSelected = onTagSelected)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = result.reference.fileType,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onOpen) {
                        Text("Open")
                    }
                    Text(
                        text = result.scoreLabel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun RecommendationPanel(
    strings: UiStrings,
    recommendations: List<Suggestion>,
    recentReferences: List<FileReference>,
    selectedReference: FileReference?,
    onUseSuggestion: (String) -> Unit,
    onTagSelected: (String) -> Unit,
    onOpenFile: (String) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0x141D2B3C)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = strings.recommendationTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    text = strings.recommendationSubtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (selectedReference != null) {
                SelectedReferenceCard(
                    reference = selectedReference,
                    strings = strings,
                    onOpenFile = onOpenFile,
                    onTagSelected = onTagSelected,
                )
            }

            Text(
                text = strings.recommendedQueries,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recommendations.take(6).forEach { suggestion ->
                    ElevatedCard(
                        colors = CardDefaults.elevatedCardColors(containerColor = Color(0x10182839)),
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = suggestion.label,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = suggestion.kind.name.lowercase(),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp,
                                )
                            }
                            Text(
                                text = suggestion.reason,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                            )
                            TextButton(onClick = { onUseSuggestion(suggestion.label) }) {
                                Text(strings.useThisSuggestion)
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0x224F6787))

            Text(
                text = strings.recentlyOpened,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recentReferences.take(5).forEach { reference ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x10182839))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = reference.title,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = reference.source,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        TextButton(onClick = { onOpenFile(reference.id) }) {
                            Text(strings.open)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedReferenceCard(
    reference: FileReference,
    strings: UiStrings,
    onOpenFile: (String) -> Unit,
    onTagSelected: (String) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0x141A2A3B)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = strings.activeItem, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            Text(reference.title, fontWeight = FontWeight.SemiBold)
            Text(reference.source, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            TagRow(tags = reference.tags, onTagSelected = onTagSelected)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onOpenFile(reference.id) }) {
                    Text(strings.open)
                }
            }
        }
    }
}

@Composable
private fun EmptyState(strings: UiStrings) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = strings.emptyResultsTitle, fontWeight = FontWeight.SemiBold)
        Text(
            text = strings.emptyResultsBody,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun TagRow(
    tags: List<String>,
    onTagSelected: (String) -> Unit,
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tags.forEach { tag ->
            AssistChip(
                onClick = { onTagSelected(tag) },
                label = { Text(tag) },
            )
        }
    }
}

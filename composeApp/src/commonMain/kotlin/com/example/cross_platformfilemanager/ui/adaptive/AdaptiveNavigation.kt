package com.example.cross_platformfilemanager.ui.adaptive

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.cross_platformfilemanager.ui.theme.TaggoTheme

internal data class AdaptiveNavigationItem<Page>(
    val page: Page,
    val label: String,
    val icon: ImageVector? = null,
)

internal enum class AdaptiveNavigationPlacement {
    BottomBar,
    NavigationRail,
    SideBar,
}

internal fun navigationPlacementFor(
    windowSizeClass: TaggoWindowSizeClass,
): AdaptiveNavigationPlacement =
    when (windowSizeClass) {
        TaggoWindowSizeClass.Compact -> AdaptiveNavigationPlacement.BottomBar
        TaggoWindowSizeClass.Medium -> AdaptiveNavigationPlacement.NavigationRail
        TaggoWindowSizeClass.Expanded -> AdaptiveNavigationPlacement.SideBar
    }

@Composable
internal fun <Page> TaggoBottomNavigation(
    items: List<AdaptiveNavigationItem<Page>>,
    selectedPage: Page,
    onPageSelected: (Page) -> Unit,
) {
    if (items.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TaggoTheme.colors.border, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)),
    ) {
        NavigationBar(
            containerColor = TaggoTheme.colors.surface,
            contentColor = TaggoTheme.colors.textMuted,
        ) {
            items.forEach { item ->
                NavigationBarItem(
                    selected = item.page == selectedPage,
                    onClick = { onPageSelected(item.page) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TaggoTheme.colors.primaryAccent,
                        selectedTextColor = TaggoTheme.colors.textPrimary,
                        indicatorColor = TaggoTheme.colors.primaryAccentSoft,
                        unselectedIconColor = TaggoTheme.colors.textMuted,
                        unselectedTextColor = TaggoTheme.colors.textMuted,
                    ),
                    icon = {
                        item.icon?.let {
                            Icon(imageVector = it, contentDescription = null)
                        }
                    },
                    label = {
                        Text(
                            text = item.label,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                )
            }
        }
    }
}

@Composable
internal fun <Page> TaggoNavigationRail(
    items: List<AdaptiveNavigationItem<Page>>,
    selectedPage: Page,
    onPageSelected: (Page) -> Unit,
) {
    if (items.isEmpty()) return

    NavigationRail(
        containerColor = TaggoTheme.colors.surface,
        contentColor = TaggoTheme.colors.textMuted,
    ) {
        items.forEach { item ->
            NavigationRailItem(
                selected = item.page == selectedPage,
                onClick = { onPageSelected(item.page) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = TaggoTheme.colors.primaryAccent,
                    selectedTextColor = TaggoTheme.colors.textPrimary,
                    indicatorColor = TaggoTheme.colors.primaryAccentSoft,
                    unselectedIconColor = TaggoTheme.colors.textMuted,
                    unselectedTextColor = TaggoTheme.colors.textMuted,
                ),
                icon = {
                    item.icon?.let {
                        Icon(imageVector = it, contentDescription = null)
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
            )
        }
    }
}

@Composable
internal fun <Page> TaggoSidebarNavigation(
    items: List<AdaptiveNavigationItem<Page>>,
    selectedPage: Page,
    onPageSelected: (Page) -> Unit,
    footer: (@Composable () -> Unit)? = null,
) {
    if (items.isEmpty()) return

    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(220.dp)
            .border(1.dp, TaggoTheme.colors.border, RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp)),
        color = TaggoTheme.colors.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items.forEach { item ->
                SidebarNavigationItem(
                    item = item,
                    selected = item.page == selectedPage,
                    onClick = { onPageSelected(item.page) },
                )
            }
            if (footer != null) {
                Spacer(modifier = Modifier.weight(1f))
                footer()
            }
        }
    }
}

@Composable
internal fun <Page> SidebarNavigationItem(
    item: AdaptiveNavigationItem<Page>,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) TaggoTheme.colors.primaryAccentSoft else TaggoTheme.colors.surface,
        contentColor = if (selected) TaggoTheme.colors.textPrimary else TaggoTheme.colors.textSecondary,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            item.icon?.let {
                Icon(imageVector = it, contentDescription = null)
            }
            Text(
                text = item.label,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

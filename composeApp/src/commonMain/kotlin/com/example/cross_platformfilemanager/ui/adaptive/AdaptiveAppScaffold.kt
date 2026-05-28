package com.example.cross_platformfilemanager.ui.adaptive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier

@Composable
@Suppress("UNUSED_PARAMETER")
internal fun <Page> AdaptiveAppScaffold(
    currentPage: Page,
    selectedNavigationPage: Page = currentPage,
    navigationItems: List<AdaptiveNavigationItem<Page>> = emptyList(),
    onPageSelected: (Page) -> Unit,
    modifier: Modifier = Modifier,
    topBar: (@Composable (TaggoWindowSizeClass) -> Unit)? = null,
    sideBar: (@Composable (TaggoWindowSizeClass) -> Unit)? = null,
    sideBarFooter: (@Composable (TaggoWindowSizeClass) -> Unit)? = null,
    bottomBar: (@Composable (TaggoWindowSizeClass) -> Unit)? = null,
    content: @Composable (TaggoWindowSizeClass) -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val windowSizeClass = taggoWindowSizeClassForWidth(maxWidth)
        CompositionLocalProvider(LocalTaggoWindowSizeClass provides windowSizeClass) {
            Column(modifier = Modifier.fillMaxSize()) {
                topBar?.invoke(windowSizeClass)
                Box(modifier = Modifier.weight(1f)) {
                    when {
                        windowSizeClass == TaggoWindowSizeClass.Expanded -> {
                            Row(modifier = Modifier.fillMaxSize()) {
                                sideBar?.invoke(windowSizeClass) ?: TaggoSidebarNavigation(
                                    items = navigationItems,
                                    selectedPage = selectedNavigationPage,
                                    onPageSelected = onPageSelected,
                                    footer = sideBarFooter?.let { { it(windowSizeClass) } },
                                )
                                Box(modifier = Modifier.weight(1f)) {
                                    content(windowSizeClass)
                                }
                            }
                        }

                        windowSizeClass == TaggoWindowSizeClass.Medium -> {
                            Row(modifier = Modifier.fillMaxSize()) {
                                sideBar?.invoke(windowSizeClass) ?: TaggoNavigationRail(
                                    items = navigationItems,
                                    selectedPage = selectedNavigationPage,
                                    onPageSelected = onPageSelected,
                                )
                                Box(modifier = Modifier.weight(1f)) {
                                    content(windowSizeClass)
                                }
                            }
                        }

                        else -> {
                            content(windowSizeClass)
                        }
                    }
                }
                if (windowSizeClass == TaggoWindowSizeClass.Compact) {
                    bottomBar?.invoke(windowSizeClass) ?: TaggoBottomNavigation(
                        items = navigationItems,
                        selectedPage = selectedNavigationPage,
                        onPageSelected = onPageSelected,
                    )
                }
            }
        }
    }

}

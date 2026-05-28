package com.example.cross_platformfilemanager.ui.adaptive

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal enum class TaggoWindowSizeClass {
    Compact,
    Medium,
    Expanded,
}

internal val LocalTaggoWindowSizeClass = staticCompositionLocalOf {
    TaggoWindowSizeClass.Expanded
}

internal fun taggoWindowSizeClassForWidth(maxWidth: Dp): TaggoWindowSizeClass =
    when {
        maxWidth < 600.dp -> TaggoWindowSizeClass.Compact
        maxWidth < 840.dp -> TaggoWindowSizeClass.Medium
        else -> TaggoWindowSizeClass.Expanded
    }

package com.example.cross_platformfilemanager.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class TaggoSpacing(
    val xs: Dp,
    val sm: Dp,
    val md: Dp,
    val lg: Dp,
    val xl: Dp,
)

internal fun defaultTaggoSpacing(): TaggoSpacing =
    TaggoSpacing(
        xs = 4.dp,
        sm = 8.dp,
        md = 16.dp,
        lg = 20.dp,
        xl = 32.dp,
    )

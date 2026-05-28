package com.example.cross_platformfilemanager.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class TaggoElevation(
    val none: Dp,
    val low: Dp,
    val medium: Dp,
    val high: Dp,
)

internal fun defaultTaggoElevation(): TaggoElevation =
    TaggoElevation(
        none = 0.dp,
        low = 1.dp,
        medium = 2.dp,
        high = 10.dp,
    )

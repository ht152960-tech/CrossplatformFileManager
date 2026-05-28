package com.example.cross_platformfilemanager.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.dp

@Immutable
data class TaggoShapes(
    val small: RoundedCornerShape,
    val medium: RoundedCornerShape,
    val card: RoundedCornerShape,
    val panel: RoundedCornerShape,
    val pill: RoundedCornerShape,
)

internal fun defaultTaggoShapes(): TaggoShapes =
    TaggoShapes(
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(16.dp),
        card = RoundedCornerShape(20.dp),
        panel = RoundedCornerShape(24.dp),
        pill = RoundedCornerShape(999.dp),
    )

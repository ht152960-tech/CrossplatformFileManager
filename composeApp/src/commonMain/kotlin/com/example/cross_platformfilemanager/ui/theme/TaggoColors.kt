package com.example.cross_platformfilemanager.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Immutable
data class TaggoColors(
    val appBackground: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceVariant: Color,
    val surfaceSelected: Color,
    val border: Color,
    val borderStrong: Color,
    val primaryAccent: Color,
    val primaryAccentSoft: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val danger: Color,
    val success: Color,
    val warning: Color,
    val backgroundBrush: Brush,
)

internal fun defaultTaggoColors(): TaggoColors =
    TaggoColors(
        appBackground = Color(0xFF0F1117),
        surface = Color(0xFF181B22),
        surfaceElevated = Color(0xFF20242D),
        surfaceVariant = Color(0xFF252A34),
        surfaceSelected = Color(0xFF2A2342),
        border = Color(0xFF2A2E37),
        borderStrong = Color(0xFF3B4252),
        primaryAccent = Color(0xFF7C5CFF),
        primaryAccentSoft = Color(0xFF2B235A),
        textPrimary = Color(0xFFF2F4F8),
        textSecondary = Color(0xFFB2BBCB),
        textMuted = Color(0xFF8690A0),
        danger = Color(0xFFFF5C7A),
        success = Color(0xFF43D17A),
        warning = Color(0xFFF5B84B),
        backgroundBrush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF11141B),
                Color(0xFF0F1117),
                Color(0xFF0C0E13),
            ),
        ),
    )

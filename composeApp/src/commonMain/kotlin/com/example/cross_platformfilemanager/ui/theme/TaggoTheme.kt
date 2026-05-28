package com.example.cross_platformfilemanager.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily

private val LocalTaggoColors = staticCompositionLocalOf { defaultTaggoColors() }
private val LocalTaggoSpacing = staticCompositionLocalOf { defaultTaggoSpacing() }
private val LocalTaggoShapes = staticCompositionLocalOf { defaultTaggoShapes() }
private val LocalTaggoElevation = staticCompositionLocalOf { defaultTaggoElevation() }

@Immutable
data class TaggoTypography(
    val material: androidx.compose.material3.Typography,
)

private val LocalTaggoTypography = staticCompositionLocalOf {
    TaggoTypography(material = androidx.compose.material3.Typography())
}

@Composable
fun ProvideTaggoTheme(
    uiFontFamily: FontFamily,
    content: @Composable () -> Unit,
) {
    val colors = defaultTaggoColors()
    val typography = taggoTypography(uiFontFamily)

    CompositionLocalProvider(
        LocalTaggoColors provides colors,
        LocalTaggoSpacing provides defaultTaggoSpacing(),
        LocalTaggoShapes provides defaultTaggoShapes(),
        LocalTaggoElevation provides defaultTaggoElevation(),
        LocalTaggoTypography provides TaggoTypography(material = typography),
    ) {
        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = colors.primaryAccent,
                onPrimary = colors.textPrimary,
                primaryContainer = colors.primaryAccentSoft,
                onPrimaryContainer = colors.textPrimary,
                secondary = colors.surfaceSelected,
                onSecondary = colors.textPrimary,
                secondaryContainer = colors.surfaceVariant,
                onSecondaryContainer = colors.textPrimary,
                tertiary = colors.success,
                onTertiary = colors.appBackground,
                tertiaryContainer = colors.surfaceVariant,
                onTertiaryContainer = colors.textPrimary,
                error = colors.danger,
                onError = colors.textPrimary,
                errorContainer = colors.surfaceVariant,
                onErrorContainer = colors.danger,
                background = colors.appBackground,
                onBackground = colors.textPrimary,
                surface = colors.surface,
                onSurface = colors.textPrimary,
                surfaceVariant = colors.surfaceVariant,
                onSurfaceVariant = colors.textSecondary,
                surfaceTint = colors.primaryAccent,
                outline = colors.border,
                outlineVariant = colors.borderStrong,
                inverseSurface = colors.textPrimary,
                inverseOnSurface = colors.appBackground,
                inversePrimary = colors.primaryAccent,
            ),
            typography = typography,
            content = content,
        )
    }
}

object TaggoTheme {
    val colors: TaggoColors
        @Composable get() = LocalTaggoColors.current

    val spacing: TaggoSpacing
        @Composable get() = LocalTaggoSpacing.current

    val shapes: TaggoShapes
        @Composable get() = LocalTaggoShapes.current

    val elevation: TaggoElevation
        @Composable get() = LocalTaggoElevation.current

    val typography: TaggoTypography
        @Composable get() = LocalTaggoTypography.current

    val textStyle: TextStyle
        @Composable get() = MaterialTheme.typography.bodyMedium
}

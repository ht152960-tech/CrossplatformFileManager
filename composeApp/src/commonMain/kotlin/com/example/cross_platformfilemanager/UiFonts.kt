@file:OptIn(
    org.jetbrains.compose.resources.InternalResourceApi::class,
)

package com.example.cross_platformfilemanager

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import org.jetbrains.compose.resources.Font
import taggo.composeapp.generated.resources.NotoSansSc
import taggo.composeapp.generated.resources.NotoSansScUi

@Composable
fun rememberAppFontFamily(): FontFamily =
    if (supportsBundledUiFont()) {
        FontFamily(Font(NotoSansScUi))
    } else {
        FontFamily.Default
    }

@Composable
fun rememberFullCjkFontFamily(): FontFamily = FontFamily(Font(NotoSansSc))

/**
 * 基于给定字体族生成整套 Material Typography。
 *
 * 这样界面层只需要决定当前使用哪套字体，
 * 不需要逐个文本样式单独设置字体族。
 */
fun appTypography(fontFamily: FontFamily): Typography {
    val defaults = Typography()

    return Typography(
        displayLarge = defaults.displayLarge.withFontFamily(fontFamily),
        displayMedium = defaults.displayMedium.withFontFamily(fontFamily),
        displaySmall = defaults.displaySmall.withFontFamily(fontFamily),
        headlineLarge = defaults.headlineLarge.withFontFamily(fontFamily),
        headlineMedium = defaults.headlineMedium.withFontFamily(fontFamily),
        headlineSmall = defaults.headlineSmall.withFontFamily(fontFamily),
        titleLarge = defaults.titleLarge.withFontFamily(fontFamily),
        titleMedium = defaults.titleMedium.withFontFamily(fontFamily),
        titleSmall = defaults.titleSmall.withFontFamily(fontFamily),
        bodyLarge = defaults.bodyLarge.withFontFamily(fontFamily),
        bodyMedium = defaults.bodyMedium.withFontFamily(fontFamily),
        bodySmall = defaults.bodySmall.withFontFamily(fontFamily),
        labelLarge = defaults.labelLarge.withFontFamily(fontFamily),
        labelMedium = defaults.labelMedium.withFontFamily(fontFamily),
        labelSmall = defaults.labelSmall.withFontFamily(fontFamily),
    )
}

private fun TextStyle.withFontFamily(fontFamily: FontFamily): TextStyle =
    if (this.fontFamily == fontFamily) this else copy(fontFamily = fontFamily)

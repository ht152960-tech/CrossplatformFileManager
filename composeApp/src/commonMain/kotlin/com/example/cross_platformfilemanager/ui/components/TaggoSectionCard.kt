package com.example.cross_platformfilemanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.example.cross_platformfilemanager.ui.theme.TaggoGlobalColors
import com.example.cross_platformfilemanager.ui.theme.TaggoGlobalRadius
import com.example.cross_platformfilemanager.ui.theme.TaggoGlobalSpacing
import com.example.cross_platformfilemanager.ui.theme.TaggoGlobalTypography
import com.example.cross_platformfilemanager.ui.theme.TaggoCompactTokens
import com.example.cross_platformfilemanager.ui.theme.TaggoThemeTokens.HomeWide

/**
 * 首页区块的基础视觉容器。
 *
 * 默认保持宽屏样式；compact 变体只使用全局 token。
 * 这个组件只负责外壳与标题区域，不承载具体业务逻辑。
 */
@Composable
internal fun TaggoSectionCard(
    title: String,
    meta: String? = null,
    modifier: Modifier = Modifier,
    prominent: Boolean = false,
    topEntryPanel: Boolean = false,
    contentBackground: Boolean = true,
    compact: Boolean = false,
    compactPadding: Dp = TaggoGlobalSpacing.Lg,
    compactContentGap: Dp = TaggoGlobalSpacing.Md,
    compactTitleFontSize: TextUnit = TaggoGlobalTypography.TitleMedium,
    trailing: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    if (compact) {
        val compactShape = RoundedCornerShape(TaggoGlobalRadius.Card)
        Card(
            colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
            shape = compactShape,
            modifier = modifier
                .fillMaxWidth()
                .background(TaggoCompactTokens.glassCardBackgroundBrush(), compactShape)
                .border(1.dp, TaggoCompactTokens.GlassCardBorder, compactShape),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            TaggoSectionCardContent(
                title = title,
                meta = meta,
                prominent = false,
                compact = true,
                compactPadding = compactPadding,
                compactContentGap = compactContentGap,
                compactTitleFontSize = compactTitleFontSize,
                modifier = Modifier.background(TaggoCompactTokens.GlassCardSubtleHighlight.copy(alpha = 0.18f)),
                trailing = trailing,
                footer = footer,
                content = content,
            )
        }
        return
    }

    val shape = RoundedCornerShape(if (prominent) HomeWide.Radius.DashboardPanelProminent else HomeWide.Radius.DashboardPanelCompact)
    if (topEntryPanel) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(HomeWide.Colors.DashboardPanelBackground)
                .border(1.dp, HomeWide.Colors.DashboardPanelBorder, shape),
        ) {
            TaggoSectionCardContent(
                title = title,
                meta = meta,
                prominent = false,
                compact = false,
                compactPadding = TaggoGlobalSpacing.Lg,
                compactContentGap = TaggoGlobalSpacing.Md,
                compactTitleFontSize = TaggoGlobalTypography.TitleMedium,
                trailing = trailing,
                footer = footer,
                content = content,
            )
        }
        return
    }

    val containerColor = HomeWide.dashboardPanelContainerColor(prominent)
    val borderColor = HomeWide.dashboardPanelBorderColor(prominent)
    val surfaceBrush = HomeWide.dashboardPanelSurfaceBrush(prominent)
    val contentModifier = if (contentBackground && !topEntryPanel) {
        Modifier.background(surfaceBrush)
    } else {
        Modifier
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = shape,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, shape),
        elevation = CardDefaults.cardElevation(defaultElevation = if (prominent) 2.dp else 1.dp),
    ) {
        TaggoSectionCardContent(
            title = title,
            meta = meta,
            prominent = prominent,
            compact = false,
            compactPadding = TaggoGlobalSpacing.Lg,
            compactContentGap = TaggoGlobalSpacing.Md,
            compactTitleFontSize = TaggoGlobalTypography.TitleMedium,
            modifier = contentModifier,
            trailing = trailing,
            footer = footer,
            content = content,
        )
    }
}

@Composable
private fun TaggoSectionCardContent(
    title: String,
    meta: String?,
    prominent: Boolean,
    compact: Boolean,
    compactPadding: Dp,
    compactContentGap: Dp,
    compactTitleFontSize: TextUnit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)?,
    footer: (@Composable () -> Unit)?,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = (if (compact) Modifier.fillMaxWidth() else Modifier.fillMaxSize())
            .then(modifier)
            .padding(
                horizontal = when {
                    compact -> compactPadding
                    prominent -> HomeWide.Spacing.PanelPaddingProminent
                    else -> HomeWide.Spacing.PanelPaddingRegular
                },
                vertical = when {
                    compact -> compactPadding
                    prominent -> HomeWide.Spacing.PanelPaddingProminent
                    else -> HomeWide.Spacing.PanelPaddingRegular
                },
            ),
        verticalArrangement = Arrangement.spacedBy(
            when {
                compact -> compactContentGap
                prominent -> HomeWide.Spacing.PanelContentGapProminent
                else -> HomeWide.Spacing.PanelContentGapRegular
            },
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                color = if (compact) TaggoGlobalColors.TextPrimary else HomeWide.Colors.TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = when {
                    compact -> compactTitleFontSize
                    prominent -> HomeWide.Typography.PanelTitleProminent
                    else -> HomeWide.Typography.PanelTitle
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (trailing != null) {
                trailing()
            } else if (meta != null && meta.isNotBlank()) {
                Text(
                    text = meta,
                    color = if (compact) TaggoGlobalColors.TextSecondary else HomeWide.Colors.TextSecondary.copy(alpha = HomeWide.Alpha.DashboardPanelMeta),
                    fontSize = if (compact) TaggoGlobalTypography.Caption else HomeWide.Typography.PanelMeta,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (trailing != null && meta != null && meta.isNotBlank()) {
            Text(
                text = meta,
                color = if (compact) TaggoGlobalColors.TextSecondary else HomeWide.Colors.TextSecondary.copy(alpha = HomeWide.Alpha.DashboardPanelMetaSecondary),
                fontSize = if (compact) TaggoGlobalTypography.Caption else HomeWide.Typography.PanelMeta,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box(modifier = if (compact) Modifier.fillMaxWidth() else Modifier.weight(1f, fill = true)) {
            content()
        }
        if (footer != null) {
            footer()
        }
    }
}

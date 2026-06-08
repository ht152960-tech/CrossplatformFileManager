package com.example.cross_platformfilemanager.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.cross_platformfilemanager.ui.theme.TaggoGlobalAlpha
import com.example.cross_platformfilemanager.ui.theme.TaggoGlobalColors
import com.example.cross_platformfilemanager.ui.theme.TaggoGlobalRadius
import com.example.cross_platformfilemanager.ui.theme.TaggoGlobalSpacing
import com.example.cross_platformfilemanager.ui.theme.TaggoGlobalTypography
import com.example.cross_platformfilemanager.ui.theme.TaggoThemeTokens.HomeWide

/**
 * 首页阶段抽出的基础按钮组件。
 *
 * 默认保持宽屏样式；compact 变体只使用全局 token。
 * 不承载打开文件、展开菜单、进入详情等业务逻辑。
 */

@Composable
internal fun TaggoOpenButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    compact: Boolean = false,
) {
    val containerColor = if (compact) {
        TaggoGlobalColors.PrimaryAccent.copy(alpha = TaggoGlobalAlpha.Strong)
    } else {
        HomeWide.toolButtonContainerColor(primary = true)
    }
    Button(
        modifier = modifier.height(if (compact) 34.dp else HomeWide.Size.ToolButtonHeight),
        shape = RoundedCornerShape(if (compact) TaggoGlobalRadius.Button else HomeWide.Radius.ToolButton),
        contentPadding = PaddingValues(
            horizontal = if (compact) TaggoGlobalSpacing.Md else HomeWide.Spacing.ToolButtonPaddingX,
            vertical = 0.dp,
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = if (compact) TaggoGlobalColors.TextPrimary else HomeWide.Colors.TextPrimary,
            disabledContainerColor = if (compact) {
                TaggoGlobalColors.PanelBackgroundSoft.copy(alpha = TaggoGlobalAlpha.Disabled)
            } else {
                HomeWide.Colors.PanelBackgroundSoft.copy(alpha = HomeWide.Alpha.ButtonDisabled)
            },
            disabledContentColor = if (compact) TaggoGlobalColors.TextMuted else HomeWide.Colors.TextMuted,
        ),
        onClick = onClick,
        enabled = enabled,
    ) {
        Text(
            text = label,
            fontSize = if (compact) TaggoGlobalTypography.Button else HomeWide.Typography.ToolButton,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun TaggoMoreButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    compact: Boolean = false,
) {
    // TODO: 当前只承接首页宽屏的“查看全部 / View all”入口。
    // 如果后续出现真正的“更多操作 ⋯”菜单按钮，应拆分为 TaggoSectionFooterLink / TaggoViewAllButton 和 TaggoMoreActionButton，
    // 避免“查看全部”和“更多菜单”混用。
    Box(
        modifier = modifier
            .then(if (compact) Modifier.wrapContentWidth() else Modifier.fillMaxWidth())
            .height(if (compact) 28.dp else HomeWide.Size.LinkButtonHeight),
        contentAlignment = if (compact) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Text(
            text = label,
            color = if (compact) TaggoGlobalColors.PrimaryAccent else HomeWide.Colors.DashboardAccent,
            fontSize = if (compact) TaggoGlobalTypography.BodySmall else HomeWide.Typography.Link,
            fontWeight = if (compact) FontWeight.Medium else FontWeight.SemiBold,
            modifier = Modifier
                .clickable(enabled = enabled, onClick = onClick)
                .padding(
                    horizontal = if (compact) TaggoGlobalSpacing.Xs else 0.dp,
                    vertical = if (compact) TaggoGlobalSpacing.Xs else HomeWide.Spacing.LinkButtonPaddingY,
                ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun TaggoIconActionButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(48.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
        )
    }
}

package com.example.cross_platformfilemanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.cross_platformfilemanager.ui.theme.TaggoThemeTokens.HomeWide

/**
 * TaggoListItemSurface 是基础条目视觉外壳。
 *
 * 它只负责背景、边框、圆角、padding、高度、Row 对齐和可选点击态。
 * 它不承载文件、标签、推荐、类型统计等业务语义。
 * content slot 承载具体业务内容。
 * 当前参数偏底层，是为了保持首页宽屏视觉不变；未来跨页面复用前再评估 variant / density。
 * 不允许在 TaggoListItemSurface 内加入 FileItem / TagItem / FileTypeItem / RecommendedItem 等业务逻辑。
 */
@Composable
internal fun TaggoListItemSurface(
    modifier: Modifier = Modifier,
    shape: Shape,
    backgroundColor: Color,
    borderColor: Color,
    backgroundBrush: Brush? = null,
    height: Dp? = null,
    borderWidth: Dp = 1.dp,
    contentPadding: PaddingValues,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(HomeWide.Spacing.TagGridGap),
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val baseModifier = modifier
        .fillMaxWidth()
        .then(if (height != null) Modifier.height(height) else Modifier)
        .clip(shape)
        .then(
            if (backgroundBrush != null) {
                Modifier.background(backgroundBrush)
            } else {
                Modifier.background(backgroundColor)
            },
        )
        .border(borderWidth, borderColor, shape)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(contentPadding)

    Row(
        modifier = baseModifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
    ) {
        content()
    }
}

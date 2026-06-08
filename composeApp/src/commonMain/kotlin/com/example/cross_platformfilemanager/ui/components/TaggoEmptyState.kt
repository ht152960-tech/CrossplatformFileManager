package com.example.cross_platformfilemanager.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cross_platformfilemanager.ui.theme.TaggoGlobalAlpha
import com.example.cross_platformfilemanager.ui.theme.TaggoGlobalColors
import com.example.cross_platformfilemanager.ui.theme.TaggoGlobalSpacing
import com.example.cross_platformfilemanager.ui.theme.TaggoGlobalTypography

/**
 * 全项目通用的空状态基础组件。
 *
 * 它只负责空状态的视觉结构，不承载任何页面业务逻辑。
 */
@Composable
internal fun TaggoEmptyState(
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    primaryAction: (@Composable () -> Unit)? = null,
    secondaryAction: (@Composable () -> Unit)? = null,
    compact: Boolean = false,
) {
    val contentSpacing = if (compact) TaggoGlobalSpacing.Xs else TaggoGlobalSpacing.Sm
    val outerPadding = if (compact) {
        PaddingValues(horizontal = TaggoGlobalSpacing.Md, vertical = TaggoGlobalSpacing.Xs)
    } else {
        PaddingValues(horizontal = TaggoGlobalSpacing.Lg, vertical = TaggoGlobalSpacing.Sm)
    }
    val maxWidth = if (compact) 360.dp else 480.dp
    val descriptionColor = TaggoGlobalColors.TextSecondary.copy(alpha = if (compact) 0.72f else TaggoGlobalAlpha.Strong)

    Column(
        modifier = modifier
            .widthIn(max = maxWidth)
            .padding(outerPadding),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(contentSpacing),
    ) {
        if (icon != null) {
            icon()
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(contentSpacing),
        ) {
            androidx.compose.material3.Text(
                text = title,
                color = TaggoGlobalColors.TextPrimary,
                fontSize = if (compact) TaggoGlobalTypography.BodySmall else TaggoGlobalTypography.TitleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (!description.isNullOrBlank()) {
                androidx.compose.material3.Text(
                    text = description,
                    color = descriptionColor,
                    fontSize = if (compact) TaggoGlobalTypography.Caption else TaggoGlobalTypography.Body,
                    lineHeight = if (compact) 14.sp else 18.sp,
                    maxLines = if (compact) 2 else 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (primaryAction != null || secondaryAction != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(TaggoGlobalSpacing.Sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                secondaryAction?.invoke()
                primaryAction?.invoke()
            }
        }
    }
}

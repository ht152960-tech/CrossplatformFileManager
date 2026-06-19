package com.example.cross_platformfilemanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import com.example.cross_platformfilemanager.SearchTag
import com.example.cross_platformfilemanager.SearchTagSource
import com.example.cross_platformfilemanager.displayTextForUi
import com.example.cross_platformfilemanager.ui.adaptive.LocalTaggoWindowSizeClass
import com.example.cross_platformfilemanager.ui.adaptive.TaggoWindowSizeClass
import com.example.cross_platformfilemanager.ui.theme.TaggoTheme

@Composable
internal fun TagFilterChip(
    tag: String,
    selected: Boolean,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    onClick: () -> Unit,
) {
    TaggoOperableTagChip(
        kind = OperableTagChipKind.Filter,
        label = tag,
        formalTag = true,
        fullCjkFontReady = fullCjkFontReady,
        fullCjkFontFamily = fullCjkFontFamily,
        actionSymbol = "+",
        emphasized = selected,
        onClick = onClick,
        onActionClick = onClick,
    )
}

@Composable
internal fun SearchTagChip(
    tag: SearchTag,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    onRemove: () -> Unit,
) {
    TaggoOperableTagChip(
        kind = OperableTagChipKind.Search,
        label = tag.value,
        formalTag = tag.source == SearchTagSource.LibraryTag,
        fullCjkFontReady = fullCjkFontReady,
        fullCjkFontFamily = fullCjkFontFamily,
        actionSymbol = "−",
        onActionClick = onRemove,
    )
}

@Composable
internal fun TagPill(
    tag: String,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    modifier: Modifier = Modifier,
) {
    val windowSizeClass = LocalTaggoWindowSizeClass.current
    val maxChipWidth = if (windowSizeClass == TaggoWindowSizeClass.Compact) 140.dp else 176.dp
    Surface(
        color = TaggoTheme.colors.panelBackgroundSoft,
        contentColor = TaggoTheme.colors.textSecondary,
        shape = RoundedCornerShape(999.dp),
        modifier = modifier.widthIn(max = maxChipWidth),
    ) {
        Text(
            text = displayFormalTagForUi(tag, fullCjkFontReady),
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            fontWeight = FontWeight.Medium,
            fontSize = if (windowSizeClass == TaggoWindowSizeClass.Compact) 12.sp else 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontFamily = fullCjkFontFamily,
        )
    }
}

@Composable
internal fun RemovableTagChip(
    tag: String,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    onClick: (() -> Unit)? = null,
    onRemove: () -> Unit,
    actionIcon: ImageVector = Icons.Outlined.Remove,
) {
    TaggoOperableTagChip(
        kind = OperableTagChipKind.Removable,
        label = tag,
        formalTag = true,
        fullCjkFontReady = fullCjkFontReady,
        fullCjkFontFamily = fullCjkFontFamily,
        actionSymbol = if (actionIcon == Icons.Outlined.Close) "×" else "−",
        onClick = onClick,
        onActionClick = onRemove,
    )
}

@Composable
internal fun AddableTagChip(
    tag: String,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    onAdd: () -> Unit,
) {
    TaggoOperableTagChip(
        kind = OperableTagChipKind.Addable,
        label = tag,
        formalTag = true,
        fullCjkFontReady = fullCjkFontReady,
        fullCjkFontFamily = fullCjkFontFamily,
        actionSymbol = "+",
        onClick = onAdd,
        onActionClick = onAdd,
    )
}

@Composable
internal fun SortChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val windowSizeClass = LocalTaggoWindowSizeClass.current
    val maxChipWidth = if (windowSizeClass == TaggoWindowSizeClass.Compact) 168.dp else 200.dp
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = Modifier.widthIn(max = maxChipWidth),
        label = {
            Text(
                text = label,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
    )
}

private fun displayFormalTagForUi(
    tag: String,
    fullCjkFontReady: Boolean,
): String {
    val displayText = displayTextForUi(tag, fullCjkFontReady)
    return if (displayText.startsWith("#")) displayText else "#$displayText"
}

private data class OperableTagChipMetrics(
    val height: Dp,
    val minWidth: Dp,
    val maxWidth: Dp,
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val textSize: TextUnit,
    val lineHeight: TextUnit,
    val cornerRadius: Dp,
    val actionTouchSize: Dp,
    val actionVisualSize: Dp,
    val actionSymbolSize: TextUnit,
)

private enum class OperableTagChipKind {
    Filter,
    Search,
    Removable,
    Addable,
}

private fun operableTagChipMetrics(
    kind: OperableTagChipKind,
    windowSizeClass: TaggoWindowSizeClass,
): OperableTagChipMetrics {
    val maxWidth = when (kind) {
        OperableTagChipKind.Removable -> if (windowSizeClass == TaggoWindowSizeClass.Compact) {
            190.dp
        } else {
            212.dp
        }
        OperableTagChipKind.Filter,
        OperableTagChipKind.Search,
        OperableTagChipKind.Addable -> if (windowSizeClass == TaggoWindowSizeClass.Compact) {
            190.dp
        } else {
            220.dp
        }
    }
    return when (windowSizeClass) {
        TaggoWindowSizeClass.Compact -> OperableTagChipMetrics(
            height = 36.dp,
            minWidth = 58.dp,
            maxWidth = maxWidth,
            horizontalPadding = 14.dp,
            verticalPadding = 7.dp,
            textSize = 13.sp,
            lineHeight = 17.sp,
            cornerRadius = 12.dp,
            actionTouchSize = 40.dp,
            actionVisualSize = 22.dp,
            actionSymbolSize = 13.sp,
        )
        TaggoWindowSizeClass.Medium -> OperableTagChipMetrics(
            height = 34.dp,
            minWidth = 54.dp,
            maxWidth = maxWidth,
            horizontalPadding = 14.dp,
            verticalPadding = 6.dp,
            textSize = 12.5.sp,
            lineHeight = 16.sp,
            cornerRadius = 11.dp,
            actionTouchSize = 40.dp,
            actionVisualSize = 21.dp,
            actionSymbolSize = 12.5.sp,
        )
        TaggoWindowSizeClass.Expanded -> OperableTagChipMetrics(
            height = 32.dp,
            minWidth = 50.dp,
            maxWidth = maxWidth,
            horizontalPadding = 14.dp,
            verticalPadding = 6.dp,
            textSize = 12.sp,
            lineHeight = 15.sp,
            cornerRadius = 10.dp,
            actionTouchSize = 40.dp,
            actionVisualSize = 20.dp,
            actionSymbolSize = 12.sp,
        )
    }
}

@Composable
private fun TaggoOperableTagChip(
    kind: OperableTagChipKind,
    label: String,
    formalTag: Boolean,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    actionSymbol: String,
    emphasized: Boolean = false,
    onClick: (() -> Unit)? = null,
    onActionClick: () -> Unit,
) {
    val windowSizeClass = LocalTaggoWindowSizeClass.current
    val metrics = operableTagChipMetrics(kind, windowSizeClass)
    val chipShape = RoundedCornerShape(metrics.cornerRadius)
    val visualActionShape = RoundedCornerShape(999.dp)
    val actionOuterPadding = metrics.actionVisualSize / 2
    val actionTouchOffset = (metrics.actionTouchSize - metrics.actionVisualSize) / 2
    val displayText = if (formalTag) {
        displayFormalTagForUi(label, fullCjkFontReady)
    } else {
        displayTextForUi(label, fullCjkFontReady)
    }
    Box(
        modifier = Modifier.widthIn(max = metrics.maxWidth + actionOuterPadding),
    ) {
        Surface(
            color = if (emphasized) {
                TaggoTheme.colors.primaryAccentSoft
            } else {
                TaggoTheme.colors.surfaceElevated
            },
            contentColor = TaggoTheme.colors.textPrimary,
            shape = chipShape,
            modifier = Modifier
                .padding(top = actionOuterPadding, end = actionOuterPadding)
                .height(metrics.height)
                .widthIn(min = metrics.minWidth, max = metrics.maxWidth)
                .border(1.dp, TaggoTheme.colors.panelBorder.copy(alpha = 0.95f), chipShape)
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        ) {
            Box(contentAlignment = Alignment.CenterStart) {
                Text(
                    text = displayText,
                    modifier = Modifier.padding(
                        start = metrics.horizontalPadding,
                        end = metrics.horizontalPadding,
                        top = metrics.verticalPadding,
                        bottom = metrics.verticalPadding,
                    ),
                    fontWeight = FontWeight.Medium,
                    fontSize = metrics.textSize,
                    lineHeight = metrics.lineHeight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = fullCjkFontFamily,
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = actionTouchOffset, y = -actionTouchOffset)
                .size(metrics.actionTouchSize)
                .clickable(onClick = onActionClick),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(metrics.actionVisualSize)
                    .clip(visualActionShape)
                    .background(TaggoTheme.colors.surfaceVariant)
                    .border(1.dp, TaggoTheme.colors.panelBorder.copy(alpha = 0.95f), visualActionShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = actionSymbol,
                    color = TaggoTheme.colors.textSecondary,
                    fontSize = metrics.actionSymbolSize,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = metrics.actionSymbolSize,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

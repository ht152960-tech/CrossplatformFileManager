package com.example.cross_platformfilemanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import com.example.cross_platformfilemanager.SearchTag
import com.example.cross_platformfilemanager.SearchTagSource
import com.example.cross_platformfilemanager.displayTextForUi
import com.example.cross_platformfilemanager.ui.adaptive.LocalTaggoWindowSizeClass
import com.example.cross_platformfilemanager.ui.adaptive.TaggoWindowSizeClass
import kotlin.math.roundToInt

@Composable
internal fun TaggoSearchConditionChip(
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
internal fun TaggoFileTagChip(
    tag: String,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    onClick: (() -> Unit)? = null,
    onRemove: () -> Unit,
) {
    TaggoOperableTagChip(
        kind = OperableTagChipKind.File,
        label = tag,
        formalTag = true,
        fullCjkFontReady = fullCjkFontReady,
        fullCjkFontFamily = fullCjkFontFamily,
        actionSymbol = "−",
        onClick = onClick,
        onActionClick = onRemove,
    )
}

@Composable
internal fun TaggoTagDeleteChip(
    tag: String,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    onClick: (() -> Unit)? = null,
    onDelete: () -> Unit,
) {
    TaggoOperableTagChip(
        kind = OperableTagChipKind.Delete,
        label = tag,
        formalTag = true,
        fullCjkFontReady = fullCjkFontReady,
        fullCjkFontFamily = fullCjkFontFamily,
        actionSymbol = "×",
        onClick = onClick,
        onActionClick = onDelete,
    )
}

@Composable
internal fun TaggoAddTagCandidateChip(
    tag: String,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    onAdd: () -> Unit,
) {
    TaggoOperableTagChip(
        kind = OperableTagChipKind.Add,
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

internal data class OperableTagChipSpacing(
    val horizontal: Dp,
    val vertical: Dp,
)

enum class OperableTagChipKind {
    Search,
    File,
    Delete,
    Add,
}

private object OperableTagChipTokens {
    data class Metrics(
        val bodyHeight: Dp,
        val bodyMinWidth: Dp,
        val bodyMaxWidth: Dp,
        val bodyHorizontalPadding: Dp,
        val bodyCornerRadius: Dp,
        val actionTouchSize: Dp,
        val actionVisualSize: Dp,
        val actionSymbolSize: TextUnit,
        val horizontalSpacing: Dp,
        val verticalSpacing: Dp,
    )

    data class Colors(
        val bodyBackground: Color,
        val bodyBorder: Color,
        val textColor: Color,
        val actionDotBackground: Color,
        val actionDotBorder: Color,
        val actionSymbolColor: Color,
    )

    data class Body(
        val background: Color,
        val border: Color,
        val textColor: Color,
        val cornerRadius: Dp,
        val borderWidth: Dp,
    ) {
        fun shape(): RoundedCornerShape = RoundedCornerShape(cornerRadius)
    }

    data class Text(
        val fontSize: TextUnit,
        val lineHeight: TextUnit,
        val fontWeight: FontWeight,
        val maxLines: Int = 1,
        val overflow: TextOverflow = TextOverflow.Ellipsis,
    )

    data class ActionDot(
        val background: Color,
        val border: Color,
        val symbolColor: Color,
        val touchSize: Dp,
        val visualSize: Dp,
        val borderWidth: Dp,
        val symbolSize: TextUnit,
    ) {
        fun shape(): RoundedCornerShape = RoundedCornerShape(999.dp)
    }

    data class Resolved(
        val metrics: Metrics,
        val colors: Colors,
        val body: Body,
        val text: Text,
        val actionDot: ActionDot,
    )

    fun resolve(
        kind: OperableTagChipKind,
        windowSizeClass: TaggoWindowSizeClass,
        emphasized: Boolean,
    ): Resolved {
        val maxWidth = when (kind) {
            OperableTagChipKind.Search,
            OperableTagChipKind.Add -> if (windowSizeClass == TaggoWindowSizeClass.Compact) 190.dp else 220.dp
            OperableTagChipKind.File,
            OperableTagChipKind.Delete -> if (windowSizeClass == TaggoWindowSizeClass.Compact) 190.dp else 212.dp
        }
        val metrics = when (windowSizeClass) {
            TaggoWindowSizeClass.Compact -> Metrics(
                bodyHeight = 32.dp,
                bodyMinWidth = 58.dp,
                bodyMaxWidth = maxWidth,
                bodyHorizontalPadding = 14.dp,
                bodyCornerRadius = 12.dp,
                actionTouchSize = 40.dp,
                actionVisualSize = 18.dp,
                actionSymbolSize = 10.sp,
                horizontalSpacing = 14.dp,
                verticalSpacing = 16.dp,
            )

            TaggoWindowSizeClass.Medium -> Metrics(
                bodyHeight = 31.dp,
                bodyMinWidth = 56.dp,
                bodyMaxWidth = maxWidth,
                bodyHorizontalPadding = 13.dp,
                bodyCornerRadius = 11.dp,
                actionTouchSize = 40.dp,
                actionVisualSize = 17.dp,
                actionSymbolSize = 9.5.sp,
                horizontalSpacing = 13.dp,
                verticalSpacing = 15.dp,
            )

            TaggoWindowSizeClass.Expanded -> Metrics(
                bodyHeight = 30.dp,
                bodyMinWidth = 54.dp,
                bodyMaxWidth = maxWidth,
                bodyHorizontalPadding = 12.dp,
                bodyCornerRadius = 10.dp,
                actionTouchSize = 40.dp,
                actionVisualSize = 16.dp,
                actionSymbolSize = 9.sp,
                horizontalSpacing = 12.dp,
                verticalSpacing = 14.dp,
            )
        }
        val colors = if (emphasized) {
            Colors(
                bodyBackground = Color(0x46242A43),
                bodyBorder = Color(0x4EA88CFF),
                textColor = Color(0xF4F6FAFF),
                actionDotBackground = Color(0xEA241F38),
                actionDotBorder = Color(0x78A88CFF),
                actionSymbolColor = Color(0xFFFFFFFF),
            )
        } else {
            Colors(
                bodyBackground = Color(0x36161B31),
                bodyBorder = Color(0x3AA9B8FF),
                textColor = Color(0xE6EEF2FF),
                actionDotBackground = Color(0xE0182030),
                actionDotBorder = Color(0x66A9B8FF),
                actionSymbolColor = Color(0xF0EEF2FF),
            )
        }
        return Resolved(
            metrics = metrics,
            colors = colors,
            body = Body(
                background = colors.bodyBackground,
                border = colors.bodyBorder,
                textColor = colors.textColor,
                cornerRadius = metrics.bodyCornerRadius,
                borderWidth = 1.dp,
            ),
            text = Text(
                fontSize = when (windowSizeClass) {
                    TaggoWindowSizeClass.Compact -> 13.sp
                    TaggoWindowSizeClass.Medium -> 12.5.sp
                    TaggoWindowSizeClass.Expanded -> 12.sp
                },
                lineHeight = when (windowSizeClass) {
                    TaggoWindowSizeClass.Compact -> 17.sp
                    TaggoWindowSizeClass.Medium -> 16.sp
                    TaggoWindowSizeClass.Expanded -> 15.sp
                },
                fontWeight = FontWeight.Medium,
            ),
            actionDot = ActionDot(
                background = colors.actionDotBackground,
                border = colors.actionDotBorder,
                symbolColor = colors.actionSymbolColor,
                touchSize = metrics.actionTouchSize,
                visualSize = metrics.actionVisualSize,
                borderWidth = 1.dp,
                symbolSize = metrics.actionSymbolSize,
            ),
        )
    }
}

internal fun operableTagChipSpacing(windowSizeClass: TaggoWindowSizeClass): OperableTagChipSpacing {
    val metrics = OperableTagChipTokens.resolve(OperableTagChipKind.File, windowSizeClass, false).metrics
    return OperableTagChipSpacing(
        horizontal = metrics.horizontalSpacing,
        vertical = metrics.verticalSpacing,
    )
}

@Composable
fun TaggoOperableTagChip(
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
    TaggoOperableTagChipV2(
        kind = kind,
        label = label,
        formalTag = formalTag,
        fullCjkFontReady = fullCjkFontReady,
        fullCjkFontFamily = fullCjkFontFamily,
        actionSymbol = actionSymbol,
        emphasized = emphasized,
        onClick = onClick,
        onActionClick = onActionClick,
    )
}

@Composable
private fun TaggoOperableTagChipV2(
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
    val density = LocalDensity.current
    val tokens = OperableTagChipTokens.resolve(kind, windowSizeClass, emphasized)
    val metrics = tokens.metrics
    val bodyShape = tokens.body.shape()
    val actionShape = tokens.actionDot.shape()
    val displayText = if (formalTag) {
        displayFormalTagForUi(label, fullCjkFontReady)
    } else {
        displayTextForUi(label, fullCjkFontReady)
    }
    Layout(
        content = {
            Box(
                modifier = Modifier
                    .widthIn(min = metrics.bodyMinWidth, max = metrics.bodyMaxWidth)
                    .height(metrics.bodyHeight),
            ) {
                Surface(
                    color = tokens.body.background,
                    contentColor = tokens.body.textColor,
                    shape = bodyShape,
                    modifier = Modifier
                        .widthIn(min = metrics.bodyMinWidth, max = metrics.bodyMaxWidth)
                        .height(metrics.bodyHeight)
                        .clip(bodyShape)
                        .border(tokens.body.borderWidth, tokens.body.border, bodyShape)
                        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = displayText,
                            modifier = Modifier.padding(
                                horizontal = metrics.bodyHorizontalPadding,
                            ),
                            color = tokens.body.textColor,
                            fontWeight = tokens.text.fontWeight,
                            fontSize = tokens.text.fontSize,
                            lineHeight = tokens.text.lineHeight,
                            maxLines = tokens.text.maxLines,
                            overflow = tokens.text.overflow,
                            fontFamily = fullCjkFontFamily,
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .size(tokens.actionDot.touchSize)
                    .clickable(onClick = onActionClick),
            )
            Box(
                modifier = Modifier
                    .size(tokens.actionDot.visualSize)
                    .clip(actionShape)
                    .background(tokens.actionDot.background)
                    .border(tokens.actionDot.borderWidth, tokens.actionDot.border, actionShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = actionSymbol,
                    color = tokens.actionDot.symbolColor,
                    fontSize = tokens.actionDot.symbolSize,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = tokens.actionDot.symbolSize,
                    textAlign = TextAlign.Center,
                    maxLines = tokens.text.maxLines,
                )
            }
        },
    ) { measurables, constraints ->
        val touchOffsetPx = with(density) { (metrics.actionTouchSize / 2).toPx().roundToInt() }
        val visualOffsetPx = with(density) { (metrics.actionVisualSize / 2).toPx().roundToInt() }
        val actionTouchSizePx = with(density) { metrics.actionTouchSize.toPx().roundToInt() }
        val actionVisualSizePx = with(density) { metrics.actionVisualSize.toPx().roundToInt() }
        val bodyHeightPx = with(density) { metrics.bodyHeight.toPx().roundToInt() }
        val bodyMinWidthPx = with(density) { metrics.bodyMinWidth.toPx().roundToInt() }
        val bodyMaxWidthPx = with(density) { metrics.bodyMaxWidth.toPx().roundToInt() }
        val bodyConstraints = constraints.copy(
            minWidth = bodyMinWidthPx.coerceAtMost(constraints.maxWidth),
            maxWidth = bodyMaxWidthPx.coerceAtMost(constraints.maxWidth),
            minHeight = bodyHeightPx,
            maxHeight = bodyHeightPx,
        )
        val bodyPlaceable = measurables[0].measure(bodyConstraints)
        val touchPlaceable = measurables[1].measure(
            Constraints.fixed(actionTouchSizePx, actionTouchSizePx),
        )
        val dotPlaceable = measurables[2].measure(
            Constraints.fixed(actionVisualSizePx, actionVisualSizePx),
        )
        val layoutWidth = bodyPlaceable.width + touchOffsetPx
        val layoutHeight = bodyPlaceable.height + touchOffsetPx
        layout(layoutWidth, layoutHeight) {
            bodyPlaceable.placeRelative(0, touchOffsetPx)
            touchPlaceable.placeRelative(bodyPlaceable.width - touchOffsetPx, 0)
            dotPlaceable.placeRelative(
                bodyPlaceable.width - visualOffsetPx,
                touchOffsetPx - visualOffsetPx,
            )
        }
    }
}

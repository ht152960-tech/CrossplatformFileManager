package com.example.cross_platformfilemanager.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cross_platformfilemanager.displayTextForUi
import com.example.cross_platformfilemanager.ui.adaptive.LocalTaggoWindowSizeClass
import com.example.cross_platformfilemanager.ui.adaptive.TaggoWindowSizeClass
import com.example.cross_platformfilemanager.ui.theme.TaggoGlobalColors
import com.example.cross_platformfilemanager.ui.theme.TaggoGlobalRadius
import com.example.cross_platformfilemanager.ui.theme.TaggoGlobalSpacing
import com.example.cross_platformfilemanager.ui.theme.TaggoGlobalTypography
import com.example.cross_platformfilemanager.ui.theme.TaggoThemeTokens.HomeWide

@Composable
internal fun TaggoTagChip(
    tag: String,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    largeCompact: Boolean = false,
    summaryCount: Int? = null,
) {
    val chipShape = RoundedCornerShape(
        when {
            compact && largeCompact -> 15.dp
            compact -> TaggoGlobalRadius.Badge
            else -> HomeWide.Radius.Badge
        },
    )
    val windowSizeClass = LocalTaggoWindowSizeClass.current
    Surface(
        color = if (compact) TaggoGlobalColors.SurfaceVariant else HomeWide.Colors.PanelBackgroundSoft,
        contentColor = if (compact) TaggoGlobalColors.TextSecondary else HomeWide.Colors.TextSecondary,
        shape = chipShape,
        modifier = modifier
            .widthIn(max = if (windowSizeClass == TaggoWindowSizeClass.Compact) 140.dp else 176.dp),
    ) {
        if (compact && largeCompact && summaryCount != null) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = displayTextForUi(tag, fullCjkFontReady),
                    color = TaggoGlobalColors.TextPrimary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = fullCjkFontFamily,
                )
                Text(
                    text = summaryCount.toString(),
                    color = TaggoGlobalColors.PrimaryAccent,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    maxLines = 1,
                    fontFamily = fullCjkFontFamily,
                )
            }
        } else if (compact && largeCompact) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = displayTextForUi(tag, fullCjkFontReady),
                    modifier = Modifier.padding(horizontal = TaggoGlobalSpacing.Md),
                    fontWeight = FontWeight.Medium,
                    fontSize = TaggoGlobalTypography.Body,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = fullCjkFontFamily,
                )
            }
        } else {
            Text(
                text = displayTextForUi(tag, fullCjkFontReady),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                fontWeight = FontWeight.Medium,
                fontSize = if (compact) TaggoGlobalTypography.Caption else 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontFamily = fullCjkFontFamily,
            )
        }
    }
}

@Composable
internal fun TaggoTagRow(
    tags: List<String>,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    modifier: Modifier = Modifier,
) {
    val windowSizeClass = LocalTaggoWindowSizeClass.current
    val visibleTags = resolveVisibleCardTags(tags, windowSizeClass)
    val remainingTagCount = tags.size - visibleTags.size
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        visibleTags.forEach { tag ->
            TaggoTagChip(
                tag = tag,
                fullCjkFontReady = fullCjkFontReady,
                fullCjkFontFamily = fullCjkFontFamily,
            )
        }
        if (remainingTagCount > 0) {
            Text(
                text = "+$remainingTagCount",
                color = HomeWide.Colors.TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun resolveVisibleCardTags(
    tags: List<String>,
    windowSizeClass: TaggoWindowSizeClass,
): List<String> {
    if (tags.isEmpty()) return emptyList()
    val maxVisibleCount = when (windowSizeClass) {
        TaggoWindowSizeClass.Expanded -> 3
        TaggoWindowSizeClass.Medium -> 3
        TaggoWindowSizeClass.Compact -> 3
    }
    val characterBudget = when (windowSizeClass) {
        TaggoWindowSizeClass.Expanded -> 30
        TaggoWindowSizeClass.Medium -> 22
        TaggoWindowSizeClass.Compact -> 20
    }
    val visible = mutableListOf<String>()
    var usedCharacters = 0
    for (tag in tags) {
        if (visible.size >= maxVisibleCount) break
        val normalizedLength = tag.trim().length.coerceAtMost(10)
        val nextCost = if (visible.isEmpty()) normalizedLength else normalizedLength + 2
        val remainingAfterThis = tags.size - (visible.size + 1)
        val overflowReserve = if (remainingAfterThis > 0) 6 else 0
        if (visible.isNotEmpty() && usedCharacters + nextCost + overflowReserve > characterBudget) break
        visible += tag
        usedCharacters += nextCost
    }
    return if (visible.isEmpty()) listOf(tags.first()) else visible
}

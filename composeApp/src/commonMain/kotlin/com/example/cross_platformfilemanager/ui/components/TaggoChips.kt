package com.example.cross_platformfilemanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val windowSizeClass = LocalTaggoWindowSizeClass.current
    val maxChipWidth = if (windowSizeClass == TaggoWindowSizeClass.Compact) 190.dp else 220.dp
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = Modifier.widthIn(max = maxChipWidth),
        label = {
            Text(
                text = displayTextForUi(tag, fullCjkFontReady),
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontFamily = fullCjkFontFamily,
            )
        },
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                )
            }
        } else {
            null
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = TaggoTheme.colors.primaryAccentSoft,
            selectedLabelColor = TaggoTheme.colors.textPrimary,
            selectedLeadingIconColor = TaggoTheme.colors.primaryAccent,
            containerColor = TaggoTheme.colors.panelBackgroundSoft,
            labelColor = MaterialTheme.colorScheme.onSurface,
            iconColor = TaggoTheme.colors.textSecondary,
            disabledContainerColor = TaggoTheme.colors.surfaceVariant,
        ),
    )
}

@Composable
internal fun SearchTagChip(
    tag: SearchTag,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    onRemove: () -> Unit,
) {
    val windowSizeClass = LocalTaggoWindowSizeClass.current
    val maxChipWidth = if (windowSizeClass == TaggoWindowSizeClass.Compact) 190.dp else 220.dp
    Surface(
        color = if (tag.source == SearchTagSource.LibraryTag) TaggoTheme.colors.primaryAccentSoft else TaggoTheme.colors.panelBackgroundSoft,
        contentColor = if (tag.source == SearchTagSource.LibraryTag) TaggoTheme.colors.textPrimary else TaggoTheme.colors.textSecondary,
        shape = RoundedCornerShape(999.dp),
        modifier = Modifier.widthIn(max = maxChipWidth),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(start = 10.dp, end = 3.dp, top = 3.dp, bottom = 3.dp),
        ) {
            Text(
                text = displayTextForUi(tag.value, fullCjkFontReady),
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontFamily = fullCjkFontFamily,
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(20.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
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
            text = displayTextForUi(tag, fullCjkFontReady),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
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
    val windowSizeClass = LocalTaggoWindowSizeClass.current
    val maxChipWidth = if (windowSizeClass == TaggoWindowSizeClass.Compact) 190.dp else 212.dp
    Box(
        modifier = Modifier
            .widthIn(max = maxChipWidth)
            .padding(top = 6.dp, end = 6.dp),
    ) {
        Surface(
            color = TaggoTheme.colors.panelBackgroundSoft,
            contentColor = TaggoTheme.colors.textSecondary,
            shape = RoundedCornerShape(999.dp),
            modifier = Modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        ) {
            Text(
                text = displayTextForUi(tag, fullCjkFontReady),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontFamily = fullCjkFontFamily,
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 6.dp, y = (-6).dp)
                .size(17.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(TaggoTheme.colors.panelBackground)
                .border(1.dp, TaggoTheme.colors.panelBorder, RoundedCornerShape(999.dp))
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = actionIcon,
                contentDescription = null,
                tint = TaggoTheme.colors.textSecondary,
                modifier = Modifier.size(11.dp),
            )
        }
    }
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

package com.example.cross_platformfilemanager.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cross_platformfilemanager.FileTypeCategory
import com.example.cross_platformfilemanager.FileTypeClassifier
import com.example.cross_platformfilemanager.FileReference
import com.example.cross_platformfilemanager.ThumbnailStatus
import com.example.cross_platformfilemanager.displayTextForUi
import com.example.cross_platformfilemanager.rememberThumbnailPainter
import com.example.cross_platformfilemanager.ui.theme.TaggoTheme
import com.example.cross_platformfilemanager.ui.theme.TaggoFileTypeColorTokens
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

internal data class FileTypeIconStyle(
    val icon: DrawableResource,
    val tint: Color,
    val backgroundBrush: Brush,
)

@Composable
internal fun FileCoverArtFrame(
    reference: FileReference,
    iconStyle: FileTypeIconStyle,
    fullCjkFontReady: Boolean,
    fullCjkFontFamily: FontFamily,
    modifier: Modifier = Modifier,
    cornerShape: RoundedCornerShape = RoundedCornerShape(16.dp),
    iconSize: Dp = 24.dp,
    showOpenStateOverlay: Boolean = true,
    canOpen: Boolean = true,
    overlayContainerSize: Dp = 18.dp,
    overlayIconSize: Dp = 11.dp,
    overlayBackgroundAlpha: Float = 0.12f,
    overlayIconAlpha: Float = 0.42f,
    disabledOverlayBackgroundAlpha: Float = 0.08f,
    disabledOverlayIconAlpha: Float = 0.30f,
) {
    val readyThumbnailPainter = rememberThumbnailPainter(reference.thumbnailPath)
        ?.takeIf { reference.thumbnailStatus == ThumbnailStatus.READY }
    val fileTypeCategory = FileTypeClassifier.classify(reference)
    Box(
        modifier = modifier
            .clip(cornerShape)
            .background(iconStyle.backgroundBrush),
        contentAlignment = Alignment.Center,
    ) {
        if (readyThumbnailPainter != null) {
            Image(
                painter = readyThumbnailPainter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.28f),
                            ),
                        ),
                    ),
            )
        } else {
            Icon(
                painter = painterResource(iconStyle.icon),
                contentDescription = null,
                tint = iconStyle.tint,
                modifier = Modifier.size(iconSize),
            )
        }

        if (showOpenStateOverlay) {
            val overlayIcon = when {
                !canOpen -> Icons.Outlined.Block
                fileTypeCategory == FileTypeCategory.Audio ||
                    fileTypeCategory == FileTypeCategory.Video -> Icons.Outlined.PlayArrow
                else -> Icons.AutoMirrored.Outlined.OpenInNew
            }
            FileCoverOpenStateOverlay(
                icon = overlayIcon,
                containerSize = overlayContainerSize,
                iconSize = overlayIconSize,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp),
                backgroundAlpha = when {
                    !canOpen -> disabledOverlayBackgroundAlpha
                    readyThumbnailPainter != null -> 0.38f
                    else -> overlayBackgroundAlpha
                },
                iconAlpha = when {
                    !canOpen -> disabledOverlayIconAlpha
                    readyThumbnailPainter != null -> 0.68f
                    else -> overlayIconAlpha
                },
            )
        }
    }
}

@Composable
private fun FileCoverOpenStateOverlay(
    icon: ImageVector,
    containerSize: Dp,
    iconSize: Dp,
    modifier: Modifier = Modifier,
    backgroundAlpha: Float,
    iconAlpha: Float,
) {
    val shape = RoundedCornerShape(999.dp)
    Box(
        modifier = modifier
            .size(containerSize)
            .clip(shape)
            .background(Color.Black.copy(alpha = backgroundAlpha)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = iconAlpha),
            modifier = Modifier.size(iconSize),
        )
    }
}

@Composable
internal fun fileTypeIconStyle(reference: FileReference): FileTypeIconStyle {
    val colors = TaggoTheme.colors

    fun style(category: FileTypeCategory): FileTypeIconStyle {
        val tokens = TaggoFileTypeColorTokens.forCategory(category)
        return FileTypeIconStyle(
            icon = FileTypeVisuals.iconDrawableForCategory(category),
            tint = tokens.iconColor,
            backgroundBrush = tokens.avatarBrush(colors.panelBackgroundSoft),
        )
    }

    return when (FileTypeClassifier.classify(reference)) {
        FileTypeCategory.TextDocument -> style(FileTypeCategory.TextDocument)
        FileTypeCategory.PdfDocument -> style(FileTypeCategory.PdfDocument)
        FileTypeCategory.Video -> style(FileTypeCategory.Video)
        FileTypeCategory.Audio -> style(FileTypeCategory.Audio)
        FileTypeCategory.Image -> style(FileTypeCategory.Image)
        FileTypeCategory.Archive -> style(FileTypeCategory.Archive)
        FileTypeCategory.Code -> style(FileTypeCategory.Code)
        FileTypeCategory.Spreadsheet -> style(FileTypeCategory.Spreadsheet)
        FileTypeCategory.Presentation -> style(FileTypeCategory.Presentation)
        FileTypeCategory.Folder -> style(FileTypeCategory.Folder)
        else -> FileTypeIconStyle(
            icon = FileTypeVisuals.iconDrawableForCategory(FileTypeCategory.Unknown),
            tint = TaggoFileTypeColorTokens.Other.iconColor,
            backgroundBrush = TaggoFileTypeColorTokens.Other.avatarBrush(colors.surface),
        )
    }
}

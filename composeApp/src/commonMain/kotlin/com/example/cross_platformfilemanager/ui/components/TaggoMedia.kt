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
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Slideshow
import androidx.compose.material.icons.outlined.TableChart
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

internal data class FileTypeIconStyle(
    val icon: ImageVector,
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
    overlayIconSize: Dp = 12.dp,
    overlayBackgroundAlpha: Float = 0.14f,
    overlayIconAlpha: Float = 0.46f,
    disabledOverlayBackgroundAlpha: Float = 0.08f,
    disabledOverlayIconAlpha: Float = 0.30f,
) {
    val thumbnailPainter = rememberThumbnailPainter(reference.thumbnailPath)
    val hasThumbnail = thumbnailPainter != null && reference.thumbnailStatus == ThumbnailStatus.READY
    val hasCoverArt = reference.coverArtSource?.isNotBlank() == true
    val fileTypeCategory = FileTypeClassifier.classify(reference)
    Box(
        modifier = modifier
            .clip(cornerShape)
            .background(iconStyle.backgroundBrush),
        contentAlignment = Alignment.Center,
    ) {
        if (hasThumbnail) {
            Image(
                painter = thumbnailPainter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        } else if (hasCoverArt) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Cover art",
                    color = iconStyle.tint,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                )
                Text(
                    text = displayTextForUi(reference.coverArtSource.orEmpty(), fullCjkFontReady),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = fullCjkFontFamily,
                )
            }
        } else {
            Icon(
                imageVector = iconStyle.icon,
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
                backgroundAlpha = if (canOpen) overlayBackgroundAlpha else disabledOverlayBackgroundAlpha,
                iconAlpha = if (canOpen) overlayIconAlpha else disabledOverlayIconAlpha,
            )
        }
    }
}

@Composable
private fun FileCoverOpenStateOverlay(
    icon: ImageVector,
    containerSize: Dp,
    iconSize: Dp,
    backgroundAlpha: Float,
    iconAlpha: Float,
) {
    val shape = RoundedCornerShape(999.dp)
    Box(
        modifier = Modifier
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

    fun style(icon: ImageVector, category: FileTypeCategory): FileTypeIconStyle {
        val tokens = TaggoFileTypeColorTokens.forCategory(category)
        return FileTypeIconStyle(
            icon = icon,
            tint = tokens.iconColor,
            backgroundBrush = tokens.avatarBrush(colors.panelBackgroundSoft),
        )
    }

    return when (FileTypeClassifier.classify(reference)) {
        FileTypeCategory.TextDocument -> style(Icons.Outlined.Description, FileTypeCategory.TextDocument)
        FileTypeCategory.PdfDocument -> style(Icons.Outlined.PictureAsPdf, FileTypeCategory.PdfDocument)
        FileTypeCategory.Video -> style(Icons.Outlined.Movie, FileTypeCategory.Video)
        FileTypeCategory.Audio -> style(Icons.Outlined.MusicNote, FileTypeCategory.Audio)
        FileTypeCategory.Image -> style(Icons.Outlined.Image, FileTypeCategory.Image)
        FileTypeCategory.Archive -> style(Icons.Outlined.Archive, FileTypeCategory.Archive)
        FileTypeCategory.Code -> style(Icons.Outlined.Code, FileTypeCategory.Code)
        FileTypeCategory.Spreadsheet -> style(Icons.Outlined.TableChart, FileTypeCategory.Spreadsheet)
        FileTypeCategory.Presentation -> style(Icons.Outlined.Slideshow, FileTypeCategory.Presentation)
        FileTypeCategory.Folder -> style(Icons.Outlined.Folder, FileTypeCategory.Folder)
        else -> FileTypeIconStyle(
            icon = Icons.Outlined.InsertDriveFile,
            tint = TaggoFileTypeColorTokens.Other.iconColor,
            backgroundBrush = TaggoFileTypeColorTokens.Other.avatarBrush(colors.surface),
        )
    }
}

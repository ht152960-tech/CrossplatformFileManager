package com.example.cross_platformfilemanager.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Slideshow
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
) {
    val thumbnailPainter = rememberThumbnailPainter(reference.thumbnailPath)
    val hasThumbnail = thumbnailPainter != null && reference.thumbnailStatus == ThumbnailStatus.READY
    val hasCoverArt = reference.coverArtSource?.isNotBlank() == true
    println("Thumbnail UI state: fileId=${reference.id}, hasThumbnailPath=${!reference.thumbnailPath.isNullOrBlank()}, thumbnailStatus=${reference.thumbnailStatus}, painterReady=$hasThumbnail, fallback=${!hasThumbnail}")
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
                contentScale = ContentScale.Crop,
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = iconStyle.icon,
                    contentDescription = null,
                    tint = iconStyle.tint,
                    modifier = Modifier.size(iconSize),
                )
                if (FileTypeClassifier.classify(reference) == FileTypeCategory.Video || FileTypeClassifier.classify(reference) == FileTypeCategory.Audio) {
                    Text(
                        text = if (reference.thumbnailStatus == ThumbnailStatus.GENERATING) "Loading" else "Reserved",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                    )
                }
            }
        }
    }
}

@Composable
internal fun fileTypeIconStyle(reference: FileReference): FileTypeIconStyle {
    val colors = TaggoTheme.colors

    fun accentBrush(accent: Color): Brush =
        Brush.linearGradient(
            listOf(
                colors.surfaceVariant,
                accent.copy(alpha = 0.18f),
            ),
        )

    return when (FileTypeClassifier.classify(reference)) {
        FileTypeCategory.TextDocument -> FileTypeIconStyle(
            icon = Icons.Outlined.Description,
            tint = Color(0xFF7AA2FF),
            backgroundBrush = accentBrush(Color(0xFF7AA2FF)),
        )
        FileTypeCategory.PdfDocument -> FileTypeIconStyle(
            icon = Icons.Outlined.PictureAsPdf,
            tint = Color(0xFFFF6B7D),
            backgroundBrush = accentBrush(Color(0xFFFF6B7D)),
        )
        FileTypeCategory.Video -> FileTypeIconStyle(
            icon = Icons.Outlined.Movie,
            tint = Color(0xFF9A7BFF),
            backgroundBrush = accentBrush(Color(0xFF9A7BFF)),
        )
        FileTypeCategory.Audio -> FileTypeIconStyle(
            icon = Icons.Outlined.MusicNote,
            tint = Color(0xFF55D6C2),
            backgroundBrush = accentBrush(Color(0xFF55D6C2)),
        )
        FileTypeCategory.Image -> FileTypeIconStyle(
            icon = Icons.Outlined.Image,
            tint = Color(0xFF64C7F5),
            backgroundBrush = accentBrush(Color(0xFF64C7F5)),
        )
        FileTypeCategory.Archive -> FileTypeIconStyle(
            icon = Icons.Outlined.Archive,
            tint = Color(0xFFF5B84B),
            backgroundBrush = accentBrush(Color(0xFFF5B84B)),
        )
        FileTypeCategory.Code -> FileTypeIconStyle(
            icon = Icons.Outlined.Code,
            tint = Color(0xFF7C5CFF),
            backgroundBrush = accentBrush(Color(0xFF7C5CFF)),
        )
        FileTypeCategory.Spreadsheet -> FileTypeIconStyle(
            icon = Icons.Outlined.TableChart,
            tint = Color(0xFF43D17A),
            backgroundBrush = accentBrush(Color(0xFF43D17A)),
        )
        FileTypeCategory.Presentation -> FileTypeIconStyle(
            icon = Icons.Outlined.Slideshow,
            tint = Color(0xFFFF7DB2),
            backgroundBrush = accentBrush(Color(0xFFFF7DB2)),
        )
        FileTypeCategory.Folder -> FileTypeIconStyle(
            icon = Icons.Outlined.Folder,
            tint = Color(0xFFFFC562),
            backgroundBrush = accentBrush(Color(0xFFFFC562)),
        )
        else -> FileTypeIconStyle(
            icon = Icons.Outlined.InsertDriveFile,
            tint = colors.textSecondary,
            backgroundBrush = Brush.linearGradient(listOf(colors.surface, colors.surfaceVariant)),
        )
    }
}

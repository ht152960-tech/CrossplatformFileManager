package com.example.cross_platformfilemanager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import java.io.File

actual fun createThumbnailGenerator(): ThumbnailGenerator? = DesktopThumbnailGenerator()

private class DesktopThumbnailGenerator : ThumbnailGenerator {
    override suspend fun generateThumbnail(reference: FileReference): ThumbnailResult = withContext(Dispatchers.IO) {
        if (FileTypeClassifier.classify(reference) != FileTypeCategory.Image) {
            return@withContext ThumbnailResult.Unsupported("desktop thumbnail generation currently supports images only")
        }

        val sourceFile = File(reference.source)
        if (!sourceFile.exists() || !sourceFile.isFile) {
            return@withContext ThumbnailResult.Failed("source file missing")
        }

        runCatching {
            val encodedSource = sourceFile.readBytes()
            val sourceImage = Image.makeFromEncoded(encodedSource)
            val maxEdge = ThumbnailConfig.MAX_SIZE_PX
            val scale = minOf(
                maxEdge.toFloat() / sourceImage.width.toFloat(),
                maxEdge.toFloat() / sourceImage.height.toFloat(),
                1f,
            )
            val targetWidth = maxOf(1, (sourceImage.width * scale).toInt())
            val targetHeight = maxOf(1, (sourceImage.height * scale).toInt())

            val surface = Surface.makeRasterN32Premul(targetWidth, targetHeight)
            surface.canvas.drawImageRect(
                sourceImage,
                Rect.makeWH(sourceImage.width.toFloat(), sourceImage.height.toFloat()),
                Rect.makeWH(targetWidth.toFloat(), targetHeight.toFloat()),
                Paint(),
            )

            val thumbnailBytes = surface.makeImageSnapshot()
                .encodeToData(EncodedImageFormat.WEBP, ThumbnailConfig.WEBP_QUALITY)
                ?.bytes
                ?: return@runCatching ThumbnailResult.Failed("webp encode returned null")

            val thumbnailFile = thumbnailFileFor(reference.id)
            thumbnailFile.parentFile?.mkdirs()
            thumbnailFile.writeBytes(thumbnailBytes)
            ThumbnailResult.Ready(thumbnailFile.absolutePath)
        }.getOrElse { error ->
            ThumbnailResult.Failed(error.message ?: error::class.simpleName.orEmpty())
        }
    }

    override fun deleteThumbnail(thumbnailPath: String) {
        runCatching {
            val target = File(thumbnailPath)
            if (target.exists()) {
                target.delete()
            }
        }
    }

    private fun thumbnailFileFor(fileId: String): File {
        val root = File(System.getProperty("user.home"), ".cross-platform-file-manager/app_cache/thumbnails")
        return File(root, "$fileId.webp")
    }
}

@Composable
actual fun rememberThumbnailPainter(thumbnailPath: String?): Painter? {
    val path = thumbnailPath?.trim()?.takeIf { it.isNotBlank() } ?: return null
    return remember(path) {
        runCatching {
            val file = File(path)
            if (!file.exists() || !file.isFile) {
                return@runCatching null
            }
            val image = Image.makeFromEncoded(file.readBytes())
            BitmapPainter(image.toComposeImageBitmap())
        }.getOrNull()
    }
}

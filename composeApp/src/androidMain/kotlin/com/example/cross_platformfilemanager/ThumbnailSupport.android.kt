package com.example.cross_platformfilemanager

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.LruCache
import android.util.Size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private object AndroidThumbnailMemoryCache {
    private const val KEY_PREFIX = "android-thumbnail:"

    private val cache = object : LruCache<String, Bitmap>(48) {
        override fun sizeOf(key: String, value: Bitmap): Int = 1
    }

    fun keyFor(reference: FileReference): String =
        "$KEY_PREFIX${reference.id}:${reference.source.hashCode()}:${reference.fileSizeBytes ?: 0L}"

    fun put(key: String, bitmap: Bitmap) {
        cache.put(key, bitmap)
    }

    fun get(key: String?): Bitmap? =
        key?.takeIf { it.startsWith(KEY_PREFIX) }?.let(cache::get)

    fun remove(key: String) {
        cache.remove(key)
    }
}

actual fun createThumbnailGenerator(): ThumbnailGenerator? =
    AndroidContextHolder.applicationContext?.contentResolver?.let(::AndroidImageThumbnailGenerator)

@Composable
actual fun rememberThumbnailPainter(thumbnailPath: String?): Painter? {
    val bitmap = AndroidThumbnailMemoryCache.get(thumbnailPath) ?: return null
    return remember(thumbnailPath, bitmap) {
        BitmapPainter(bitmap.asImageBitmap())
    }
}

private class AndroidImageThumbnailGenerator(
    private val contentResolver: android.content.ContentResolver,
) : ThumbnailGenerator {
    override suspend fun generateThumbnail(reference: FileReference): ThumbnailResult =
        withContext(Dispatchers.IO) {
            if (FileTypeClassifier.classify(reference) != FileTypeCategory.Image) {
                return@withContext ThumbnailResult.Unsupported("only image thumbnails are supported on Android")
            }

            val uri = runCatching { Uri.parse(reference.source) }.getOrNull()
                ?: return@withContext ThumbnailResult.Failed("invalid image uri")
            if (uri.scheme != "content") {
                return@withContext ThumbnailResult.Unsupported("only content uri images are supported")
            }

            val bitmap = loadThumbnail(uri) ?: decodeSampledBitmap(uri)
                ?: return@withContext ThumbnailResult.Failed("image thumbnail decode failed")
            val key = AndroidThumbnailMemoryCache.keyFor(reference)
            AndroidThumbnailMemoryCache.put(key, bitmap)
            ThumbnailResult.Ready(key)
        }

    override fun deleteThumbnail(thumbnailPath: String) {
        AndroidThumbnailMemoryCache.remove(thumbnailPath)
    }

    private fun loadThumbnail(uri: Uri): Bitmap? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            runCatching {
                contentResolver.loadThumbnail(
                    uri,
                    Size(ThumbnailConfig.MAX_SIZE_PX, ThumbnailConfig.MAX_SIZE_PX),
                    null,
                )
            }.getOrNull()
        } else {
            null
        }

    private fun decodeSampledBitmap(uri: Uri): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        runCatching {
            contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, bounds)
            }
        }
        val sampleSize = calculateInSampleSize(
            width = bounds.outWidth,
            height = bounds.outHeight,
            targetSize = ThumbnailConfig.MAX_SIZE_PX,
        )
        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return runCatching {
            contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOptions)
            }
        }.getOrNull()
    }

    private fun calculateInSampleSize(
        width: Int,
        height: Int,
        targetSize: Int,
    ): Int {
        if (width <= 0 || height <= 0) return 1
        var sampleSize = 1
        var halfWidth = width / 2
        var halfHeight = height / 2
        while (halfWidth / sampleSize >= targetSize && halfHeight / sampleSize >= targetSize) {
            sampleSize *= 2
        }
        return sampleSize.coerceAtLeast(1)
    }
}

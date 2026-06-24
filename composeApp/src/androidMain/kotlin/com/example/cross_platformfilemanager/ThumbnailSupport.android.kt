package com.example.cross_platformfilemanager

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
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
    AndroidContextHolder.applicationContext?.contentResolver?.let(::AndroidMediaThumbnailGenerator)

@Composable
actual fun rememberThumbnailPainter(thumbnailPath: String?): Painter? {
    val bitmap = AndroidThumbnailMemoryCache.get(thumbnailPath) ?: return null
    return remember(thumbnailPath, bitmap) {
        BitmapPainter(bitmap.asImageBitmap())
    }
}

private class AndroidMediaThumbnailGenerator(
    private val contentResolver: android.content.ContentResolver,
) : ThumbnailGenerator {
    override suspend fun generateThumbnail(reference: FileReference): ThumbnailResult =
        withContext(Dispatchers.IO) {
            val category = FileTypeClassifier.classify(reference)
            if (category != FileTypeCategory.Image && category != FileTypeCategory.Video) {
                return@withContext ThumbnailResult.Unsupported("only image and video thumbnails are supported on Android")
            }

            val uri = runCatching { Uri.parse(reference.source) }.getOrNull()
                ?: return@withContext ThumbnailResult.Failed("invalid media uri")
            if (uri.scheme != "content") {
                return@withContext ThumbnailResult.Unsupported("only content uri media thumbnails are supported")
            }

            val bitmap = when (category) {
                FileTypeCategory.Image -> loadThumbnail(uri) ?: decodeSampledBitmap(uri)
                FileTypeCategory.Video -> loadThumbnail(uri) ?: retrieveVideoFrame(uri)
                else -> null
            }?.scaledToThumbnail()
            if (bitmap == null) {
                debugLog(
                    "TaggoThumbnailAndroid",
                    "actual result null id=${reference.id} category=$category uriScheme=${uri.scheme.orEmpty()} " +
                        "isContent=${uri.scheme == "content"} reason=decode failed"
                )
                return@withContext ThumbnailResult.Failed("${category.name.lowercase()} thumbnail decode failed")
            }
            debugLog("TaggoThumbnailAndroid", "actual result bitmap non-null id=${reference.id} category=$category")
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
            }.onFailure { error ->
                logThumbnailFailure("loadThumbnail", uri, error)
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
        }.onFailure { error ->
            logThumbnailFailure("BitmapFactory bounds", uri, error)
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
        }.onFailure { error ->
            logThumbnailFailure("BitmapFactory decode", uri, error)
        }.getOrNull().also { bitmap ->
            if (bitmap == null) {
                debugLog(
                    "TaggoThumbnailAndroid",
                    "BitmapFactory decode returned null uriScheme=${uri.scheme.orEmpty()} isContent=${uri.scheme == "content"}"
                )
            }
        }
    }

    private fun retrieveVideoFrame(uri: Uri): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            val descriptor = try {
                contentResolver.openFileDescriptor(uri, "r")
            } catch (error: Exception) {
                logThumbnailFailure("openFileDescriptor", uri, error)
                null
            }
            descriptor?.use { opened ->
                retriever.setDataSource(opened.fileDescriptor)
                val frameTimeUs = retriever.middleFrameTimeUs()
                retriever.getFrameAtTime(frameTimeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            }
        } catch (error: Exception) {
            logThumbnailFailure("MediaMetadataRetriever", uri, error)
            null
        } finally {
            runCatching { retriever.release() }
        }
    }

    private fun logThumbnailFailure(stage: String, uri: Uri, error: Throwable) {
        debugLog(
            "TaggoThumbnailAndroid",
            "$stage failed type=${error::class.simpleName.orEmpty()} " +
                "message=${error.message.orEmpty().thumbnailLogValue()} uriScheme=${uri.scheme.orEmpty()} " +
                "isContent=${uri.scheme == "content"}"
        )
    }

    private fun String.thumbnailLogValue(): String =
        replace('\n', ' ').replace('\r', ' ').take(96)

    private fun MediaMetadataRetriever.middleFrameTimeUs(): Long {
        val durationMs = runCatching {
            extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
        }.getOrNull()
        return durationMs
            ?.takeIf { it > 0L }
            ?.let { duration -> (duration / 2L).coerceAtMost(Long.MAX_VALUE / 1_000L) * 1_000L }
            ?: 0L
    }

    private fun Bitmap.scaledToThumbnail(): Bitmap {
        val maxDimension = maxOf(width, height)
        if (maxDimension <= ThumbnailConfig.MAX_SIZE_PX || maxDimension <= 0) return this

        val scale = ThumbnailConfig.MAX_SIZE_PX.toFloat() / maxDimension.toFloat()
        val scaledWidth = (width * scale).toInt().coerceAtLeast(1)
        val scaledHeight = (height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(this, scaledWidth, scaledHeight, true)
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

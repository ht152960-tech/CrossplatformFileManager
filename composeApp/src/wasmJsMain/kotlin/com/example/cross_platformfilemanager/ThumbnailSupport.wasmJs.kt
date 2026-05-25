@file:OptIn(
    kotlin.io.encoding.ExperimentalEncodingApi::class,
    kotlin.js.ExperimentalWasmJsInterop::class,
)

package com.example.cross_platformfilemanager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.await
import kotlin.io.encoding.Base64
import kotlin.js.JsAny
import kotlin.js.Promise
import org.jetbrains.skia.Image

// Wasm 端缩略图能力通过浏览器桥接实现，生成结果以 data URL 形式回写到共享层。
actual fun createThumbnailGenerator(): ThumbnailGenerator? = BrowserThumbnailGeneratorWasm()

private external interface BrowserThumbnailInteropWasm {
    fun generateThumbnail(source: String, maxSize: Int, quality: Int): Promise<JsAny?>
}

private class BrowserThumbnailGeneratorWasm : ThumbnailGenerator {
    override suspend fun generateThumbnail(reference: FileReference): ThumbnailResult {
        println("Web thumbnail start: fileId=${reference.id}, fileKind=${reference.webThumbnailKind()}, mimeType=${reference.fileType}")
        if (reference.sourceKind != FileSourceKind.BrowserHandle) {
            return ThumbnailResult.Unsupported("web thumbnail generation only supports browser-selected files")
        }
        if (!reference.isLikelyWebThumbnailCandidate()) {
            return ThumbnailResult.Unsupported("web thumbnail generation currently supports images and videos only")
        }
        val bridge = browserInterop() ?: return ThumbnailResult.Failed("browser thumbnail bridge unavailable")
        val encodedJs: JsAny? = bridge.generateThumbnail(
            reference.source,
            ThumbnailConfig.MAX_SIZE_PX,
            ThumbnailConfig.WEBP_QUALITY,
        ).await()
        return encodedJs.toThumbnailResult(reference.id)
    }

    override fun deleteThumbnail(thumbnailPath: String) = Unit
}

@Composable
actual fun rememberThumbnailPainter(thumbnailPath: String?): Painter? {
    val path = thumbnailPath?.trim()?.takeIf { it.isNotBlank() } ?: return null
    return remember(path) {
        println("Thumbnail UI load attempt: hasThumbnail=true, length=${path.length}")
        decodeThumbnailDataUrl(path)
    }
}

private fun decodeThumbnailDataUrl(thumbnailPath: String): Painter? = runCatching {
    val encoded = thumbnailPath.substringAfter("base64,", "")
    if (encoded.isBlank()) {
        return@runCatching null
    }
    val image = Image.makeFromEncoded(Base64.decode(encoded))
    BitmapPainter(image.toComposeImageBitmap())
}.onFailure {
    println("Thumbnail UI load failed: ${it.message}")
}.getOrNull()

// Wasm 端只对图像和视频做缩略图尝试，避免把不支持的来源交给浏览器桥接白跑一遍。
private fun FileReference.isLikelyWebThumbnailCandidate(): Boolean {
    val normalizedType = fileType.trim().lowercase()
    if (normalizedType in WEB_IMAGE_TYPES || normalizedType in WEB_VIDEO_TYPES) {
        return true
    }
    val normalizedSource = source.trim().lowercase()
    return WEB_IMAGE_SUFFIXES.any { suffix -> normalizedSource.endsWith(suffix) } ||
        WEB_VIDEO_SUFFIXES.any { suffix -> normalizedSource.endsWith(suffix) }
}

private fun FileReference.webThumbnailKind(): String {
    val normalizedType = fileType.trim().lowercase()
    val normalizedSource = source.trim().lowercase()
    return when {
        normalizedType in WEB_IMAGE_TYPES || WEB_IMAGE_SUFFIXES.any { normalizedSource.endsWith(it) } -> "image"
        normalizedType in WEB_VIDEO_TYPES || WEB_VIDEO_SUFFIXES.any { normalizedSource.endsWith(it) } -> "video"
        else -> "unknown"
    }
}

private fun browserInterop(): BrowserThumbnailInteropWasm? =
    js("(globalThis.fileAtlasBrowser || null)")

private fun JsAny?.toThumbnailResult(fileId: String): ThumbnailResult {
    val encoded = this?.toString().orEmpty()
    val status = encoded.substringBefore('\t').trim()
    val payload = encoded.substringAfter('\t', "").trim()
    return when {
        status == "ready" && payload.isNotBlank() -> {
            println("Web thumbnail ready: fileId=$fileId, saveMode=thumbnailPathDataUrl, dataLength=${payload.length}")
            ThumbnailResult.Ready(payload)
        }

        status == "unsupported" -> ThumbnailResult.Unsupported(payload.ifBlank {
            "browser thumbnail generation unsupported"
        })

        status == "failed" -> ThumbnailResult.Failed(payload.ifBlank {
            "browser thumbnail generation failed"
        })

        else -> ThumbnailResult.Failed("browser thumbnail generation returned an invalid result")
    }
}

private val WEB_IMAGE_TYPES = setOf(
    "image",
    "png",
    "jpg",
    "jpeg",
    "gif",
    "webp",
    "bmp",
    "svg",
)

private val WEB_IMAGE_SUFFIXES = listOf(
    ".png",
    ".jpg",
    ".jpeg",
    ".gif",
    ".webp",
    ".bmp",
    ".svg",
)

private val WEB_VIDEO_TYPES = setOf(
    "video",
    "mp4",
    "mov",
    "avi",
    "mkv",
    "webm",
    "mpg",
    "mpeg",
)

private val WEB_VIDEO_SUFFIXES = listOf(
    ".mp4",
    ".mov",
    ".avi",
    ".mkv",
    ".webm",
    ".mpg",
    ".mpeg",
)

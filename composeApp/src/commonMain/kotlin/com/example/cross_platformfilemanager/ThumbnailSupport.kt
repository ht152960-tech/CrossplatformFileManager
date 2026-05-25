package com.example.cross_platformfilemanager

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter

enum class ThumbnailStatus {
    NONE,
    GENERATING,
    READY,
    FAILED,
    UNSUPPORTED,
}

object ThumbnailConfig {
    const val MAX_SIZE_PX = 320
    const val WEBP_QUALITY = 75
}

interface ThumbnailGenerator {
    suspend fun generateThumbnail(reference: FileReference): ThumbnailResult
    fun deleteThumbnail(thumbnailPath: String)
}

sealed interface ThumbnailResult {
    data class Ready(val thumbnailPath: String) : ThumbnailResult
    data class Failed(val reason: String) : ThumbnailResult
    data class Unsupported(val reason: String) : ThumbnailResult
}

expect fun createThumbnailGenerator(): ThumbnailGenerator?

@Composable
expect fun rememberThumbnailPainter(thumbnailPath: String?): Painter?

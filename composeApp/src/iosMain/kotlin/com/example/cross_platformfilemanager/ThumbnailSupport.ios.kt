package com.example.cross_platformfilemanager

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter

actual fun createThumbnailGenerator(): ThumbnailGenerator? = null

@Composable
actual fun rememberThumbnailPainter(thumbnailPath: String?): Painter? = null

package com.example.cross_platformfilemanager

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter

// Android 端当前未接入缩略图生成和展示实现。
actual fun createThumbnailGenerator(): ThumbnailGenerator? = null

@Composable
actual fun rememberThumbnailPainter(thumbnailPath: String?): Painter? = null

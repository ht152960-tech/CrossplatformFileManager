package com.example.cross_platformfilemanager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

data class AppFontLoadState(
    val uiFontReady: Boolean,
    val failed: Boolean,
)

data class FullCjkFontLoadState(
    val fullCjkFontReady: Boolean,
    val failed: Boolean,
)

@Composable
expect fun rememberAppFontLoadState(): AppFontLoadState

@Composable
expect fun rememberFullCjkFontLoadState(): FullCjkFontLoadState

@Composable
expect fun reportComposeAppMounted()

expect fun reportStartupTrace(message: String)

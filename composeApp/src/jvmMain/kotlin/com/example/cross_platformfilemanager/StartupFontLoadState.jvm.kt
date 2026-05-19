package com.example.cross_platformfilemanager

import androidx.compose.runtime.Composable

@Composable
actual fun rememberAppFontLoadState(): AppFontLoadState = AppFontLoadState(
    uiFontReady = true,
    failed = false,
)

@Composable
actual fun rememberFullCjkFontLoadState(): FullCjkFontLoadState = FullCjkFontLoadState(
    fullCjkFontReady = true,
    failed = false,
)

@Composable
actual fun reportComposeAppMounted() {
}

actual fun reportStartupTrace(message: String) {
}

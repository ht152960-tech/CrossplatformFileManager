package com.example.cross_platformfilemanager

import androidx.compose.runtime.Composable

// Android 端当前直接把字体加载视为已就绪，不额外走异步探测流程。
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

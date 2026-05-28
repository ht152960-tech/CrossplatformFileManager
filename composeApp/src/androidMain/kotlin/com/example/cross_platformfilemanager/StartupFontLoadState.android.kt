package com.example.cross_platformfilemanager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

// Android 端当前不单独探测异步字体失败，只记录诊断能力缺失。
@Composable
actual fun rememberAppFontLoadState(): AppFontLoadState {
    LaunchedEffect(Unit) {
        reportStartupTrace("uiFont status=unsupported failureDetectionSupported=false platform=android")
    }
    return AppFontLoadState(
        uiFontReady = true,
        status = StartupFontDiagnosticStatus.Unsupported,
        failureDetectionSupported = false,
    )
}

@Composable
actual fun rememberFullCjkFontLoadState(): FullCjkFontLoadState {
    LaunchedEffect(Unit) {
        reportStartupTrace("fullCjkFont status=unsupported failureDetectionSupported=false platform=android")
    }
    return FullCjkFontLoadState(
        fullCjkFontReady = true,
        status = StartupFontDiagnosticStatus.Unsupported,
        failureDetectionSupported = false,
    )
}

@Composable
actual fun reportComposeAppMounted() {
}

actual fun reportStartupTrace(message: String) {
}

actual fun reportStartupTimeline(label: String) {
}

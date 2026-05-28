package com.example.cross_platformfilemanager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

enum class StartupFontDiagnosticStatus {
    Pending,
    Ready,
    Failed,
    Timeout,
    Unsupported,
}

/**
 * 主界面字体的加载状态。
 */
data class AppFontLoadState(
    val uiFontReady: Boolean,
    val status: StartupFontDiagnosticStatus,
    val failureDetectionSupported: Boolean,
)

/**
 * 完整中日韩字形字体的加载状态。
 *
 * 这个状态独立于主界面字体，
 * 便于启动阶段分别判断“基础 UI 可用”与“完整中文显示可用”。
 */
data class FullCjkFontLoadState(
    val fullCjkFontReady: Boolean,
    val status: StartupFontDiagnosticStatus,
    val failureDetectionSupported: Boolean,
)

@Composable
expect fun rememberAppFontLoadState(): AppFontLoadState

@Composable
expect fun rememberFullCjkFontLoadState(): FullCjkFontLoadState

@Composable
expect fun reportComposeAppMounted()

expect fun reportStartupTrace(message: String)

expect fun reportStartupTimeline(label: String)

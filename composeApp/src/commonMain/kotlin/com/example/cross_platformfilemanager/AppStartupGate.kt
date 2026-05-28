package com.example.cross_platformfilemanager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

/**
 * 应用启动门面。
 *
 * 当前仅等待快照恢复，字体状态只用于启动诊断，不再阻塞首页展示。
 */
@Composable
fun AppStartupGate(
    snapshotReady: Boolean,
    fontLoadState: AppFontLoadState,
    fullCjkFontLoadState: FullCjkFontLoadState,
    content: @Composable () -> Unit,
) {
    val gateStartMillis = remember { nowMillis() }

    val branch = if (!snapshotReady) "loading" else "content"

    LaunchedEffect(
        branch,
        snapshotReady,
        fontLoadState.uiFontReady,
        fontLoadState.status,
        fontLoadState.failureDetectionSupported,
        fullCjkFontLoadState.fullCjkFontReady,
        fullCjkFontLoadState.status,
        fullCjkFontLoadState.failureDetectionSupported,
    ) {
        reportStartupTimeline("AppStartupGate branch=$branch")
        reportStartupTrace(
            "AppStartupGate branch=$branch elapsed=${nowMillis() - gateStartMillis}ms " +
                "snapshotReady=$snapshotReady " +
                "uiFontReady=${fontLoadState.uiFontReady} " +
                "uiFontStatus=${fontLoadState.status} " +
                "uiFontFailureDetectionSupported=${fontLoadState.failureDetectionSupported} " +
                "fullCjkFontReady=${fullCjkFontLoadState.fullCjkFontReady} " +
                "fullCjkFontStatus=${fullCjkFontLoadState.status} " +
                "fullCjkFontFailureDetectionSupported=${fullCjkFontLoadState.failureDetectionSupported}",
        )
    }

    when (branch) {
        "loading" -> {
            StartupSplashScreen(
                snapshotReady = snapshotReady,
                uiFontReady = fontLoadState.uiFontReady,
                fullCjkFontReady = fullCjkFontLoadState.fullCjkFontReady,
            )
        }

        else -> {
            reportComposeAppMounted()
            reportStartupTimeline("AppStartupGate content invoked")
            content()
        }
    }
}

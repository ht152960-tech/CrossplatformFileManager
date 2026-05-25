package com.example.cross_platformfilemanager

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

/**
 * 应用启动门面。
 *
 * 只有当快照恢复和字体加载都满足条件后，才真正展示主内容；
 * 否则在启动阶段统一显示加载页或错误页。
 */
@Composable
fun AppStartupGate(
    snapshotReady: Boolean,
    fontLoadState: AppFontLoadState,
    fullCjkFontLoadState: FullCjkFontLoadState,
    content: @Composable () -> Unit,
) {
    val gateStartMillis = remember { nowMillis() }

    val branch = when {
        fontLoadState.failed -> "failed"
        fullCjkFontLoadState.failed -> "failed"
        !snapshotReady || !fontLoadState.uiFontReady || !fullCjkFontLoadState.fullCjkFontReady -> "loading"
        else -> "content"
    }

    LaunchedEffect(
        branch,
        snapshotReady,
        fontLoadState.uiFontReady,
        fontLoadState.failed,
        fullCjkFontLoadState.fullCjkFontReady,
        fullCjkFontLoadState.failed,
    ) {
        reportStartupTrace(
            "AppStartupGate branch=$branch elapsed=${nowMillis() - gateStartMillis}ms " +
                "snapshotReady=$snapshotReady " +
                "uiFontReady=${fontLoadState.uiFontReady} " +
                "uiFontFailed=${fontLoadState.failed} " +
                "fullCjkFontReady=${fullCjkFontLoadState.fullCjkFontReady} " +
                "fullCjkFontFailed=${fullCjkFontLoadState.failed}",
        )
    }

    Crossfade(
        targetState = branch,
        animationSpec = tween(durationMillis = 140),
    ) { currentBranch ->
        when (currentBranch) {
            "failed" -> {
                reportComposeAppMounted()
                StartupFontErrorScreen()
            }

            "loading" -> {
                StartupSplashScreen(
                    snapshotReady = snapshotReady,
                    uiFontReady = fontLoadState.uiFontReady,
                    fullCjkFontReady = fullCjkFontLoadState.fullCjkFontReady,
                )
            }

            else -> {
                reportComposeAppMounted()
                content()
            }
        }
    }
}

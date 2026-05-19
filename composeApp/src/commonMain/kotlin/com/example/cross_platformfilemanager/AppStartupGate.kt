package com.example.cross_platformfilemanager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.runtime.remember

@Composable
fun AppStartupGate(
    snapshotReady: Boolean,
    fontLoadState: AppFontLoadState,
    content: @Composable () -> Unit,
) {
    val gateStartMillis = remember { nowMillis() }
    val branch = when {
        fontLoadState.failed -> "failed"
        !snapshotReady || !fontLoadState.uiFontReady -> "loading"
        else -> "content"
    }

    LaunchedEffect(
        branch,
        snapshotReady,
        fontLoadState.uiFontReady,
        fontLoadState.failed,
    ) {
        reportStartupTrace(
            "AppStartupGate branch=$branch elapsed=${nowMillis() - gateStartMillis}ms " +
                "snapshotReady=$snapshotReady " +
                "uiFontReady=${fontLoadState.uiFontReady} " +
                "uiFontFailed=${fontLoadState.failed}",
        )
    }

    when {
        fontLoadState.failed -> {
            reportComposeAppMounted()
            StartupFontErrorScreen()
        }

        !snapshotReady || !fontLoadState.uiFontReady -> {
            StartupSplashScreen(
                snapshotReady = snapshotReady,
                uiFontReady = fontLoadState.uiFontReady,
                fullCjkFontReady = false,
            )
        }

        else -> {
            reportComposeAppMounted()
            content()
        }
    }
}

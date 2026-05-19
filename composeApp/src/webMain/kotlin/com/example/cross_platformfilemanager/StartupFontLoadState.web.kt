package com.example.cross_platformfilemanager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import crossplatformfilemanager.composeapp.generated.resources.NotoSansSc
import crossplatformfilemanager.composeapp.generated.resources.NotoSansScUi
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.preloadFont
import kotlinx.browser.window
import kotlinx.browser.document

@OptIn(ExperimentalResourceApi::class)
@Composable
actual fun rememberAppFontLoadState(): AppFontLoadState {
    val uiFont by preloadFont(NotoSansScUi)
    val startedAt = remember { nowMillis() }

    LaunchedEffect(uiFont) {
        reportStartupTrace("uiFontReady=${uiFont != null} elapsed=${nowMillis() - startedAt}ms")
    }

    return AppFontLoadState(
        uiFontReady = uiFont != null,
        failed = false,
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
actual fun rememberFullCjkFontLoadState(): FullCjkFontLoadState {
    val fullCjkFont by preloadFont(NotoSansSc)
    val startedAt = remember { nowMillis() }

    LaunchedEffect(fullCjkFont) {
        reportStartupTrace("fullCjkFontReady=${fullCjkFont != null} elapsed=${nowMillis() - startedAt}ms")
    }

    return FullCjkFontLoadState(
        fullCjkFontReady = fullCjkFont != null,
        failed = false,
    )
}

@Composable
actual fun reportComposeAppMounted() {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        reportStartupTrace("reportComposeAppMounted dispatch")
        window.dispatchEvent(org.w3c.dom.events.Event("file-atlas-ui-ready"))
    }
}

actual fun reportStartupTrace(message: String) {
    println(message)
    val traceNode = document.getElementById("boot-trace") ?: return
    val existing = traceNode.textContent.orEmpty()
    traceNode.textContent = if (existing.isBlank()) message else "$existing\n$message"
}

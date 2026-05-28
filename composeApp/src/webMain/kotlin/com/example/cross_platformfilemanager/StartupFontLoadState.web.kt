package com.example.cross_platformfilemanager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.preloadFont
import org.w3c.fetch.Response
import taggo.composeapp.generated.resources.NotoSansSc
import taggo.composeapp.generated.resources.NotoSansScUi

private const val UiFontTimeoutMillis = 3_000L
private const val FullCjkFontTimeoutMillis = 8_000L
private const val UiFontResourceUrl = "composeResources/taggo.composeapp.generated.resources/font/noto_sans_sc_ui.woff2"
private const val FullCjkFontResourceUrl = "composeResources/taggo.composeapp.generated.resources/font/noto_sans_sc.ttf"

@OptIn(ExperimentalResourceApi::class)
@Composable
actual fun rememberAppFontLoadState(): AppFontLoadState {
    val uiFont by preloadFont(NotoSansScUi)
    val startedAt = remember { nowMillis() }
    var status by remember { mutableStateOf(StartupFontDiagnosticStatus.Pending) }

    LaunchedEffect(Unit) {
        reportStartupTimeline("uiFont preload start resource=noto_sans_sc_ui.woff2 size=118916")
    }

    LaunchedEffect(Unit) {
        val fetchStatus = probeFontResource(UiFontResourceUrl)
        if (uiFont == null && status == StartupFontDiagnosticStatus.Pending && fetchStatus == StartupFontDiagnosticStatus.Failed) {
            status = StartupFontDiagnosticStatus.Failed
        }
    }

    LaunchedEffect(Unit) {
        delay(UiFontTimeoutMillis)
        if (uiFont == null && status == StartupFontDiagnosticStatus.Pending) {
            status = StartupFontDiagnosticStatus.Timeout
        }
    }

    LaunchedEffect(uiFont) {
        if (uiFont != null) {
            status = StartupFontDiagnosticStatus.Ready
        }
    }

    LaunchedEffect(status) {
        reportFontStatus(
            name = "uiFont",
            status = status,
            startedAt = startedAt,
            failureDetectionSupported = true,
        )
    }

    return AppFontLoadState(
        uiFontReady = uiFont != null,
        status = status,
        failureDetectionSupported = true,
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
actual fun rememberFullCjkFontLoadState(): FullCjkFontLoadState {
    val fullCjkFont by preloadFont(NotoSansSc)
    val startedAt = remember { nowMillis() }
    var status by remember { mutableStateOf(StartupFontDiagnosticStatus.Pending) }

    LaunchedEffect(Unit) {
        reportStartupTimeline("fullCjkFont preload start resource=noto_sans_sc.ttf size=17773244")
    }

    LaunchedEffect(Unit) {
        val fetchStatus = probeFontResource(FullCjkFontResourceUrl)
        if (
            fullCjkFont == null &&
            status == StartupFontDiagnosticStatus.Pending &&
            fetchStatus == StartupFontDiagnosticStatus.Failed
        ) {
            status = StartupFontDiagnosticStatus.Failed
        }
    }

    LaunchedEffect(Unit) {
        delay(FullCjkFontTimeoutMillis)
        if (fullCjkFont == null && status == StartupFontDiagnosticStatus.Pending) {
            status = StartupFontDiagnosticStatus.Timeout
        }
    }

    LaunchedEffect(fullCjkFont) {
        if (fullCjkFont != null) {
            status = StartupFontDiagnosticStatus.Ready
        }
    }

    LaunchedEffect(status) {
        reportFontStatus(
            name = "fullCjkFont",
            status = status,
            startedAt = startedAt,
            failureDetectionSupported = true,
        )
    }

    return FullCjkFontLoadState(
        fullCjkFontReady = fullCjkFont != null,
        status = status,
        failureDetectionSupported = true,
    )
}

private suspend fun probeFontResource(resourceUrl: String): StartupFontDiagnosticStatus = runCatching {
    val response = window.fetch(resourceUrl).await<Response>()
    if (response.ok) {
        StartupFontDiagnosticStatus.Pending
    } else {
        StartupFontDiagnosticStatus.Failed
    }
}.getOrElse {
    StartupFontDiagnosticStatus.Failed
}

private fun reportFontStatus(
    name: String,
    status: StartupFontDiagnosticStatus,
    startedAt: Long,
    failureDetectionSupported: Boolean,
) {
    when (status) {
        StartupFontDiagnosticStatus.Pending -> {
            reportStartupTrace(
                "$name status=pending failureDetectionSupported=$failureDetectionSupported " +
                    "elapsed=${nowMillis() - startedAt}ms",
            )
        }

        StartupFontDiagnosticStatus.Ready -> {
            reportStartupTrace("$name status=ready elapsed=${nowMillis() - startedAt}ms")
            reportStartupTimeline("${name}Ready")
        }

        StartupFontDiagnosticStatus.Failed -> {
            reportStartupTrace(
                "$name status=failed failureDetectionSupported=$failureDetectionSupported " +
                    "elapsed=${nowMillis() - startedAt}ms",
            )
            reportStartupTimeline("${name}Failed")
        }

        StartupFontDiagnosticStatus.Timeout -> {
            reportStartupTrace(
                "$name status=timeout failureDetectionSupported=$failureDetectionSupported " +
                    "elapsed=${nowMillis() - startedAt}ms",
            )
            reportStartupTimeline("${name}Timeout")
        }

        StartupFontDiagnosticStatus.Unsupported -> {
            reportStartupTrace(
                "$name status=unsupported failureDetectionSupported=$failureDetectionSupported " +
                    "elapsed=${nowMillis() - startedAt}ms",
            )
        }
    }
}

@Composable
actual fun reportComposeAppMounted() {
    LaunchedEffect(Unit) {
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

actual fun reportStartupTimeline(label: String) {
    val now = window.performance.now()
    val base = document.documentElement
        ?.getAttribute("data-startup-trace-start")
        ?.toDoubleOrNull()
        ?: 0.0
    val elapsed = kotlin.math.round(now - base).toLong()
    reportStartupTrace("[startup] $label +${elapsed}ms")
}

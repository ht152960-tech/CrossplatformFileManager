package com.example.cross_platformfilemanager

import kotlinx.coroutines.await
import kotlin.js.JsAny
import kotlin.js.Promise

// 浏览器端直接用新标签页打开外部地址，本地磁盘路径则不在这里强行猜。
class JsPlatform : Platform {
    override val name: String = "Web with Kotlin/JS"
}

actual fun getPlatform(): Platform = JsPlatform()

private external interface BrowserOpenInterop {
    fun openReference(source: String): Promise<JsAny?>
}

actual suspend fun openReferenceExternally(reference: FileReference): Boolean {
    return openReferenceExternallyWithResult(reference).opened
}

actual suspend fun openReferenceExternallyWithResult(reference: FileReference): OpenReferenceResult {
    val target = reference.source.trim()
    if (target.isBlank()) return OpenReferenceResult(opened = false)
    val result: JsAny? = browserInterop().openReference(target).await()
    val payload = result?.toString().orEmpty()
    return when {
        payload == "ok" -> OpenReferenceResult(opened = true)
        payload.startsWith("error:") -> OpenReferenceResult(
            opened = false,
            message = payload.removePrefix("error:").ifBlank { null },
        )
        payload.toBooleanStrictOrNull() == true -> OpenReferenceResult(opened = true)
        else -> OpenReferenceResult(opened = false)
    }
}

private fun browserInterop(): BrowserOpenInterop = js("window.fileAtlasBrowser")

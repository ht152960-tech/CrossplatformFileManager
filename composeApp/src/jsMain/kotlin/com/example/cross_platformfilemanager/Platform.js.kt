package com.example.cross_platformfilemanager

import kotlinx.coroutines.await
import kotlin.js.JsAny
import kotlin.js.Promise

/**
 * Kotlin/JS 平台信息实现。
 *
 * Web 端的外部打开能力通过浏览器桥接对象完成，
 * 这里只负责把共享层的文件条目请求转交给前端宿主环境。
 */
class JsPlatform : Platform {
    override val name: String = "Web with Kotlin/JS"
}

actual fun getPlatform(): Platform = JsPlatform()

actual fun isReferenceExternallyOpenable(reference: FileReference): Boolean {
    if (reference.source.trim().isBlank()) return false
    return when (reference.sourceKind) {
        FileSourceKind.ManualPath,
        FileSourceKind.Url,
        FileSourceKind.BrowserHandle,
        -> true
        else -> false
    }
}

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

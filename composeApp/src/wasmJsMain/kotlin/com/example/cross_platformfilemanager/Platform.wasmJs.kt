package com.example.cross_platformfilemanager

import kotlinx.coroutines.await
import kotlin.js.JsAny
import kotlin.js.Promise

/**
 * Kotlin/Wasm 平台信息实现。
 *
 * 与 JS 端类似，Wasm 端的外部打开能力也通过浏览器桥接对象完成。
 */
class WasmPlatform : Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

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

private external interface BrowserOpenInteropWasm {
    fun openReference(source: String): Promise<JsAny?>
}

actual suspend fun openReferenceExternally(reference: FileReference): Boolean {
    return openReferenceExternallyWithResult(reference).opened
}

actual suspend fun openReferenceExternallyWithResult(reference: FileReference): OpenReferenceResult {
    val target = reference.source.trim()
    if (target.isBlank()) return OpenReferenceResult(opened = false)
    val bridge = browserInterop() ?: return OpenReferenceResult(opened = false)
    val result: JsAny? = bridge.openReference(target).await()
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

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun browserInterop(): BrowserOpenInteropWasm? =
    js("(globalThis.fileAtlasBrowser || null)")

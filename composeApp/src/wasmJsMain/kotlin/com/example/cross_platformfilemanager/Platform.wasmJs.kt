package com.example.cross_platformfilemanager

import kotlinx.browser.window

// Wasm 端和 JS 一样，只对标准网页地址做外部打开。
class WasmPlatform : Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

actual fun openReferenceExternally(reference: FileReference): Boolean {
    val target = reference.source.trim()
    if (target.isBlank()) return false
    if (!target.startsWith("http://", ignoreCase = true) && !target.startsWith("https://", ignoreCase = true)) {
        return false
    }
    return window.open(target, "_blank") != null
}

package com.example.cross_platformfilemanager

import kotlinx.browser.window

// 浏览器端直接用新标签页打开外部地址，本地磁盘路径则不在这里强行猜。
class JsPlatform : Platform {
    override val name: String = "Web with Kotlin/JS"
}

actual fun getPlatform(): Platform = JsPlatform()

actual fun openReferenceExternally(reference: FileReference): Boolean {
    val target = reference.source.trim()
    if (target.isBlank()) return false
    if (!target.startsWith("http://", ignoreCase = true) && !target.startsWith("https://", ignoreCase = true)) {
        return false
    }
    return window.open(target, "_blank") != null
}

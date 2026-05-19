package com.example.cross_platformfilemanager

import java.awt.Desktop
import java.io.File
import java.net.URI

// JVM 端可以直接交给系统桌面去打开本地文件或网址。
class JVMPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun openReferenceExternally(reference: FileReference): Boolean {
    val target = reference.source.trim()
    if (target.isBlank()) return false

    return runCatching {
        if (!Desktop.isDesktopSupported()) return false

        val desktop = Desktop.getDesktop()
        when {
            target.startsWith("http://", ignoreCase = true) || target.startsWith("https://", ignoreCase = true) -> {
                desktop.browse(URI(target))
            }
            target.startsWith("file:", ignoreCase = true) -> {
                val file = File(URI(target))
                if (!file.exists()) return false
                desktop.open(file)
            }
            else -> {
                val file = File(target)
                if (!file.exists()) return false
                desktop.open(file)
            }
        }
        true
    }.getOrDefault(false)
}

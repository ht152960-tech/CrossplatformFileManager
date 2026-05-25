package com.example.cross_platformfilemanager

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

/**
 * JVM 桌面端入口。
 *
 * 该入口创建桌面窗口并挂载共享 Compose 应用。
 */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "贴格 | Taggo",
    ) {
        App()
    }
}

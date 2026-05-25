package com.example.cross_platformfilemanager

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

//jvm端启动入口。
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "贴格 | Taggo",
    ) {
        App()
    }
}

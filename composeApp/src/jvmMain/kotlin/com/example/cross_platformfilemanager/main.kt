package com.example.cross_platformfilemanager

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "CrossplatformFileManager",
    ) {
        App()
    }
}
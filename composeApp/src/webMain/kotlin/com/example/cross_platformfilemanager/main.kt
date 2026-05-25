package com.example.cross_platformfilemanager

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

/**
 * Web 端入口。
 *
 * 这里把共享 Compose 应用挂载到浏览器视口。
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {
        App()
    }
}

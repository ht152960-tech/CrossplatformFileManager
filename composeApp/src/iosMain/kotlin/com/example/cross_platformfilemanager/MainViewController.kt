package com.example.cross_platformfilemanager

import androidx.compose.ui.window.ComposeUIViewController

/**
 * iOS 端入口视图控制器。
 *
 * 它把共享 Compose 应用挂载到 iOS 的 UIViewController 中。
 */
fun MainViewController() = ComposeUIViewController { App() }

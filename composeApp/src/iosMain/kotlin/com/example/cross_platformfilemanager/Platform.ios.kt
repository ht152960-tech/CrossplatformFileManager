package com.example.cross_platformfilemanager

import platform.UIKit.UIDevice

// iOS 端先只保留平台名；文件外部打开后续再按需要接系统能力。
class IOSPlatform : Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual suspend fun openReferenceExternally(reference: FileReference): Boolean = false

actual suspend fun openReferenceExternallyWithResult(reference: FileReference): OpenReferenceResult =
    OpenReferenceResult(opened = false)

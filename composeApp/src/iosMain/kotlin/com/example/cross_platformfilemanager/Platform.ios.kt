package com.example.cross_platformfilemanager

import platform.UIKit.UIDevice

/**
 * iOS 平台信息实现。
 *
 * 当前 iOS 端先只暴露平台名称，
 * 外部打开文件的能力后续再按需要接入系统接口。
 */
class IOSPlatform : Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

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

actual suspend fun openReferenceExternally(reference: FileReference): Boolean = false

actual suspend fun openReferenceExternallyWithResult(reference: FileReference): OpenReferenceResult =
    OpenReferenceResult(opened = false)

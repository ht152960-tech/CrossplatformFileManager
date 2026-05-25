package com.example.cross_platformfilemanager

import android.os.Build

/**
 * Android 平台信息实现。
 *
 * 当前 Android 端只提供平台名称，不在这里直接接入系统级外部打开能力。
 */
class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual suspend fun openReferenceExternally(reference: FileReference): Boolean = false

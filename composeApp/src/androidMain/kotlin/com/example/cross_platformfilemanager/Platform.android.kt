package com.example.cross_platformfilemanager

import android.os.Build

// Android 端暂时不接系统级打开器，避免在没有上下文时误触发。
class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual suspend fun openReferenceExternally(reference: FileReference): Boolean = false

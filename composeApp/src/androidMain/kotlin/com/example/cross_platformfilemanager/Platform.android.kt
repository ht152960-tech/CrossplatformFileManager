package com.example.cross_platformfilemanager

import android.os.Build

//Android 平台实现。
class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()
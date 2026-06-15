package com.example.cross_platformfilemanager

import android.content.Context

internal object AndroidContextHolder {
    var applicationContext: Context? = null
        private set

    fun register(context: Context) {
        applicationContext = context.applicationContext
    }
}

internal fun registerAndroidApplicationContext(context: Context) {
    AndroidContextHolder.register(context)
}

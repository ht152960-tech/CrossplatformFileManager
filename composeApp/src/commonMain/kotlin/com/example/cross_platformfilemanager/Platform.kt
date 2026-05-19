package com.example.cross_platformfilemanager

//跨平台能力抽象。
interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

// 详情页的“打开此文件”按钮会走这里，由各平台自己决定是否支持本地打开。
expect fun openReferenceExternally(reference: FileReference): Boolean

package com.example.cross_platformfilemanager

//跨平台能力抽象。
interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
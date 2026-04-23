package com.example.cross_platformfilemanager

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
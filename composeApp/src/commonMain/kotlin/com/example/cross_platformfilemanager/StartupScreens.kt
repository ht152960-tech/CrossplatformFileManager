package com.example.cross_platformfilemanager

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import taggo.composeapp.generated.resources.TaggoLogoSplashSafe2048

private val StartupBackgroundColor = Color(0xFF09111F)
private val StartupSpinnerColor = Color(0xFF60A5FA)
private val StartupErrorTextColor = Color(0xFFE5ECF6)

/**
 * 启动加载页。
 *
 * 这里只负责展示应用启动中的视觉占位，
 * 不承载业务逻辑，真正的启动判断由 [AppStartupGate] 统一控制。
 */
@Composable
fun StartupSplashScreen(
    snapshotReady: Boolean,
    uiFontReady: Boolean,
    fullCjkFontReady: Boolean,
) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        reportStartupTimeline("StartupSplashScreen first composition")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StartupBackgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Image(
                painter = painterResource(TaggoLogoSplashSafe2048),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .width(192.dp)
                    .height(128.dp),
            )
            CircularProgressIndicator(
                color = StartupSpinnerColor,
                strokeWidth = 4.dp,
                modifier = Modifier.size(38.dp),
            )
        }
    }
}

/**
 * 启动失败页。
 *
 * 当前主要用于字体加载失败等启动前置条件不满足的场景。
 */
@Composable
fun StartupFontErrorScreen() {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        reportStartupTimeline("StartupFontErrorScreen first composition")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StartupBackgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Image(
                painter = painterResource(TaggoLogoSplashSafe2048),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .width(160.dp)
                    .height(106.dp),
            )
            androidx.compose.material3.Text(
                text = "Startup failed",
                color = StartupErrorTextColor,
            )
        }
    }
}

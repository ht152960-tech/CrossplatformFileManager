package com.example.cross_platformfilemanager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import taggo.composeapp.generated.resources.TaggoLogoSplashSafe2048

@Composable
fun StartupSplashScreen(
    snapshotReady: Boolean,
    uiFontReady: Boolean,
    fullCjkFontReady: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(startupBackgroundBrush()),
        contentAlignment = Alignment.Center,
    ) {
        Column(
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
                color = Color(0xFF2563EB),
                strokeWidth = 4.dp,
                modifier = Modifier.size(38.dp),
            )
        }
    }
}

@Composable
fun StartupFontErrorScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(startupBackgroundBrush()),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Image(
                painter = painterResource(TaggoLogoSplashSafe2048),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .width(160.dp)
                    .height(106.dp),
            )
        }
    }
}

private fun startupBackgroundBrush(): Brush =
    Brush.verticalGradient(
        listOf(
            Color.White,
            Color(0xFFFBFCFF),
        ),
    )

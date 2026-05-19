package com.example.cross_platformfilemanager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Folder,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.92f),
                modifier = Modifier.size(54.dp),
            )
            CircularProgressIndicator(
                color = Color.White.copy(alpha = 0.86f),
                strokeWidth = 3.dp,
                modifier = Modifier.size(30.dp),
            )
            Text(
                text = "Loading app...",
                color = Color.White,
                fontSize = 18.sp,
            )
            Text(
                text = "snapshotReady=$snapshotReady  uiFontReady=$uiFontReady  fullCjkFontReady=$fullCjkFontReady",
                color = Color.White.copy(alpha = 0.86f),
                fontSize = 12.sp,
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Font loading failed",
                color = Color.White,
                fontSize = 20.sp,
            )
            Text(
                text = "The UI font resource did not become ready. Check the font asset path and network response.",
                color = Color.White.copy(alpha = 0.78f),
                fontSize = 14.sp,
            )
        }
    }
}

private fun startupBackgroundBrush(): Brush =
    Brush.verticalGradient(
        listOf(
            Color(0xFF0F172A),
            Color(0xFF111D37),
            Color(0xFF182B52),
        ),
    )

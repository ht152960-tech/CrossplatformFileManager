package com.example.cross_platformfilemanager

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
actual fun TaggoBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    BackHandler(enabled = enabled, onBack = onBack)
}

package com.example.cross_platformfilemanager

import androidx.compose.runtime.Composable

@Composable
expect fun TaggoBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
)

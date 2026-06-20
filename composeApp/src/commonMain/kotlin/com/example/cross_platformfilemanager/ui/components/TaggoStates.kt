package com.example.cross_platformfilemanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cross_platformfilemanager.ui.theme.TaggoTheme

@Composable
internal fun EmptyPanel(
    title: String,
    body: String,
    compactGlass: Boolean = false,
) {
    if (compactGlass) {
        val shape = RoundedCornerShape(24.dp)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = shape,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x120F1424), shape)
                .border(1.dp, Color(0x22A9B8FF), shape),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x10161B31))
                        .border(1.dp, Color(0x1FA9B8FF), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FolderOpen,
                        contentDescription = null,
                        tint = Color(0xA8BFC7D8),
                        modifier = Modifier.size(24.dp),
                    )
                }
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TaggoTheme.colors.textPrimary,
                )
                Text(
                    text = body,
                    color = TaggoTheme.colors.textSecondary,
                    fontSize = 14.sp,
                )
            }
        }
    } else {
        Card(
            colors = CardDefaults.cardColors(containerColor = TaggoTheme.colors.panelBackgroundSoft),
            shape = RoundedCornerShape(15.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(text = title, fontWeight = FontWeight.SemiBold, color = TaggoTheme.colors.textPrimary)
                Text(
                    text = body,
                    color = TaggoTheme.colors.textSecondary,
                    fontSize = 11.sp,
                )
            }
        }
    }
}

@Composable
internal fun SearchEmptyState(
    title: String,
    body: String,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TaggoTheme.colors.panelBackgroundSoft),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(TaggoTheme.colors.panelBackground),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = TaggoTheme.colors.textSecondary,
                    modifier = Modifier.size(24.dp),
                )
            }
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

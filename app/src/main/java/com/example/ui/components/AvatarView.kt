package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.BlueSecondary
import com.example.ui.theme.BlueTertiary

val AVATAR_PRESETS = listOf(
    "preset_1" to listOf(Color(0xFF0062FF), Color(0xFF00D2FF)),
    "preset_2" to listOf(Color(0xFF1D4ED8), Color(0xFF38BDF8)),
    "preset_3" to listOf(Color(0xFF0369A1), Color(0xFF67E8F9)),
    "preset_4" to listOf(Color(0xFF0F172A), Color(0xFF2563EB)),
    "preset_5" to listOf(Color(0xFF0284C7), Color(0xFF93C5FD))
)

@Composable
fun UserAvatar(
    photoUriOrPreset: String,
    username: String,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val clickModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    val isUri = photoUriOrPreset.startsWith("content://") ||
            photoUriOrPreset.startsWith("file://") ||
            photoUriOrPreset.startsWith("http")

    Box(
        modifier = clickModifier
            .size(size)
            .clip(CircleShape)
            .border(2.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (isUri) {
            AsyncImage(
                model = photoUriOrPreset,
                contentDescription = "Photo de profil de $username",
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            val presetColors = AVATAR_PRESETS.firstOrNull { it.first == photoUriOrPreset }?.second
                ?: listOf(BluePrimary, BlueSecondary)

            Box(
                modifier = Modifier
                    .size(size)
                    .background(Brush.linearGradient(presetColors)),
                contentAlignment = Alignment.Center
            ) {
                if (username.isNotBlank()) {
                    Text(
                        text = username.take(1).uppercase(),
                        color = Color.White,
                        fontSize = (size.value * 0.42f).sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(size * 0.55f)
                    )
                }
            }
        }
    }
}

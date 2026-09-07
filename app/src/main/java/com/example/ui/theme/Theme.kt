package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme =
  lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = BluePrimaryContainer,
    onPrimaryContainer = BlueOnPrimaryContainer,
    secondary = BlueSecondary,
    onSecondary = Color.White,
    secondaryContainer = BlueSecondaryContainer,
    onSecondaryContainer = BlueOnSecondaryContainer,
    tertiary = BlueTertiary,
    onTertiary = Color.White,
    tertiaryContainer = BlueTertiaryContainer,
    background = WhiteBackground,
    onBackground = SlateTextPrimary,
    surface = WhiteSurface,
    onSurface = SlateTextPrimary,
    surfaceVariant = WhiteSurfaceVariant,
    onSurfaceVariant = SlateTextSecondary,
    outline = SlateBorder,
    outlineVariant = Color(0xFFEEF2F6),
  )

private val DarkColorScheme =
  darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF002B66),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = Color(0xFF38BDF8),
    onSecondary = Color(0xFF00364F),
    secondaryContainer = Color(0xFF075985),
    onSecondaryContainer = Color(0xFFE0F2FE),
    background = Color(0xFF0B1329),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF111D3D),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155),
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Default to luminous modern white & blue as requested
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}


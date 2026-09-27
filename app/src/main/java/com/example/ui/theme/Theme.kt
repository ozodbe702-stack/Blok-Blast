package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BlockBlastColorScheme = darkColorScheme(
    primary = NeonGold,
    onPrimary = Color(0xFF1A103C),
    primaryContainer = Color(0xFF423075),
    onPrimaryContainer = Color(0xFFFFF3CD),
    secondary = ElectricCyan,
    onSecondary = Color(0xFF00262B),
    secondaryContainer = Color(0xFF163D5C),
    onSecondaryContainer = Color(0xFFB8F8FF),
    tertiary = VibrantMagenta,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF56183D),
    onTertiaryContainer = Color(0xFFFFD9E3),
    background = ArcadeBgDeep,
    onBackground = Color(0xFFF6F2FF),
    surface = ArcadeSurface,
    onSurface = Color(0xFFF6F2FF),
    surfaceVariant = ArcadeSurfaceVariant,
    onSurfaceVariant = Color(0xFFD0C4F2),
    outline = ArcadeCellBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BlockBlastColorScheme,
        typography = Typography,
        content = content
    )
}

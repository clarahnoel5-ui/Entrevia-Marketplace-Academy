package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Premium High-Contrast Dark Scheme (Navy & Gold Highlight)
private val DarkColorScheme = darkColorScheme(
    primary = LuxuryGold,
    onPrimary = LuxuryNavy,
    primaryContainer = LuxuryNavyLight,
    onPrimaryContainer = LuxuryGoldLight,
    secondary = LuxuryGoldLight,
    onSecondary = LuxuryNavy,
    tertiary = LuxuryBeigeAccent,
    onTertiary = LuxuryTextDark,
    background = LuxuryNavy,
    onBackground = LuxuryTextLight,
    surface = LuxuryNavyCard,
    onSurface = LuxuryTextLight,
    surfaceVariant = LuxuryNavyLight,
    onSurfaceVariant = LuxuryBeige,
    outline = LuxuryGoldDark
)

// Premium Clean Light Scheme (Ivory, Navy Accent, and Gold)
private val LightColorScheme = lightColorScheme(
    primary = LuxuryNavy,
    onPrimary = Color.White,
    primaryContainer = LuxuryBeigeAccent,
    onPrimaryContainer = LuxuryNavy,
    secondary = LuxuryGoldDark,
    onSecondary = Color.White,
    tertiary = LuxuryNavyLight,
    onTertiary = Color.White,
    background = LuxuryBeige,
    onBackground = LuxuryTextDark,
    surface = Color.White,
    onSurface = LuxuryTextDark,
    surfaceVariant = LuxuryBeigeAccent,
    onSurfaceVariant = LuxuryTextDark,
    outline = LuxuryGoldDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep styling consistent by defaulting dynamic colors to false
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

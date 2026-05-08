package com.example.alleycat.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// AlleyCat always uses dark theme for the arcade aesthetic
private val AlleyCatColorScheme = darkColorScheme(
    primary = NeonCyan,
    secondary = NeonMagenta,
    tertiary = NeonGreen,
    background = NightDark,
    surface = SurfaceDark,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    error = DangerRed,
    onError = Color.White
)

@Composable
fun AlleyCatTheme(
    content: @Composable () -> Unit
) {
    // Status bar and navigation bar colors are handled by enableEdgeToEdge() in MainActivity
    // and the window background color set in themes.xml
    MaterialTheme(
        colorScheme = AlleyCatColorScheme,
        typography = Typography,
        content = content
    )
}

package com.example.alleycat.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AlleyCatColorScheme = darkColorScheme(
    primary = SunsetOrange,
    secondary = GraffitiPink,
    tertiary = GraffitiLime,
    background = AlleyDark,
    surface = SurfaceDark,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = WarmCream,
    onSurface = WarmCream,
    error = DangerRed,
    onError = Color.White
)

@Composable
fun AlleyCatTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AlleyCatColorScheme,
        typography = Typography,
        content = content
    )
}

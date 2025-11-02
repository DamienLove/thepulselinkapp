package com.pulselink.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF5E6FFF),
    secondary = Color(0xFFFFEA00),
    surface = Color(0xFF0F101A),
    onSurface = Color(0xFFE8EAFF)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF1A237E),
    secondary = Color(0xFFFFEA00),
    surface = Color(0xFFF4F4FF),
    onSurface = Color(0xFF060713)
)

@Composable
fun PulseLinkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = MaterialTheme.typography,
        content = content
    )
}

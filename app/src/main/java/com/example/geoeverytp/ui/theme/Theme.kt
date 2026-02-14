package com.example.geoeverytp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

private val DarkColorScheme = darkColorScheme(
    primary = White,
    onPrimary = Black,
    secondary = Gray200,
    onSecondary = Black,
    background = Black,
    onBackground = White,
    surface = Gray800,
    onSurface = White,
    error = White,
    onError = Black
)

private val LightColorScheme = lightColorScheme(
    primary = Black,
    onPrimary = White,
    secondary = Gray700,
    onSecondary = White,
    background = White,
    onBackground = Black,
    surface = Gray200,
    onSurface = Black,
    error = Black,
    onError = White
)

/** App theme: black/white; follows system dark/light. */
@Composable
fun GeoEveryTPTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

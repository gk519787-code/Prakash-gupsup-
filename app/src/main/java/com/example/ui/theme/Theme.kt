package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = GupSupRed,
    onPrimary = GupSupBg,
    primaryContainer = GupSupDarkRaised,
    onPrimaryContainer = GupSupDarkText,
    secondary = GupSupBlue,
    onSecondary = GupSupBg,
    background = GupSupDarkBg,
    onBackground = GupSupDarkText,
    surface = GupSupDarkSurface,
    onSurface = GupSupDarkText,
    surfaceVariant = GupSupDarkRaised,
    onSurfaceVariant = GupSupDim,
    outline = GupSupDarkLine
)

private val LightColorScheme = lightColorScheme(
    primary = GupSupRed,
    onPrimary = GupSupSurface,
    primaryContainer = GupSupRaised,
    onPrimaryContainer = GupSupText,
    secondary = GupSupBlue,
    onSecondary = GupSupSurface,
    background = GupSupBg,
    onBackground = GupSupText,
    surface = GupSupSurface,
    onSurface = GupSupText,
    surfaceVariant = GupSupRaised,
    onSurfaceVariant = GupSupMuted,
    outline = GupSupLine
)

@Composable
fun GupSupTheme(
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

package com.nightcatchers.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = GhostPurple,
    onPrimary = OnSurface,
    primaryContainer = MidnightPurple,
    onPrimaryContainer = GhostPurpleLight,
    secondary = SlimeGreen,
    onSecondary = DeepVoid,
    secondaryContainer = SlimeGreenDark,
    tertiary = EctoplasmTeal,
    background = DeepVoid,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariant,
    onBackground = OnSurface,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceMuted,
    error = DangerRed,
)

@Composable
fun NightCatchersTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = NightCatchersTypography,
        content = content,
    )
}

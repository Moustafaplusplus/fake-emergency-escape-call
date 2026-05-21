package com.fakeemergencyescape.call.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryAccent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4F4FF),
    onPrimaryContainer = AppTextAccent,
    secondary = SecondaryAccent,
    onSecondary = Color.White,
    tertiary = TertiaryAccent,
    onTertiary = Color.White,
    background = AppBackground,
    onBackground = AppOnBackground,
    surface = AppSurface,
    onSurface = AppOnSurface,
    surfaceVariant = AppSurfaceVariant,
    onSurfaceVariant = AppOnSurfaceMuted,
    outline = AppOutline,
    error = Color(0xFFFF3B30),
    onError = Color.White,
    errorContainer = Color(0xFFFFE5E5),
    onErrorContainer = Color(0xFF9B1C1C),
)

@Composable
fun FakeEmergencyEscapeCallTheme(
    content: @Composable () -> Unit,
) {
    // App UI is designed for the light palette — keeps text readable on cards & gradient BG.
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
    ) {
        Surface(
            color = colorScheme.background,
            contentColor = colorScheme.onBackground,
        ) {
            content()
        }
    }
}

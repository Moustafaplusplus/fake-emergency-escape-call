package com.fakeemergencyescape.call.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = TertiaryAccent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A66),
    onPrimaryContainer = PrimaryAccentLight,
    secondary = SecondaryAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF3D2560),
    onSecondaryContainer = Color(0xFFE0C4FF),
    tertiary = PrimaryAccent,
    onTertiary = Color(0xFF001A24),
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceMuted,
    outline = DarkOutline,
    outlineVariant = Color(0xFF1F2A40),
    error = Color(0xFFFF453A),
    onError = Color.White,
    errorContainer = Color(0xFF5C1A18),
    onErrorContainer = Color(0xFFFFDAD6),
)

@Composable
fun FakeEmergencyEscapeCallTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        shapes = AppShapes,
    ) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
        ) {
            content()
        }
    }
}

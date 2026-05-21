package com.fakeemergencyescape.call.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryAccentLight,
    onPrimary = Color(0xFF001A26),
    primaryContainer = PrimaryAccentDark,
    onPrimaryContainer = PrimaryAccentLight,
    secondary = SecondaryAccent,
    tertiary = TertiaryAccent,
    background = Color(0xFF0F1419),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF1A1F2E),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF2C3444),
    onSurfaceVariant = Color(0xFFA0B0D4),
    outline = Color(0xFF50617A),
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryAccent,
    onPrimary = AppSurface,
    primaryContainer = PrimaryAccentLight,
    onPrimaryContainer = PrimaryAccentDark,
    secondary = SecondaryAccent,
    onSecondary = AppSurface,
    tertiary = TertiaryAccent,
    background = AppBackground,
    onBackground = AppOnSurface,
    surface = AppSurface,
    onSurface = AppOnSurface,
    surfaceVariant = AppSurfaceVariant,
    onSurfaceVariant = AppOnSurfaceMuted,
    outline = AppOutline,
    error = Color(0xFFFF3B30),
    errorContainer = Color(0xFFFFE5E5),
    onErrorContainer = Color(0xFF8B0000),
)

@Composable
fun FakeEmergencyEscapeCallTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content,
    )
}

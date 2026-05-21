package com.fakeemergencyescape.call.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Samsung / One UI–style dark chrome for incoming and active call screens. */
private val CallDarkScheme = darkColorScheme(
    primary = CallAnswerGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A2F),
    onPrimaryContainer = CallTextPrimary,
    secondary = PrimaryAccentLight,
    background = CallBackgroundTop,
    onBackground = CallTextPrimary,
    surface = CallBackgroundBottom,
    onSurface = CallTextPrimary,
    surfaceVariant = CallControlBg,
    onSurfaceVariant = CallTextSecondary,
    error = CallDeclineRed,
    onError = Color.White,
)

@Composable
fun CallScreenTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CallDarkScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content,
    )
}

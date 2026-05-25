package com.fakeemergencyescape.call.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object BrandGradients {
    val bluePurpleHorizontal: Brush = Brush.horizontalGradient(
        colors = listOf(TertiaryAccent, SecondaryAccent),
    )

    val bluePurpleVertical: Brush = Brush.verticalGradient(
        colors = listOf(GradientBlueStart, TertiaryAccent, SecondaryAccent),
    )

    val fabRadial: Brush = Brush.radialGradient(
        colors = listOf(PrimaryAccentLight, TertiaryAccent, SecondaryAccent),
    )

    val chipSelected: Brush = Brush.horizontalGradient(
        colors = listOf(TertiaryAccent, SecondaryAccent),
    )

    val cardSheen: Brush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.08f),
            Color.Transparent,
        ),
    )

    val screenBackground: Brush = Brush.verticalGradient(
        colors = listOf(DarkBackgroundTop, DarkBackground, DarkBackgroundBottom),
    )
}

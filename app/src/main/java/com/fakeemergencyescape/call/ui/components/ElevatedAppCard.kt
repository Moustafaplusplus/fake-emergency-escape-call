package com.fakeemergencyescape.call.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.fakeemergencyescape.call.ui.theme.BrandGradients
import com.fakeemergencyescape.call.ui.theme.DarkGlassBorder
import com.fakeemergencyescape.call.ui.theme.DarkSurfaceElevated
import com.fakeemergencyescape.call.ui.theme.SecondaryAccent
import com.fakeemergencyescape.call.ui.theme.TertiaryAccent

private val CardShape = RoundedCornerShape(20.dp)

@Composable
fun ElevatedAppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale = remember { Animatable(1f) }
    LaunchedEffect(pressed) {
        scale.animateTo(
            targetValue = if (pressed && onClick != null) 0.98f else 1f,
            animationSpec = spring(stiffness = 500f),
        )
    }
    val elevation = when {
        pressed && onClick != null -> 4.dp
        else -> 12.dp
    }
    val cardBrush = Brush.verticalGradient(
        colors = listOf(
            DarkSurfaceElevated,
            MaterialTheme.colorScheme.surface,
        ),
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .shadow(
                elevation = elevation,
                shape = CardShape,
                ambientColor = Color.Black.copy(alpha = 0.45f),
                spotColor = TertiaryAccent.copy(alpha = 0.2f),
            )
            .clip(CardShape)
            .background(cardBrush)
            .border(1.dp, DarkGlassBorder, CardShape)
            .border(
                width = 1.dp,
                brush = BrandGradients.cardSheen,
                shape = CardShape,
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interaction,
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            ),
    ) {
        Column(content = content)
    }
}

@Composable
fun AppScreenBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val infinite = rememberInfiniteTransition(label = "bg")
    val drift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Reverse),
        label = "drift",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BrandGradients.screenBackground),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { translationX = -60f + drift * 120f; translationY = -40f }
                .background(
                    Brush.radialGradient(
                        colors = listOf(TertiaryAccent.copy(alpha = 0.18f), Color.Transparent),
                        radius = 900f,
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { translationX = 80f - drift * 140f; translationY = 200f }
                .background(
                    Brush.radialGradient(
                        colors = listOf(SecondaryAccent.copy(alpha = 0.14f), Color.Transparent),
                        radius = 750f,
                    ),
                ),
        )
        content()
    }
}

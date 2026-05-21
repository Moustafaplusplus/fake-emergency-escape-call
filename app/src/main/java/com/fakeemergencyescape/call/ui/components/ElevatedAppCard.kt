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
import com.fakeemergencyescape.call.ui.theme.GlassBorder

private val CardShape = RoundedCornerShape(24.dp)

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
        pressed && onClick != null -> 6.dp
        else -> 16.dp
    }
    val cardBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
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
                ambientColor = Color.Black.copy(alpha = 0.12f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            )
            .clip(CardShape)
            .background(cardBrush)
            .border(1.dp, GlassBorder, CardShape)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.7f),
                        Color.Transparent,
                    ),
                ),
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
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Reverse),
        label = "drift",
    )
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background.copy(alpha = 0.92f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                    ),
                ),
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { translationX = -40f + drift * 80f }
                .background(
                    Brush.radialGradient(
                        colors = listOf(primary.copy(alpha = 0.08f), Color.Transparent),
                        radius = 900f,
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { translationX = 60f - drift * 100f; translationY = 120f }
                .background(
                    Brush.radialGradient(
                        colors = listOf(tertiary.copy(alpha = 0.06f), Color.Transparent),
                        radius = 700f,
                    ),
                ),
        )
        content()
    }
}

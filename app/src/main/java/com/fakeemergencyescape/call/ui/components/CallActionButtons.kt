package com.fakeemergencyescape.call.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fakeemergencyescape.call.ui.theme.CallAnswerGreen
import com.fakeemergencyescape.call.ui.theme.CallDeclineRed
import com.fakeemergencyescape.call.ui.theme.CallEndRed
import com.fakeemergencyescape.call.ui.theme.CallTextPrimary
import com.fakeemergencyescape.call.ui.theme.CallTextSecondary

@Composable
fun LargeCallCircleButton(
    icon: ImageVector,
    label: String,
    backgroundColor: Color,
    iconTint: Color,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    size: Dp = 88.dp,
    pulsing: Boolean = false,
    pulseDelayMs: Int = 0,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val pressScale = remember { Animatable(1f) }
    LaunchedEffect(pressed) {
        pressScale.animateTo(if (pressed) 0.9f else 1f, spring(stiffness = 600f))
    }
    val elevation = if (pressed) 8.dp else 22.dp

    val infinite = rememberInfiniteTransition(label = "call_action_pulse")
    val pulseScale by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(pulseDelayMs),
        ),
        label = "pulse_scale",
    )
    val ringScale by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(pulseDelayMs),
        ),
        label = "ring_scale",
    )
    val ringAlpha by infinite.animateFloat(
        initialValue = 0.42f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(pulseDelayMs),
        ),
        label = "ring_alpha",
    )
    val showPulse = pulsing && !pressed
    val animatedScale = if (showPulse) pulseScale * pressScale.value else pressScale.value

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier,
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (showPulse) {
                Box(
                    modifier = Modifier
                        .size(size)
                        .graphicsLayer {
                            scaleX = ringScale
                            scaleY = ringScale
                            alpha = ringAlpha
                        }
                        .clip(CircleShape)
                        .background(backgroundColor.copy(alpha = 0.55f)),
                )
            }
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                    }
                    .size(size)
                .shadow(elevation, CircleShape, spotColor = backgroundColor.copy(alpha = 0.65f))
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            backgroundColor.copy(alpha = 1f),
                            backgroundColor.copy(alpha = 0.85f),
                        ),
                    ),
                )
                .then(
                    if (onClick != null) {
                        Modifier.clickable(
                            interactionSource = interaction,
                            indication = ripple(bounded = true, radius = size / 2),
                            onClick = onClick,
                        )
                    } else {
                        Modifier
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(size * 0.45f),
                )
            }
        }
        Text(
            text = label,
            color = CallTextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun EndCallButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale = remember { Animatable(1f) }
    LaunchedEffect(pressed) {
        scale.animateTo(if (pressed) 0.9f else 1f, spring(stiffness = 600f))
    }
    val size = 80.dp
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .size(size)
            .shadow(if (pressed) 8.dp else 20.dp, CircleShape, spotColor = CallEndRed.copy(alpha = 0.65f))
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(CallEndRed, CallEndRed.copy(alpha = 0.88f)),
                ),
            )
            .clickable(
                interactionSource = interaction,
                indication = ripple(bounded = true),
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.CallEnd,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(34.dp),
        )
    }
}

@Composable
fun CallControlGridButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    enabled: Boolean = true,
) {
    val bg = if (active) Color.White.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.15f)
    val tint = if (enabled) CallTextPrimary else CallTextSecondary.copy(alpha = 0.45f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(bg)
                .shadow(
                    elevation = if (active) 8.dp else 0.dp,
                    shape = CircleShape,
                    spotColor = Color.White.copy(alpha = 0.2f),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(28.dp),
            )
        }
        Text(
            text = label,
            color = if (enabled) CallTextSecondary else CallTextSecondary.copy(alpha = 0.4f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

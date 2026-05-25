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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fakeemergencyescape.call.ui.theme.BrandGradients
import com.fakeemergencyescape.call.ui.theme.CallAnswerGreen
import com.fakeemergencyescape.call.ui.theme.DarkGlassBorder
import com.fakeemergencyescape.call.ui.theme.DarkOutline
import com.fakeemergencyescape.call.ui.theme.DarkSurfaceVariant
import com.fakeemergencyescape.call.ui.theme.SecondaryAccent
import com.fakeemergencyescape.call.ui.theme.TertiaryAccent

@Composable
fun Primary3DButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    success: Boolean = false,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale = remember { Animatable(1f) }
    LaunchedEffect(pressed, enabled) {
        scale.animateTo(
            targetValue = if (!enabled) 1f else if (pressed) 0.96f else 1f,
            animationSpec = spring(stiffness = 600f),
        )
    }
    val elevation = if (pressed) 4.dp else 14.dp
    val shape = RoundedCornerShape(22.dp)

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .shadow(elevation, shape, spotColor = TertiaryAccent.copy(alpha = 0.45f))
            .clip(shape)
            .background(
                when {
                    !enabled -> Brush.horizontalGradient(
                        colors = listOf(DarkSurfaceVariant, DarkSurfaceVariant),
                    )
                    success -> Brush.verticalGradient(
                        colors = listOf(
                            CallAnswerGreen.copy(alpha = 0.95f),
                            CallAnswerGreen.copy(alpha = 0.82f),
                        ),
                    )
                    else -> BrandGradients.bluePurpleHorizontal
                },
            )
            .border(1.dp, Color.White.copy(alpha = if (enabled) 0.25f else 0.1f), shape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .defaultMinSize(minHeight = 56.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (enabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
        )
    }
}

@Composable
fun Secondary3DButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale = remember { Animatable(1f) }
    LaunchedEffect(pressed, enabled) {
        scale.animateTo(if (!enabled) 1f else if (pressed) 0.97f else 1f, spring(stiffness = 600f))
    }
    val shape = RoundedCornerShape(20.dp)
    val elevation = if (pressed) 2.dp else 8.dp

    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .shadow(elevation, shape, spotColor = TertiaryAccent.copy(alpha = 0.15f))
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.5.dp, TertiaryAccent.copy(alpha = 0.45f), shape)
            .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick)
            .defaultMinSize(minHeight = 52.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        )
    }
}

@Composable
fun Chip3D(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale = remember { Animatable(1f) }
    LaunchedEffect(pressed) {
        scale.animateTo(if (pressed) 0.94f else 1f, tween(120))
    }
    val shape = RoundedCornerShape(16.dp)
    val bg = if (selected) {
        BrandGradients.chipSelected
    } else {
        Brush.linearGradient(
            listOf(
                MaterialTheme.colorScheme.surface,
                DarkSurfaceVariant,
            ),
        )
    }
    val elevation = when {
        !enabled -> 0.dp
        selected && !pressed -> 8.dp
        pressed -> 3.dp
        else -> 4.dp
    }

    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .shadow(elevation, shape, spotColor = if (selected) TertiaryAccent.copy(0.35f) else Color.Black.copy(0.25f))
            .clip(shape)
            .background(bg)
            .then(
                if (selected) {
                    Modifier.border(1.dp, Color.White.copy(alpha = 0.3f), shape)
                } else {
                    Modifier.border(1.dp, DarkOutline.copy(alpha = 0.8f), shape)
                },
            )
            .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
fun Fab3D(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val infinite = rememberInfiniteTransition(label = "fab_pulse")
    val pulse by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Reverse),
        label = "fab_scale",
    )
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val pressScale = remember { Animatable(1f) }
    LaunchedEffect(pressed) {
        pressScale.animateTo(if (pressed) 0.9f else 1f, spring(stiffness = 700f))
    }
    val size = 64.dp

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = pulse * pressScale.value
                scaleY = pulse * pressScale.value
            }
            .shadow(18.dp, CircleShape, spotColor = TertiaryAccent.copy(alpha = 0.55f))
            .size(size)
            .clip(CircleShape)
            .background(BrandGradients.fabRadial)
            .border(2.dp, Color.White.copy(alpha = 0.35f), CircleShape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = Color.White, modifier = Modifier.size(30.dp))
    }
}

@Composable
fun IconButton3D(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    size: Dp = 44.dp,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale = remember { Animatable(1f) }
    LaunchedEffect(pressed) {
        scale.animateTo(if (pressed) 0.88f else 1f, spring(stiffness = 800f))
    }
    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .shadow(if (pressed) 2.dp else 6.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.35f))
            .size(size)
            .clip(CircleShape)
            .background(containerColor)
            .border(0.5.dp, DarkGlassBorder, CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = if (enabled) tint else tint.copy(alpha = 0.4f),
            modifier = Modifier.size(size * 0.48f),
        )
    }
}

@Composable
fun TextButton3D(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale = remember { Animatable(1f) }
    LaunchedEffect(pressed) {
        scale.animateTo(if (pressed) 0.95f else 1f, tween(100))
    }
    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .shadow(if (pressed) 1.dp else 4.dp, RoundedCornerShape(14.dp), spotColor = accent.copy(0.2f))
            .clip(RoundedCornerShape(14.dp))
            .background(accent.copy(alpha = 0.14f))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(text, color = accent, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun GradientOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
) {
    val shape = RoundedCornerShape(18.dp)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale = remember { Animatable(1f) }
    LaunchedEffect(pressed) {
        scale.animateTo(if (pressed) 0.98f else 1f, spring(stiffness = 600f))
    }

    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .clip(shape)
            .background(BrandGradients.bluePurpleHorizontal)
            .padding(1.5.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .defaultMinSize(minHeight = 52.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                leadingIcon?.let {
                    Icon(it, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            trailingIcon?.let {
                Icon(it, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

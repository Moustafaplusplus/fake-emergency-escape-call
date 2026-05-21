package com.fakeemergencyescape.call.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

fun Modifier.staggeredEntrance(index: Int, baseDelayMs: Long = 50L): Modifier = composed {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(index) {
        delay(index * baseDelayMs)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
        )
    }
    graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * 36f
        scaleX = 0.92f + 0.08f * progress.value
        scaleY = 0.92f + 0.08f * progress.value
    }
}

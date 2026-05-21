package com.fakeemergencyescape.call.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fakeemergencyescape.call.R

private val SplashBackground = Color(0xFF0A0A0C)
private val SplashGlow = Color(0x66FF2D2D)

@Composable
fun AnimatedSplashScreen(modifier: Modifier = Modifier) {
    val enterAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        enterAnim.animateTo(1f, tween(durationMillis = 900, easing = FastOutSlowInEasing))
    }
    val enterProgress = enterAnim.value

    val infinite = rememberInfiniteTransition(label = "splash_pulse")
    val pulse by infinite.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_scale",
    )
    val glowAlpha by infinite.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow_alpha",
    )

    val logoScale = (0.82f + 0.18f * enterProgress) * pulse
    val logoAlpha = enterProgress.coerceIn(0f, 1f)
    val textAlpha = ((enterProgress - 0.35f) / 0.65f).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SplashBackground,
                        Color(0xFF141418),
                        SplashBackground,
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(y = 12.dp)
                .alpha(glowAlpha * logoAlpha)
                .background(
                    Brush.radialGradient(
                        colors = listOf(SplashGlow, Color.Transparent),
                    ),
                    shape = RoundedCornerShape(50),
                ),
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 40.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_app_logo),
                contentDescription = null,
                modifier = Modifier
                    .size(132.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha)
                    .shadow(24.dp, RoundedCornerShape(28.dp), spotColor = SplashGlow)
                    .clip(RoundedCornerShape(28.dp)),
                contentScale = ContentScale.Crop,
            )
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .padding(top = 28.dp)
                    .alpha(textAlpha),
            )
        }
    }
}

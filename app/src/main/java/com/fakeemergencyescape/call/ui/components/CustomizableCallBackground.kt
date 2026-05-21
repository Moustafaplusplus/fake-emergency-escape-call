package com.fakeemergencyescape.call.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.fakeemergencyescape.call.domain.model.CallBackgroundAppearance
import com.fakeemergencyescape.call.domain.model.CallBackgroundType

@Composable
fun CustomizableCallBackground(
    appearance: CallBackgroundAppearance,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val overlayColor = Color.Black.copy(alpha = appearance.overlayAlpha.coerceIn(0f, 0.85f))
    val blurMod = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && appearance.blurRadiusDp > 0f) {
        Modifier.blur(appearance.blurRadiusDp.dp)
    } else {
        Modifier
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (appearance.backgroundType) {
            CallBackgroundType.GRADIENT -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    appearance.gradientTopColor,
                                    appearance.gradientBottomColor,
                                    appearance.gradientTopColor.copy(alpha = 0.95f),
                                ),
                            ),
                        ),
                )
            }
            CallBackgroundType.SOLID -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(appearance.solidColor),
                )
            }
            CallBackgroundType.IMAGE -> {
                val path = appearance.backgroundImagePath
                if (!path.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(path)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .then(blurMod),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    DefaultGradientFallback(appearance)
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(overlayColor),
        )
        content()
    }
}

@Composable
private fun DefaultGradientFallback(appearance: CallBackgroundAppearance) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        appearance.gradientTopColor,
                        appearance.gradientBottomColor,
                    ),
                ),
            ),
    )
}

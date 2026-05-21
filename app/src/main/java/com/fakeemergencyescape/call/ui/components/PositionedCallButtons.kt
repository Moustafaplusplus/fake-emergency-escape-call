package com.fakeemergencyescape.call.ui.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fakeemergencyescape.call.R
import com.fakeemergencyescape.call.domain.model.CallAppearanceSettings
import com.fakeemergencyescape.call.ui.theme.CallAnswerGreen
import com.fakeemergencyescape.call.ui.theme.CallDeclineRed

private val ButtonAnchorWidth = 112.dp
private val ButtonAnchorHeight = 124.dp

@Composable
fun PositionedCallButtons(
    appearance: CallAppearanceSettings,
    onDecline: () -> Unit,
    onAnswer: () -> Unit,
    modifier: Modifier = Modifier,
    draggable: Boolean = false,
    onDeclinePositionChange: (Float, Float) -> Unit = { _, _ -> },
    onAnswerPositionChange: (Float, Float) -> Unit = { _, _ -> },
) {
    val declineLabel = stringResource(R.string.incoming_decline)
    val answerLabel = stringResource(R.string.incoming_answer)
    val buttonSize = (88 * appearance.buttonSizeScale).dp.coerceIn(64.dp, 108.dp)

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val maxHeightPx = with(density) { maxHeight.toPx() }
        val anchorWidthPx = with(density) { ButtonAnchorWidth.toPx() }
        val anchorHeightPx = with(density) { ButtonAnchorHeight.toPx() }

        fun clampX(x: Float) = x.coerceIn(0.06f, 0.94f)
        fun clampY(y: Float) = y.coerceIn(0.52f, 0.94f)

        fun Modifier.positioned(nx: Float, ny: Float): Modifier {
            val offsetX = with(density) {
                (maxWidthPx * nx - anchorWidthPx / 2f)
                    .coerceIn(0f, maxWidthPx - anchorWidthPx)
                    .toDp()
            }
            val offsetY = with(density) {
                (maxHeightPx * ny - anchorHeightPx / 2f)
                    .coerceIn(0f, maxHeightPx - anchorHeightPx)
                    .toDp()
            }
            return this.offset(x = offsetX, y = offsetY)
        }

        DraggableCallButtonHost(
            modifier = Modifier,
            positionedModifier = { nx, ny -> Modifier.positioned(nx, ny) },
            draggable = draggable,
            normalizedX = appearance.declineButtonX,
            normalizedY = appearance.declineButtonY,
            maxWidthPx = maxWidthPx,
            maxHeightPx = maxHeightPx,
            clampX = ::clampX,
            clampY = ::clampY,
            onPositionChange = { x, y -> onDeclinePositionChange(clampX(x), clampY(y)) },
        ) {
            LargeCallCircleButton(
                icon = Icons.Rounded.CallEnd,
                label = declineLabel,
                backgroundColor = CallDeclineRed,
                iconTint = androidx.compose.ui.graphics.Color.White,
                size = buttonSize,
                onClick = if (draggable) null else onDecline,
                pulsing = !draggable,
                pulseDelayMs = 0,
            )
        }

        DraggableCallButtonHost(
            modifier = Modifier,
            positionedModifier = { nx, ny -> Modifier.positioned(nx, ny) },
            draggable = draggable,
            normalizedX = appearance.answerButtonX,
            normalizedY = appearance.answerButtonY,
            maxWidthPx = maxWidthPx,
            maxHeightPx = maxHeightPx,
            clampX = ::clampX,
            clampY = ::clampY,
            onPositionChange = { x, y -> onAnswerPositionChange(clampX(x), clampY(y)) },
        ) {
            LargeCallCircleButton(
                icon = Icons.Rounded.Call,
                label = answerLabel,
                backgroundColor = CallAnswerGreen,
                iconTint = androidx.compose.ui.graphics.Color.White,
                size = buttonSize,
                onClick = if (draggable) null else onAnswer,
                pulsing = !draggable,
                pulseDelayMs = 450,
            )
        }
    }
}

@Composable
private fun DraggableCallButtonHost(
    modifier: Modifier,
    positionedModifier: (Float, Float) -> Modifier,
    draggable: Boolean,
    normalizedX: Float,
    normalizedY: Float,
    maxWidthPx: Float,
    maxHeightPx: Float,
    clampX: (Float) -> Float,
    clampY: (Float) -> Float,
    onPositionChange: (Float, Float) -> Unit,
    content: @Composable () -> Unit,
) {
    var posX by remember { mutableFloatStateOf(normalizedX) }
    var posY by remember { mutableFloatStateOf(normalizedY) }
    var isDragging by remember { mutableStateOf(false) }
    val latestX by rememberUpdatedState(normalizedX)
    val latestY by rememberUpdatedState(normalizedY)

    SideEffect {
        if (!isDragging) {
            posX = normalizedX
            posY = normalizedY
        }
    }

    Box(
        modifier = modifier
            .then(positionedModifier(posX, posY))
            .size(ButtonAnchorWidth, ButtonAnchorHeight)
            .then(
                if (draggable) {
                    Modifier.pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                isDragging = true
                                posX = latestX
                                posY = latestY
                            },
                            onDragEnd = {
                                isDragging = false
                                onPositionChange(posX, posY)
                            },
                            onDragCancel = {
                                isDragging = false
                                posX = latestX
                                posY = latestY
                            },
                        ) { change, dragAmount ->
                            change.consume()
                            posX = clampX(posX + dragAmount.x / maxWidthPx)
                            posY = clampY(posY + dragAmount.y / maxHeightPx)
                        }
                    }
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

package com.fakeemergencyescape.call.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import com.fakeemergencyescape.call.domain.model.visibleControls
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.fakeemergencyescape.call.domain.model.ActiveCallAppearanceSettings
import com.fakeemergencyescape.call.domain.model.ActiveCallControlId
import com.fakeemergencyescape.call.domain.model.ActiveCallControlPlacement
import com.fakeemergencyescape.call.ui.active.ActiveCallControlUi

private val ControlAnchorWidth = 88.dp
private val ControlAnchorHeight = 92.dp
private val EndCallAnchorWidth = 96.dp
private val EndCallAnchorHeight = 96.dp

@Composable
fun PositionedActiveCallControls(
    appearance: ActiveCallAppearanceSettings,
    controlUi: @Composable (ActiveCallControlId) -> ActiveCallControlUi,
    onControlClick: (ActiveCallControlId) -> Unit,
    endCallLabel: String,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier,
    editable: Boolean = false,
    onControlPositionChange: (ActiveCallControlId, Float, Float) -> Unit = { _, _, _ -> },
    onControlRemove: (ActiveCallControlId) -> Unit = {},
    onEndCallPositionChange: (Float, Float) -> Unit = { _, _ -> },
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val maxHeightPx = with(density) { maxHeight.toPx() }
        val controlWidthPx = with(density) { ControlAnchorWidth.toPx() }
        val controlHeightPx = with(density) { ControlAnchorHeight.toPx() }
        val endWidthPx = with(density) { EndCallAnchorWidth.toPx() }
        val endHeightPx = with(density) { EndCallAnchorHeight.toPx() }

        fun clampControlX(x: Float) = x.coerceIn(0.08f, 0.92f)
        fun clampControlY(y: Float) = y.coerceIn(0.42f, 0.82f)
        fun clampEndX(x: Float) = x.coerceIn(0.2f, 0.8f)
        fun clampEndY(y: Float) = y.coerceIn(0.78f, 0.94f)

        for (placement in appearance.visibleControls()) {
            key(placement.id) {
                val ui = controlUi(placement.id)
                DraggableActiveControlHost(
                    positionedModifier = { nx, ny ->
                        positionedOffset(
                            density = density,
                            maxWidthPx = maxWidthPx,
                            maxHeightPx = maxHeightPx,
                            anchorWidthPx = controlWidthPx,
                            anchorHeightPx = controlHeightPx,
                            nx = nx,
                            ny = ny,
                        )
                    },
                    editable = editable,
                    normalizedX = placement.x,
                    normalizedY = placement.y,
                    maxWidthPx = maxWidthPx,
                    maxHeightPx = maxHeightPx,
                    clampX = ::clampControlX,
                    clampY = ::clampControlY,
                    onPositionChange = { x, y -> onControlPositionChange(placement.id, x, y) },
                    onRemove = { onControlRemove(placement.id) },
                ) {
                    Box(
                        modifier = Modifier.graphicsLayer {
                            scaleX = appearance.controlSizeScale
                            scaleY = appearance.controlSizeScale
                        },
                    ) {
                        CallControlGridButton(
                            icon = ui.icon,
                            label = ui.label,
                            onClick = { if (!editable) onControlClick(placement.id) },
                            active = ui.active,
                            enabled = ui.enabled && !editable,
                        )
                    }
                }
            }
        }

        DraggableActiveControlHost(
            positionedModifier = { nx, ny ->
                positionedOffset(
                    density = density,
                    maxWidthPx = maxWidthPx,
                    maxHeightPx = maxHeightPx,
                    anchorWidthPx = endWidthPx,
                    anchorHeightPx = endHeightPx,
                    nx = nx,
                    ny = ny,
                )
            },
            editable = editable,
            normalizedX = appearance.endCallX,
            normalizedY = appearance.endCallY,
            maxWidthPx = maxWidthPx,
            maxHeightPx = maxHeightPx,
            clampX = ::clampEndX,
            clampY = ::clampEndY,
            onPositionChange = onEndCallPositionChange,
            showRemove = false,
        ) {
            Box(
                modifier = Modifier.graphicsLayer {
                    scaleX = appearance.controlSizeScale
                    scaleY = appearance.controlSizeScale
                },
            ) {
                EndCallButton(
                    label = endCallLabel,
                    onClick = if (editable) ({}) else onEndCall,
                )
            }
        }
    }
}

private fun positionedOffset(
    density: androidx.compose.ui.unit.Density,
    maxWidthPx: Float,
    maxHeightPx: Float,
    anchorWidthPx: Float,
    anchorHeightPx: Float,
    nx: Float,
    ny: Float,
): Modifier {
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
    return Modifier.offset(x = offsetX, y = offsetY)
}

@Composable
private fun DraggableActiveControlHost(
    positionedModifier: (Float, Float) -> Modifier,
    editable: Boolean,
    normalizedX: Float,
    normalizedY: Float,
    maxWidthPx: Float,
    maxHeightPx: Float,
    clampX: (Float) -> Float,
    clampY: (Float) -> Float,
    onPositionChange: (Float, Float) -> Unit,
    onRemove: () -> Unit = {},
    showRemove: Boolean = true,
    content: @Composable () -> Unit,
) {
    var posX by remember { mutableFloatStateOf(normalizedX) }
    var posY by remember { mutableFloatStateOf(normalizedY) }
    var isDragging by remember { mutableStateOf(false) }
    val latestX by rememberUpdatedState(normalizedX)
    val latestY by rememberUpdatedState(normalizedY)
    val scope = rememberCoroutineScope()

    SideEffect {
        if (!isDragging) {
            posX = normalizedX
            posY = normalizedY
        }
    }

    Box(
        modifier = Modifier
            .then(positionedModifier(posX, posY))
            .size(
                if (showRemove) ControlAnchorWidth else EndCallAnchorWidth,
                if (showRemove) ControlAnchorHeight else EndCallAnchorHeight,
            )
            .then(
                if (editable) {
                    Modifier.pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                scope.launch {
                                    isDragging = true
                                    posX = latestX
                                    posY = latestY
                                }
                            },
                            onDragEnd = {
                                scope.launch {
                                    isDragging = false
                                    onPositionChange(posX, posY)
                                }
                            },
                            onDragCancel = {
                                scope.launch {
                                    isDragging = false
                                    posX = latestX
                                    posY = latestY
                                }
                            },
                        ) { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                posX = clampX(posX + dragAmount.x / maxWidthPx)
                                posY = clampY(posY + dragAmount.y / maxHeightPx)
                            }
                        }
                    }
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
        if (editable && showRemove) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(26.dp)
                    .offset(x = 6.dp, y = (-6).dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.55f),
                    contentColor = Color.White,
                ),
            ) {
                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
            }
        }
    }
}

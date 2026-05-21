package com.fakeemergencyescape.call.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.fakeemergencyescape.call.ui.theme.CallBackgroundBottom
import com.fakeemergencyescape.call.ui.theme.CallBackgroundTop

@Composable
fun CallScreenBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CallBackgroundTop,
                        CallBackgroundBottom,
                        CallBackgroundTop.copy(alpha = 0.95f),
                    ),
                ),
            ),
        content = content,
    )
}

package com.fakeemergencyescape.call.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
fun IncomingCallActions(
    onDecline: () -> Unit,
    onAnswer: () -> Unit,
    declineLabel: String,
    answerLabel: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LargeCallCircleButton(
            icon = Icons.Rounded.CallEnd,
            label = declineLabel,
            backgroundColor = CallDeclineRed,
            iconTint = Color.White,
            size = 80.dp,
            onClick = onDecline,
        )
        LargeCallCircleButton(
            icon = Icons.Rounded.Call,
            label = answerLabel,
            backgroundColor = CallAnswerGreen,
            iconTint = Color.White,
            size = 80.dp,
            onClick = onAnswer,
        )
    }
}

@Composable
fun LargeCallCircleButton(
    icon: ImageVector,
    label: String,
    backgroundColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .shadow(16.dp, CircleShape, spotColor = backgroundColor.copy(alpha = 0.6f))
                .clip(CircleShape)
                .background(backgroundColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, radius = size / 2),
                    onClick = onClick,
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
    Box(
        modifier = modifier
            .size(76.dp)
            .shadow(16.dp, CircleShape, spotColor = CallEndRed.copy(alpha = 0.6f))
            .clip(CircleShape)
            .background(CallEndRed)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
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

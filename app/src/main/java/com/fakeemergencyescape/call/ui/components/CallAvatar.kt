package com.fakeemergencyescape.call.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fakeemergencyescape.call.ui.theme.CallAvatarRing
import com.fakeemergencyescape.call.ui.theme.PrimaryAccent
import com.fakeemergencyescape.call.ui.theme.PrimaryAccentLight

@Composable
fun CallAvatar(
    initials: String,
    modifier: Modifier = Modifier,
    size: Dp = Dp.Unspecified,
    backgroundColor: Color? = null,
    contentColor: Color = Color.White,
    showRing: Boolean = false,
) {
    val avatarSize = if (size == Dp.Unspecified) Modifier else Modifier.size(size)
    
    val bgModifier = if (backgroundColor != null) {
        Modifier.background(color = backgroundColor, shape = CircleShape)
    } else {
        Modifier.background(
            brush = Brush.linearGradient(
                colors = listOf(PrimaryAccent, PrimaryAccentLight),
            ),
            shape = CircleShape,
        )
    }

    Box(
        modifier = modifier
            .then(avatarSize)
            .shadow(if (showRing) 16.dp else 8.dp, CircleShape)
            .clip(CircleShape)
            .then(bgModifier)
            .then(
                if (showRing) {
                    Modifier.border(3.dp, CallAvatarRing, CircleShape)
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            color = contentColor,
            fontSize = if (size != Dp.Unspecified) (size.value * 0.36f).sp else 28.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

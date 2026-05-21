package com.fakeemergencyescape.call.domain.model

import androidx.compose.ui.graphics.Color

interface CallBackgroundAppearance {
    val backgroundType: CallBackgroundType
    val gradientTopArgb: Long
    val gradientBottomArgb: Long
    val solidColorArgb: Long
    val backgroundImagePath: String?
    val blurRadiusDp: Float
    val overlayAlpha: Float

    val gradientTopColor: Color get() = Color(gradientTopArgb.toInt())
    val gradientBottomColor: Color get() = Color(gradientBottomArgb.toInt())
    val solidColor: Color get() = Color(solidColorArgb.toInt())
}

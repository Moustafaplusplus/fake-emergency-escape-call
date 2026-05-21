package com.fakeemergencyescape.call.domain.model

import androidx.compose.ui.graphics.Color
import com.fakeemergencyescape.call.ui.theme.CallBackgroundBottom
import com.fakeemergencyescape.call.ui.theme.CallBackgroundTop

enum class CallBackgroundType {
    GRADIENT,
    SOLID,
    IMAGE,
}

data class CallAppearanceSettings(
    override val backgroundType: CallBackgroundType = CallBackgroundType.GRADIENT,
    override val gradientTopArgb: Long = 0xFF0A1428L, // Using literal value to avoid ExceptionInInitializerError
    override val gradientBottomArgb: Long = 0xFF1F2D47L,
    override val solidColorArgb: Long = 0xFF0A1428L,
    override val backgroundImagePath: String? = null,
    override val blurRadiusDp: Float = 0f,
    override val overlayAlpha: Float = 0.4f,
    val declineButtonX: Float = 0.28f,
    val declineButtonY: Float = 0.82f,
    val answerButtonX: Float = 0.72f,
    val answerButtonY: Float = 0.82f,
    val buttonSizeScale: Float = 1f,
    val callerNameScale: Float = 1f,
    val avatarScale: Float = 1f,
    val showMobileLabel: Boolean = true,
) : CallBackgroundAppearance

fun Color.toArgbLong(): Long = this.value.toLong()

data class CallColorPreset(
    val id: String,
    val label: String,
    val top: Long,
    val bottom: Long,
)

val DefaultCallAppearance = CallAppearanceSettings()

val CallColorPresets = listOf(
    CallColorPreset("classic", "Classic navy", 0xFF0A1428, 0xFF1F2D47),
    CallColorPreset("midnight", "Midnight", 0xFF0D0D12, 0xFF1A1A2E),
    CallColorPreset("purple", "Purple dusk", 0xFF1A0A2E, 0xFF3D1F5C),
    CallColorPreset("ocean", "Ocean", 0xFF0A1F2E, 0xFF134E6F),
    CallColorPreset("forest", "Forest", 0xFF0A1F14, 0xFF1B4332),
    CallColorPreset("ember", "Ember", 0xFF2D0A0A, 0xFF5C1F1F),
)

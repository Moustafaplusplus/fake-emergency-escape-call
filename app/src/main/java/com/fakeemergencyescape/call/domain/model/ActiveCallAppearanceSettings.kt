package com.fakeemergencyescape.call.domain.model

import com.fakeemergencyescape.call.ui.theme.CallBackgroundBottom
import com.fakeemergencyescape.call.ui.theme.CallBackgroundTop

data class ActiveCallAppearanceSettings(
    override val backgroundType: CallBackgroundType = CallBackgroundType.GRADIENT,
    override val gradientTopArgb: Long = 0xFF0A1428L, // Using literal value to avoid ExceptionInInitializerError
    override val gradientBottomArgb: Long = 0xFF1F2D47L,
    override val solidColorArgb: Long = 0xFF0A1428L,
    override val backgroundImagePath: String? = null,
    override val blurRadiusDp: Float = 0f,
    override val overlayAlpha: Float = 0.4f,
    val controls: List<ActiveCallControlPlacement> = listOf(
        ActiveCallControlPlacement(ActiveCallControlId.MUTE, 0.18f, 0.58f),
        ActiveCallControlPlacement(ActiveCallControlId.SPEAKER, 0.5f, 0.58f),
        ActiveCallControlPlacement(ActiveCallControlId.KEYPAD, 0.82f, 0.58f),
        ActiveCallControlPlacement(ActiveCallControlId.BLUETOOTH, 0.15f, 0.72f),
        ActiveCallControlPlacement(ActiveCallControlId.HOLD, 0.38f, 0.72f),
        ActiveCallControlPlacement(ActiveCallControlId.REPLAY, 0.62f, 0.72f),
        ActiveCallControlPlacement(ActiveCallControlId.MORE, 0.85f, 0.72f),
    ),
    val endCallX: Float = 0.5f,
    val endCallY: Float = 0.88f,
    val controlSizeScale: Float = 1f,
    val callerNameScale: Float = 1f,
    val avatarScale: Float = 1f,
) : CallBackgroundAppearance

val DefaultActiveCallAppearance = ActiveCallAppearanceSettings()

fun ActiveCallAppearanceSettings.visibleControls(): List<ActiveCallControlPlacement> =
    controls.filter { it.visible }

fun ActiveCallAppearanceSettings.hiddenControlIds(): List<ActiveCallControlId> =
    controls.filter { !it.visible }.map { it.id }

fun ActiveCallAppearanceSettings.withControl(
    id: ActiveCallControlId,
    transform: (ActiveCallControlPlacement) -> ActiveCallControlPlacement,
): ActiveCallAppearanceSettings {
    val updated = controls.map { if (it.id == id) transform(it) else it }
    return copy(controls = updated)
}

fun encodeActiveControls(controls: List<ActiveCallControlPlacement>): String =
    controls.joinToString(";") { "${it.id.name},${it.x},${it.y},${if (it.visible) 1 else 0}" }

fun decodeActiveControls(raw: String?): List<ActiveCallControlPlacement> {
    val defaultControls = listOf(
        ActiveCallControlPlacement(ActiveCallControlId.MUTE, 0.18f, 0.58f),
        ActiveCallControlPlacement(ActiveCallControlId.SPEAKER, 0.5f, 0.58f),
        ActiveCallControlPlacement(ActiveCallControlId.KEYPAD, 0.82f, 0.58f),
        ActiveCallControlPlacement(ActiveCallControlId.BLUETOOTH, 0.15f, 0.72f),
        ActiveCallControlPlacement(ActiveCallControlId.HOLD, 0.38f, 0.72f),
        ActiveCallControlPlacement(ActiveCallControlId.REPLAY, 0.62f, 0.72f),
        ActiveCallControlPlacement(ActiveCallControlId.MORE, 0.85f, 0.72f),
    )
    if (raw.isNullOrBlank()) return defaultControls
    return raw.split(";").mapNotNull { part ->
        val pieces = part.split(",")
        if (pieces.size < 4) return@mapNotNull null
        val id = runCatching { ActiveCallControlId.valueOf(pieces[0]) }.getOrNull() ?: return@mapNotNull null
        ActiveCallControlPlacement(
            id = id,
            x = pieces[1].toFloatOrNull() ?: return@mapNotNull null,
            y = pieces[2].toFloatOrNull() ?: return@mapNotNull null,
            visible = pieces[3] == "1",
        )
    }.ifEmpty { defaultControls }
}

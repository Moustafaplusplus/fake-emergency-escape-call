package com.fakeemergencyescape.call.domain.model

enum class ActiveCallControlId {
    MUTE,
    SPEAKER,
    KEYPAD,
    BLUETOOTH,
    HOLD,
    REPLAY,
    MORE,
}

data class ActiveCallControlPlacement(
    val id: ActiveCallControlId,
    val x: Float,
    val y: Float,
    val visible: Boolean = true,
)

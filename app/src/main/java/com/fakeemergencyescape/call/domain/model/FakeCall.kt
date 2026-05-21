package com.fakeemergencyescape.call.domain.model

data class FakeCall(
    val id: String,
    val callerName: String,
    val callerPhotoUri: String?,
    val message: String,
    val scheduledAtMillis: Long,
    val ringtoneUri: String?,
    val voiceLocale: String,
    val speechRate: Float,
    val pitch: Float,
    val vibrationEnabled: Boolean,
    val status: CallStatus,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)

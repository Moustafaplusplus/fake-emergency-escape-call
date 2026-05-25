package com.fakeemergencyescape.call.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fakeemergencyescape.call.domain.model.CallStatus
import com.fakeemergencyescape.call.domain.model.MessageType

@Entity(
    tableName = "fake_calls",
    indices = [
        Index(value = ["scheduledAtMillis"]),
        Index(value = ["status"]),
    ],
)
data class FakeCallEntity(
    @PrimaryKey val id: String,
    val callerName: String,
    val callerPhotoUri: String?,
    val message: String,
    val scriptJson: String? = null,
    val messageType: MessageType = MessageType.TEXT,
    val voiceMessageUri: String? = null,
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

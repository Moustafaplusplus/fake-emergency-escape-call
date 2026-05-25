package com.fakeemergencyescape.call.data.local

import com.fakeemergencyescape.call.domain.model.CallTemplate
import com.fakeemergencyescape.call.domain.model.FakeCall

fun FakeCallEntity.toDomain(): FakeCall = FakeCall(
    id = id,
    callerName = callerName,
    callerPhotoUri = callerPhotoUri,
    message = message,
    scriptJson = scriptJson,
    messageType = messageType,
    voiceMessageUri = voiceMessageUri,
    scheduledAtMillis = scheduledAtMillis,
    ringtoneUri = ringtoneUri,
    voiceLocale = voiceLocale,
    speechRate = speechRate,
    pitch = pitch,
    vibrationEnabled = vibrationEnabled,
    status = status,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
)

fun FakeCall.toEntity(): FakeCallEntity = FakeCallEntity(
    id = id,
    callerName = callerName,
    callerPhotoUri = callerPhotoUri,
    message = message,
    scriptJson = scriptJson,
    messageType = messageType,
    voiceMessageUri = voiceMessageUri,
    scheduledAtMillis = scheduledAtMillis,
    ringtoneUri = ringtoneUri,
    voiceLocale = voiceLocale,
    speechRate = speechRate,
    pitch = pitch,
    vibrationEnabled = vibrationEnabled,
    status = status,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
)

fun TemplateEntity.toDomain(): CallTemplate = CallTemplate(
    id = id,
    category = category,
    title = title,
    message = message,
    scriptJson = scriptJson,
    suggestedCallerName = suggestedCallerName,
)

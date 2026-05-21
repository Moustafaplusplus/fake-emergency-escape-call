package com.fakeemergencyescape.call.data.repository

import com.fakeemergencyescape.call.data.local.CallHistoryDao
import com.fakeemergencyescape.call.data.local.CallHistoryEntity
import com.fakeemergencyescape.call.data.local.DatabaseSeeder
import com.fakeemergencyescape.call.data.local.FakeCallDao
import com.fakeemergencyescape.call.data.local.TemplateDao
import com.fakeemergencyescape.call.data.local.toDomain
import com.fakeemergencyescape.call.data.local.toEntity
import com.fakeemergencyescape.call.domain.model.CallStatus
import com.fakeemergencyescape.call.domain.model.CallTemplate
import com.fakeemergencyescape.call.domain.model.FakeCall
import com.fakeemergencyescape.call.domain.scheduler.FakeCallScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeCallRepository @Inject constructor(
    private val fakeCallDao: FakeCallDao,
    private val callHistoryDao: CallHistoryDao,
    private val templateDao: TemplateDao,
    private val databaseSeeder: DatabaseSeeder,
    private val fakeCallScheduler: FakeCallScheduler,
) {
    fun canScheduleExactAlarms(): Boolean = fakeCallScheduler.canScheduleExactAlarms()

    fun observeScheduled(): Flow<List<FakeCall>> =
        fakeCallDao.observeScheduled().map { list -> list.map { it.toDomain() } }

    fun observePast(): Flow<List<FakeCall>> =
        fakeCallDao.observePast().map { list -> list.map { it.toDomain() } }

    fun observeTemplates(): Flow<List<CallTemplate>> =
        templateDao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun getById(id: String): FakeCall? =
        fakeCallDao.getById(id)?.toDomain()

    fun observeCall(id: String): Flow<FakeCall?> =
        fakeCallDao.observeById(id).map { it?.toDomain() }

    suspend fun scheduleCall(
        callerName: String,
        message: String,
        scheduledAtMillis: Long,
        vibrationEnabled: Boolean,
        vibrateOnly: Boolean,
        voiceLocale: String = Locale.getDefault().toLanguageTag(),
        speechRate: Float = 1f,
        pitch: Float = 1f,
        existingId: String? = null,
    ): String {
        databaseSeeder.seedTemplatesIfNeeded()
        val now = System.currentTimeMillis()
        val id = existingId ?: UUID.randomUUID().toString()

        existingId?.let { fakeCallScheduler.cancel(it) }

        val entity = FakeCall(
            id = id,
            callerName = callerName.trim(),
            callerPhotoUri = null,
            message = message.trim(),
            scheduledAtMillis = scheduledAtMillis,
            ringtoneUri = if (vibrateOnly) VIBRATE_ONLY_URI else null,
            voiceLocale = voiceLocale,
            speechRate = speechRate,
            pitch = pitch,
            vibrationEnabled = vibrationEnabled,
            status = CallStatus.SCHEDULED,
            createdAtMillis = existingId?.let { fakeCallDao.getById(it)?.createdAtMillis } ?: now,
            updatedAtMillis = now,
        ).toEntity()

        fakeCallDao.upsert(entity)

        if (existingId == null) {
            callHistoryDao.insert(
                CallHistoryEntity(
                    id = UUID.randomUUID().toString(),
                    fakeCallId = id,
                    triggeredAtMillis = null,
                    answeredAtMillis = null,
                    declinedAtMillis = null,
                    completedAtMillis = null,
                    finalStatus = CallStatus.SCHEDULED.name,
                ),
            )
        }

        fakeCallScheduler.schedule(entity.toDomain())
        return id
    }

    /**
     * @return true if the call was SCHEDULED and is now RINGING.
     */
    suspend fun onAlarmTriggered(callId: String): Boolean {
        val call = getById(callId) ?: return false
        if (call.status != CallStatus.SCHEDULED) return false

        val now = System.currentTimeMillis()
        fakeCallDao.updateStatus(callId, CallStatus.RINGING, now)
        callHistoryDao.markTriggered(callId, now, CallStatus.RINGING.name)
        return true
    }

    suspend fun cancelCall(id: String) {
        fakeCallScheduler.cancel(id)
        fakeCallDao.updateStatus(id, CallStatus.CANCELLED)
    }

    suspend fun deleteCall(id: String) {
        fakeCallScheduler.cancel(id)
        fakeCallDao.deleteById(id)
    }

    suspend fun rescheduleAllPendingAlarms() {
        val scheduled = fakeCallDao.getAllScheduled().map { it.toDomain() }
        scheduled.forEach { fakeCallScheduler.schedule(it) }
    }

    suspend fun answerCall(id: String) {
        val now = System.currentTimeMillis()
        fakeCallDao.updateStatus(id, CallStatus.ANSWERED, now)
        callHistoryDao.markAnswered(id, now, CallStatus.ANSWERED.name)
    }

    suspend fun declineCall(id: String) {
        val now = System.currentTimeMillis()
        fakeCallDao.updateStatus(id, CallStatus.DECLINED, now)
        callHistoryDao.markDeclined(id, now, CallStatus.DECLINED.name)
    }

    suspend fun missCall(id: String) {
        val now = System.currentTimeMillis()
        fakeCallDao.updateStatus(id, CallStatus.MISSED, now)
        callHistoryDao.updateFinalStatus(id, CallStatus.MISSED.name)
    }

    suspend fun completeCall(id: String) {
        val now = System.currentTimeMillis()
        fakeCallDao.updateStatus(id, CallStatus.COMPLETED, now)
        callHistoryDao.markCompleted(id, now, CallStatus.COMPLETED.name)
    }

    companion object {
        const val VIBRATE_ONLY_URI = "VIBRATE_ONLY"
    }
}

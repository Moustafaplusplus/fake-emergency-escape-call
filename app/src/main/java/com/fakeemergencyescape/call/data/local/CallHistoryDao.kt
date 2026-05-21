package com.fakeemergencyescape.call.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CallHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: CallHistoryEntity)

    @Query(
        """
        UPDATE call_history 
        SET triggeredAtMillis = :triggeredAtMillis, finalStatus = :finalStatus 
        WHERE fakeCallId = :fakeCallId
        """,
    )
    suspend fun markTriggered(
        fakeCallId: String,
        triggeredAtMillis: Long,
        finalStatus: String,
    )

    @Query(
        """
        UPDATE call_history 
        SET answeredAtMillis = :atMillis, finalStatus = :finalStatus 
        WHERE fakeCallId = :fakeCallId
        """,
    )
    suspend fun markAnswered(fakeCallId: String, atMillis: Long, finalStatus: String)

    @Query(
        """
        UPDATE call_history 
        SET declinedAtMillis = :atMillis, finalStatus = :finalStatus 
        WHERE fakeCallId = :fakeCallId
        """,
    )
    suspend fun markDeclined(fakeCallId: String, atMillis: Long, finalStatus: String)

    @Query(
        """
        UPDATE call_history 
        SET completedAtMillis = :atMillis, finalStatus = :finalStatus 
        WHERE fakeCallId = :fakeCallId
        """,
    )
    suspend fun markCompleted(fakeCallId: String, atMillis: Long, finalStatus: String)

    @Query(
        """
        UPDATE call_history 
        SET finalStatus = :finalStatus 
        WHERE fakeCallId = :fakeCallId
        """,
    )
    suspend fun updateFinalStatus(fakeCallId: String, finalStatus: String)
}

package com.fakeemergencyescape.call.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fakeemergencyescape.call.domain.model.CallStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface FakeCallDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(call: FakeCallEntity)

    @Query("SELECT * FROM fake_calls WHERE id = :id")
    suspend fun getById(id: String): FakeCallEntity?

    @Query("SELECT * FROM fake_calls WHERE id = :id")
    fun observeById(id: String): Flow<FakeCallEntity?>

    @Query(
        """
        SELECT * FROM fake_calls 
        WHERE status = 'SCHEDULED' 
        ORDER BY scheduledAtMillis ASC
        """,
    )
    fun observeScheduled(): Flow<List<FakeCallEntity>>

    @Query(
        """
        SELECT * FROM fake_calls 
        WHERE status IN ('COMPLETED', 'DECLINED', 'MISSED', 'CANCELLED') 
        ORDER BY updatedAtMillis DESC
        """,
    )
    fun observePast(): Flow<List<FakeCallEntity>>

    @Query("UPDATE fake_calls SET status = :status, updatedAtMillis = :now WHERE id = :id")
    suspend fun updateStatus(id: String, status: CallStatus, now: Long = System.currentTimeMillis())

    @Query("DELETE FROM fake_calls WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM fake_calls WHERE status = 'SCHEDULED' ORDER BY scheduledAtMillis ASC")
    suspend fun getAllScheduled(): List<FakeCallEntity>
}

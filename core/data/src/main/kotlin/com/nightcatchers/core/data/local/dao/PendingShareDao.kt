package com.nightcatchers.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nightcatchers.core.data.local.entity.PendingShareEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingShareDao {
    @Query("SELECT * FROM pending_shares WHERE status = 'PENDING_REVIEW' ORDER BY createdAtMs DESC")
    fun observePending(): Flow<List<PendingShareEntity>>

    @Query("SELECT * FROM pending_shares ORDER BY createdAtMs DESC")
    fun observeAll(): Flow<List<PendingShareEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PendingShareEntity)

    @Query("UPDATE pending_shares SET status = :status WHERE id = :shareId")
    suspend fun updateStatus(shareId: String, status: String)

    @Query("UPDATE pending_shares SET status = 'EXPIRED' WHERE status = 'PENDING_REVIEW' AND expiresAtMs < :nowMs")
    suspend fun expireOlderThan(nowMs: Long)
}

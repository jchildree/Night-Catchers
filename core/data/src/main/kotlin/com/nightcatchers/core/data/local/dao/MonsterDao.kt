package com.nightcatchers.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nightcatchers.core.data.local.entity.MonsterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MonsterDao {
    @Query("SELECT * FROM monsters WHERE isReleased = 0 ORDER BY captureDateEpochMs DESC")
    fun observeAll(): Flow<List<MonsterEntity>>

    @Query("SELECT * FROM monsters WHERE id = :id")
    fun observeById(id: String): Flow<MonsterEntity?>

    @Query("SELECT * FROM monsters WHERE id = :id")
    suspend fun getById(id: String): MonsterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MonsterEntity)

    @Query("UPDATE monsters SET isReleased = 1 WHERE id = :id")
    suspend fun markReleased(id: String)
}

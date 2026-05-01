package com.nightcatchers.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nightcatchers.core.data.local.entity.PetStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PetStateDao {
    @Query("SELECT * FROM pet_states WHERE monsterId = :monsterId")
    fun observeByMonsterId(monsterId: String): Flow<PetStateEntity?>

    @Query("SELECT * FROM pet_states WHERE monsterId = :monsterId")
    suspend fun getByMonsterId(monsterId: String): PetStateEntity?

    @Query("SELECT * FROM pet_states")
    fun observeAllForSync(): Flow<List<PetStateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PetStateEntity)
}

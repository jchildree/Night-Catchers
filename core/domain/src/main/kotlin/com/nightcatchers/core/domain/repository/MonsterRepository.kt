package com.nightcatchers.core.domain.repository

import com.nightcatchers.core.domain.model.Monster
import kotlinx.coroutines.flow.Flow

interface MonsterRepository {
    fun observeAll(): Flow<List<Monster>>
    fun observeById(monsterId: String): Flow<Monster?>
    suspend fun save(monster: Monster)
    suspend fun release(monsterId: String)
    suspend fun getById(monsterId: String): Monster?
}

package com.nightcatchers.core.testing

import com.nightcatchers.core.domain.model.Monster
import com.nightcatchers.core.domain.repository.MonsterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeMonsterRepository : MonsterRepository {

    private val monsters = MutableStateFlow<List<Monster>>(emptyList())

    override fun observeAll(): Flow<List<Monster>> = monsters

    override fun observeById(monsterId: String): Flow<Monster?> =
        monsters.map { list -> list.firstOrNull { it.id == monsterId } }

    override suspend fun save(monster: Monster) {
        monsters.update { list ->
            val idx = list.indexOfFirst { it.id == monster.id }
            if (idx >= 0) list.toMutableList().also { it[idx] = monster } else list + monster
        }
    }

    override suspend fun release(monsterId: String) {
        monsters.update { list -> list.map { if (it.id == monsterId) it.copy(isReleased = true) else it } }
    }

    override suspend fun getById(monsterId: String): Monster? =
        monsters.value.firstOrNull { it.id == monsterId }
}

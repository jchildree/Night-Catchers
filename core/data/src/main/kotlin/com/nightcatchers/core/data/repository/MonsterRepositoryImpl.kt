package com.nightcatchers.core.data.repository

import com.nightcatchers.core.common.Dispatcher
import com.nightcatchers.core.common.NightCatchersDispatchers
import com.nightcatchers.core.data.local.dao.MonsterDao
import com.nightcatchers.core.data.local.entity.toDomain
import com.nightcatchers.core.data.local.entity.toEntity
import com.nightcatchers.core.domain.model.Monster
import com.nightcatchers.core.domain.repository.MonsterRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MonsterRepositoryImpl @Inject constructor(
    private val dao: MonsterDao,
    @Dispatcher(NightCatchersDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : MonsterRepository {

    override fun observeAll(): Flow<List<Monster>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeById(monsterId: String): Flow<Monster?> =
        dao.observeById(monsterId).map { it?.toDomain() }

    override suspend fun save(monster: Monster): Unit = withContext(ioDispatcher) {
        dao.insert(monster.toEntity())
    }

    override suspend fun release(monsterId: String): Unit = withContext(ioDispatcher) {
        dao.markReleased(monsterId)
    }

    override suspend fun getById(monsterId: String): Monster? = withContext(ioDispatcher) {
        dao.getById(monsterId)?.toDomain()
    }
}

package com.nightcatchers.core.data.repository

import com.nightcatchers.core.common.Dispatcher
import com.nightcatchers.core.common.NightCatchersDispatchers
import com.nightcatchers.core.data.local.dao.PetStateDao
import com.nightcatchers.core.data.local.entity.toDomain
import com.nightcatchers.core.data.local.entity.toEntity
import com.nightcatchers.core.domain.model.PetInteraction
import com.nightcatchers.core.domain.model.PetState
import com.nightcatchers.core.domain.model.PetStats
import com.nightcatchers.core.domain.repository.PetRepository
import com.nightcatchers.core.domain.usecase.GetMoodStateUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject

class PetRepositoryImpl @Inject constructor(
    private val dao: PetStateDao,
    private val getMoodState: GetMoodStateUseCase,
    @Dispatcher(NightCatchersDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : PetRepository {

    override fun observePetState(monsterId: String): Flow<PetState?> =
        dao.observeByMonsterId(monsterId).map { it?.toDomain() }

    override suspend fun getPetState(monsterId: String): PetState? = withContext(ioDispatcher) {
        dao.getByMonsterId(monsterId)?.toDomain()
    }

    override suspend fun savePetState(state: PetState): Unit = withContext(ioDispatcher) {
        dao.insert(state.toEntity())
    }

    override suspend fun applyInteraction(monsterId: String, interaction: PetInteraction): PetState =
        withContext(ioDispatcher) {
            val current = dao.getByMonsterId(monsterId)?.toDomain()
                ?: error("No pet state for $monsterId")
            val updated = applyInteractionToStats(current.stats, interaction)
            val now = Instant.now()
            val newState = current.copy(
                stats = updated,
                mood = getMoodState(updated),
                lastInteractedAt = now,
                updatedAt = now,
            )
            dao.insert(newState.toEntity())
            newState
        }

    override suspend fun applyDecay(monsterId: String): PetState = withContext(ioDispatcher) {
        val current = dao.getByMonsterId(monsterId)?.toDomain()
            ?: error("No pet state for $monsterId")
        val decayed = current.stats.copy(
            hunger = (current.stats.hunger - 4).coerceAtLeast(0),
            happiness = (current.stats.happiness - 3).coerceAtLeast(0),
            energy = (current.stats.energy - 2).coerceAtLeast(0),
        )
        val now = Instant.now()
        val newState = current.copy(
            stats = decayed,
            mood = getMoodState(decayed),
            updatedAt = now,
        )
        dao.insert(newState.toEntity())
        newState
    }

    private fun applyInteractionToStats(stats: PetStats, interaction: PetInteraction): PetStats =
        when (interaction) {
            PetInteraction.Feed -> stats.copy(
                hunger = (stats.hunger + 25).coerceAtMost(100),
                happiness = (stats.happiness + 5).coerceAtMost(100),
            )
            PetInteraction.Play -> stats.copy(
                happiness = (stats.happiness + 20).coerceAtMost(100),
                energy = (stats.energy - 10).coerceAtLeast(0),
                trust = (stats.trust + 3).coerceAtMost(100),
            )
            PetInteraction.Train -> stats.copy(
                trust = (stats.trust + 8).coerceAtMost(100),
                energy = (stats.energy - 15).coerceAtLeast(0),
                spookiness = (stats.spookiness - 5).coerceAtLeast(0),
            )
            PetInteraction.Story -> stats.copy(
                happiness = (stats.happiness + 10).coerceAtMost(100),
                trust = (stats.trust + 5).coerceAtMost(100),
                energy = (stats.energy - 5).coerceAtLeast(0),
            )
        }

}

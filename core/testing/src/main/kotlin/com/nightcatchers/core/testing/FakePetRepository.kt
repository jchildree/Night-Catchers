package com.nightcatchers.core.testing

import com.nightcatchers.core.domain.model.EvolutionStage
import com.nightcatchers.core.domain.model.Mood
import com.nightcatchers.core.domain.model.PetInteraction
import com.nightcatchers.core.domain.model.PetState
import com.nightcatchers.core.domain.model.PetStats
import com.nightcatchers.core.domain.repository.PetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.Instant

class FakePetRepository : PetRepository {

    private val states = MutableStateFlow<Map<String, PetState>>(emptyMap())

    override fun observePetState(monsterId: String): Flow<PetState?> =
        states.map { it[monsterId] }

    override suspend fun getPetState(monsterId: String): PetState? = states.value[monsterId]

    override suspend fun savePetState(state: PetState) {
        states.update { it + (state.monsterId to state) }
    }

    override suspend fun applyInteraction(monsterId: String, interaction: PetInteraction): PetState {
        val current = states.value[monsterId] ?: defaultState(monsterId)
        val updated = current.copy(
            stats = applyInteractionToStats(current.stats, interaction),
            lastInteractedAt = Instant.now(),
            updatedAt = Instant.now(),
        )
        savePetState(updated)
        return updated
    }

    override suspend fun applyDecay(monsterId: String): PetState {
        val current = states.value[monsterId] ?: defaultState(monsterId)
        val decayed = current.copy(
            stats = current.stats.copy(
                hunger = (current.stats.hunger - 5).coerceAtLeast(0),
                happiness = (current.stats.happiness - 3).coerceAtLeast(0),
                energy = (current.stats.energy - 2).coerceAtLeast(0),
            ),
            updatedAt = Instant.now(),
        )
        savePetState(decayed)
        return decayed
    }

    private fun defaultState(monsterId: String) = PetState(
        monsterId = monsterId,
        stats = PetStats(hunger = 50, happiness = 50, energy = 50, spookiness = 50, trust = 0),
        mood = Mood.CONTENT,
        stage = EvolutionStage.BABY,
        lastInteractedAt = Instant.now(),
        updatedAt = Instant.now(),
    )

    private fun applyInteractionToStats(stats: PetStats, interaction: PetInteraction): PetStats =
        when (interaction) {
            is PetInteraction.Feed -> stats.copy(
                hunger = (stats.hunger + 25).coerceAtMost(100),
                happiness = (stats.happiness + 5).coerceAtMost(100),
            )
            is PetInteraction.Play -> stats.copy(
                happiness = (stats.happiness + 20).coerceAtMost(100),
                energy = (stats.energy - 10).coerceAtLeast(0),
                trust = (stats.trust + 3).coerceAtMost(100),
            )
            is PetInteraction.Train -> stats.copy(
                trust = (stats.trust + 8).coerceAtMost(100),
                energy = (stats.energy - 15).coerceAtLeast(0),
                spookiness = (stats.spookiness - 5).coerceAtLeast(0),
            )
            is PetInteraction.Story -> stats.copy(
                happiness = (stats.happiness + 10).coerceAtMost(100),
                trust = (stats.trust + 5).coerceAtMost(100),
                energy = (stats.energy - 5).coerceAtLeast(0),
            )
            is PetInteraction.Comfort -> stats.copy(
                happiness = (stats.happiness + 15).coerceAtMost(100),
                spookiness = (stats.spookiness - 10).coerceAtLeast(0),
                trust = (stats.trust + 6).coerceAtMost(100),
                energy = (stats.energy - 5).coerceAtLeast(0),
            )
            is PetInteraction.Praise -> stats.copy(
                happiness = (stats.happiness + 12).coerceAtMost(100),
                trust = (stats.trust + 4).coerceAtMost(100),
            )
        }
}

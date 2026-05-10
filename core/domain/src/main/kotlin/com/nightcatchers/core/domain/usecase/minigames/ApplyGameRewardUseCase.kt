package com.nightcatchers.core.domain.usecase.minigames

import com.nightcatchers.core.domain.model.PetState
import com.nightcatchers.core.domain.model.PetStats
import com.nightcatchers.core.domain.model.minigames.GameRewards
import com.nightcatchers.core.domain.model.minigames.GameStatDelta
import com.nightcatchers.core.domain.model.minigames.MiniGameId
import com.nightcatchers.core.domain.repository.PetRepository
import javax.inject.Inject

class ApplyGameRewardUseCase @Inject constructor(
    private val petRepository: PetRepository,
) {
    suspend operator fun invoke(
        monsterId: String,
        game: MiniGameId,
        dailyBonusActive: Boolean = false,
    ): PetState {
        val current = petRepository.getPetState(monsterId) ?: error("Monster $monsterId not found")
        val delta = GameRewards.forGame(game)
        val happinessBonus = if (dailyBonusActive) 5 else 0
        val newStats = current.stats.applyDelta(delta, happinessBonus)
        val updated = current.copy(stats = newStats)
        petRepository.savePetState(updated)
        return updated
    }

    private fun PetStats.applyDelta(delta: GameStatDelta, happinessBonus: Int): PetStats = copy(
        hunger = (hunger + delta.hunger).coerceIn(0, 100),
        happiness = (happiness + delta.happiness + happinessBonus).coerceIn(0, 100),
        energy = (energy + delta.energy).coerceIn(0, 100),
        spookiness = (spookiness + delta.spookiness).coerceIn(0, 100),
        trust = (trust + delta.trust).coerceIn(0, 100),
    )
}

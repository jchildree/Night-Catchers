package com.nightcatchers.core.domain.usecase.minigames

import com.nightcatchers.core.domain.model.PetState
import com.nightcatchers.core.domain.model.minigames.FoodItem
import com.nightcatchers.core.domain.repository.PetRepository
import javax.inject.Inject

class FoodTossUseCase @Inject constructor(
    private val petRepository: PetRepository,
) {
    /** Returns false if the monster is TooFull (hunger >= 90) or not found. */
    suspend fun checkCanPlay(monsterId: String): Boolean {
        val state = petRepository.getPetState(monsterId) ?: return false
        return state.stats.hunger < 90
    }

    /**
     * Applies the Food Toss session result.
     * Stat rewards are proportional to hits out of 3 throws:
     *   - Hunger +25 (scaled by hit ratio)
     *   - Happiness +10 (scaled by hit ratio)
     *   - Energy -10 (always applied)
     *   - SockBall bonus +5 happiness when at least one hit with SOCK_BALL
     */
    suspend fun applyResult(monsterId: String, hits: Int, foodItems: List<FoodItem>): PetState {
        val current = petRepository.getPetState(monsterId) ?: error("Monster not found: $monsterId")
        val ratio = hits.toFloat() / 3f
        val hungerGain = (25 * ratio).toInt().coerceAtLeast(0)
        val happinessGain = (10 * ratio).toInt().coerceAtLeast(0)
        val energyCost = 10
        val sockBonus = if (foodItems.any { it == FoodItem.SOCK_BALL } && hits > 0) 5 else 0
        val newStats = current.stats.copy(
            hunger = (current.stats.hunger + hungerGain).coerceIn(0, 100),
            happiness = (current.stats.happiness + happinessGain + sockBonus).coerceIn(0, 100),
            energy = (current.stats.energy - energyCost).coerceIn(0, 100),
        )
        val updated = current.copy(stats = newStats)
        petRepository.savePetState(updated)
        return updated
    }
}

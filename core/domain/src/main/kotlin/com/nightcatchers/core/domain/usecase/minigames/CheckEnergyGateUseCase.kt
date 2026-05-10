package com.nightcatchers.core.domain.usecase.minigames

import com.nightcatchers.core.domain.model.PetStats
import com.nightcatchers.core.domain.model.minigames.MiniGameId
import com.nightcatchers.core.domain.model.minigames.MiniGameTier
import javax.inject.Inject

class CheckEnergyGateUseCase @Inject constructor() {
    // Returns true if the game is PLAYABLE (not blocked)
    operator fun invoke(game: MiniGameId, stats: PetStats): Boolean {
        // Critical stat overrides — always allow regardless of energy
        if (game == MiniGameId.FOOD_TOSS && stats.hunger < 15) return true
        if (game == MiniGameId.CUDDLE_STORM && stats.trust < 10) return true
        // Skill games need energy >= 25
        if (game.tier == MiniGameTier.SKILL && stats.energy < 25) return false
        // Bonding games need energy >= 10
        if (game.tier == MiniGameTier.BONDING && stats.energy < 10) return false
        // Food Toss is blocked if hunger >= 90 (TooFull)
        if (game == MiniGameId.FOOD_TOSS && stats.hunger >= 90) return false
        return true
    }
}

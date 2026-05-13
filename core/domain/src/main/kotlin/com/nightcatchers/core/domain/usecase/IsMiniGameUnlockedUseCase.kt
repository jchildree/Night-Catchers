package com.nightcatchers.core.domain.usecase

import com.nightcatchers.core.domain.model.MiniGameId
import com.nightcatchers.core.domain.model.MiniGameTier
import com.nightcatchers.core.domain.model.PetStats
import javax.inject.Inject

/**
 * Energy-gating rules from Section 19:
 * - SKILL games require Energy ≥ 25.
 * - BONDING games require Energy ≥ 10, except for critical-stat overrides:
 *   - Food Toss is always unlocked when Hunger < 15 (starving).
 *   - Food Toss is locked when Hunger ≥ 90 (TooFull — no point feeding a full monster).
 *   - Cuddle Storm is always unlocked when Trust < 10 (newly captured monsters).
 *
 * The "Gentle Mode" override that disables all gating lives at the profile layer; this use
 * case only encodes the per-stat rules.
 */
class IsMiniGameUnlockedUseCase @Inject constructor() {

    operator fun invoke(gameId: MiniGameId, stats: PetStats): UnlockState {
        criticalOverride(gameId, stats)?.let { return it }
        val minEnergy = when (gameId.tier) {
            MiniGameTier.SKILL -> SKILL_MIN_ENERGY
            MiniGameTier.BONDING -> BONDING_MIN_ENERGY
        }
        return if (stats.energy >= minEnergy) {
            UnlockState.Unlocked
        } else {
            UnlockState.Locked(reason = LockReason.LowEnergy(required = minEnergy, current = stats.energy))
        }
    }

    private fun criticalOverride(gameId: MiniGameId, stats: PetStats): UnlockState? = when {
        gameId == MiniGameId.FOOD_TOSS && stats.hunger < CRITICAL_HUNGER -> UnlockState.Unlocked
        gameId == MiniGameId.FOOD_TOSS && stats.hunger >= FULL_HUNGER -> UnlockState.Locked(reason = LockReason.TooFull)
        gameId == MiniGameId.CUDDLE_STORM && stats.trust < CRITICAL_TRUST -> UnlockState.Unlocked
        else -> null
    }

    sealed interface UnlockState {
        data object Unlocked : UnlockState
        data class Locked(val reason: LockReason) : UnlockState
    }

    sealed interface LockReason {
        data class LowEnergy(val required: Int, val current: Int) : LockReason
        data object TooFull : LockReason
    }

    private companion object {
        const val SKILL_MIN_ENERGY = 25
        const val BONDING_MIN_ENERGY = 10
        const val CRITICAL_HUNGER = 15
        const val FULL_HUNGER = 90
        const val CRITICAL_TRUST = 10
    }
}

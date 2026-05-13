package com.nightcatchers.core.domain

import com.nightcatchers.core.domain.model.MiniGameId
import com.nightcatchers.core.domain.model.PetStats
import com.nightcatchers.core.domain.usecase.IsMiniGameUnlockedUseCase
import com.nightcatchers.core.domain.usecase.IsMiniGameUnlockedUseCase.LockReason
import com.nightcatchers.core.domain.usecase.IsMiniGameUnlockedUseCase.UnlockState
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class IsMiniGameUnlockedUseCaseTest {

    private val useCase = IsMiniGameUnlockedUseCase()

    @Test
    fun `skill game locked when energy below 25`() {
        val stats = stats(energy = 24)
        val result = useCase(MiniGameId.GHOST_DASH, stats)
        result.shouldBeInstanceOf<UnlockState.Locked>()
        (result.reason as LockReason.LowEnergy).required shouldBe 25
    }

    @Test
    fun `skill game unlocked at energy 25`() {
        useCase(MiniGameId.GHOST_DASH, stats(energy = 25)) shouldBe UnlockState.Unlocked
    }

    @Test
    fun `bonding game locked below energy 10`() {
        val result = useCase(MiniGameId.SPOOK_TAG, stats(energy = 9))
        result.shouldBeInstanceOf<UnlockState.Locked>()
    }

    @Test
    fun `food toss bypasses energy gate when hunger critical`() {
        val stats = stats(energy = 0, hunger = 14)
        useCase(MiniGameId.FOOD_TOSS, stats) shouldBe UnlockState.Unlocked
    }

    @Test
    fun `food toss still gated when hunger above critical threshold`() {
        val stats = stats(energy = 5, hunger = 50)
        val result = useCase(MiniGameId.FOOD_TOSS, stats)
        result.shouldBeInstanceOf<UnlockState.Locked>()
    }

    @Test
    fun `cuddle storm bypasses energy gate when trust critical`() {
        val stats = stats(energy = 0, trust = 5)
        useCase(MiniGameId.CUDDLE_STORM, stats) shouldBe UnlockState.Unlocked
    }

    @Test
    fun `food toss locked when monster is too full`() {
        val result = useCase(MiniGameId.FOOD_TOSS, stats(energy = 50, hunger = 90))
        result.shouldBeInstanceOf<UnlockState.Locked>()
        result.reason shouldBe LockReason.TooFull
    }

    @Test
    fun `food toss unlocked just below full threshold`() {
        useCase(MiniGameId.FOOD_TOSS, stats(energy = 50, hunger = 89)) shouldBe UnlockState.Unlocked
    }

    private fun stats(
        hunger: Int = 50,
        happiness: Int = 50,
        energy: Int = 50,
        spookiness: Int = 50,
        trust: Int = 50,
    ): PetStats = PetStats(hunger, happiness, energy, spookiness, trust)
}

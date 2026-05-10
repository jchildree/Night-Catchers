package com.nightcatchers.core.domain.minigames

import com.nightcatchers.core.domain.model.PetStats
import com.nightcatchers.core.domain.model.minigames.MiniGameId
import com.nightcatchers.core.domain.usecase.minigames.CheckEnergyGateUseCase
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class CheckEnergyGateUseCaseTest {

    private val useCase = CheckEnergyGateUseCase()

    private fun stats(
        hunger: Int = 50,
        happiness: Int = 50,
        energy: Int = 50,
        spookiness: Int = 50,
        trust: Int = 50,
    ) = PetStats(hunger = hunger, happiness = happiness, energy = energy, spookiness = spookiness, trust = trust)

    // ── Skill game energy gate ──────────────────────────────────────────────

    @Test
    fun `skill game is blocked when energy is below 25`() {
        val result = useCase(MiniGameId.GHOST_DASH, stats(energy = 24))
        result shouldBe false
    }

    @Test
    fun `skill game is blocked at exactly 24 energy`() {
        val result = useCase(MiniGameId.SLIME_SORT, stats(energy = 24))
        result shouldBe false
    }

    @Test
    fun `skill game is allowed at exactly 25 energy`() {
        val result = useCase(MiniGameId.GHOST_DASH, stats(energy = 25))
        result shouldBe true
    }

    @Test
    fun `skill game is allowed when energy is above 25`() {
        val result = useCase(MiniGameId.PROTON_WRANGLE, stats(energy = 60))
        result shouldBe true
    }

    // ── Bonding game energy gate ────────────────────────────────────────────

    @Test
    fun `bonding game is blocked when energy is below 10`() {
        val result = useCase(MiniGameId.SPOOK_TAG, stats(energy = 9))
        result shouldBe false
    }

    @Test
    fun `bonding game is allowed at exactly 10 energy`() {
        val result = useCase(MiniGameId.SPOOK_TAG, stats(energy = 10))
        result shouldBe true
    }

    @Test
    fun `bonding game is allowed when energy is above 10`() {
        val result = useCase(MiniGameId.CUDDLE_STORM, stats(energy = 50, trust = 20))
        result shouldBe true
    }

    // ── Food Toss critical override ─────────────────────────────────────────

    @Test
    fun `Food Toss is always allowed when hunger is below 15 even at zero energy`() {
        val result = useCase(MiniGameId.FOOD_TOSS, stats(hunger = 14, energy = 0))
        result shouldBe true
    }

    @Test
    fun `Food Toss override applies at hunger exactly 14`() {
        val result = useCase(MiniGameId.FOOD_TOSS, stats(hunger = 14, energy = 5))
        result shouldBe true
    }

    @Test
    fun `Food Toss does NOT get override at hunger exactly 15`() {
        // hunger = 15 is not < 15, so normal energy gate applies
        val result = useCase(MiniGameId.FOOD_TOSS, stats(hunger = 15, energy = 5))
        result shouldBe false
    }

    // ── Food Toss TooFull block ─────────────────────────────────────────────

    @Test
    fun `Food Toss is blocked when hunger is 90 or above`() {
        val result = useCase(MiniGameId.FOOD_TOSS, stats(hunger = 90, energy = 50))
        result shouldBe false
    }

    @Test
    fun `Food Toss is blocked at hunger exactly 90`() {
        val result = useCase(MiniGameId.FOOD_TOSS, stats(hunger = 90, energy = 100))
        result shouldBe false
    }

    @Test
    fun `Food Toss is allowed when hunger is 89 with sufficient energy`() {
        val result = useCase(MiniGameId.FOOD_TOSS, stats(hunger = 89, energy = 20))
        result shouldBe true
    }

    // ── Cuddle Storm trust override ─────────────────────────────────────────

    @Test
    fun `Cuddle Storm is always allowed when trust is below 10 even at low energy`() {
        val result = useCase(MiniGameId.CUDDLE_STORM, stats(trust = 9, energy = 0))
        result shouldBe true
    }

    @Test
    fun `Cuddle Storm override applies at trust exactly 9`() {
        val result = useCase(MiniGameId.CUDDLE_STORM, stats(trust = 9, energy = 2))
        result shouldBe true
    }

    @Test
    fun `Cuddle Storm does NOT get override at trust exactly 10`() {
        // trust = 10 is not < 10, so normal energy gate applies
        val result = useCase(MiniGameId.CUDDLE_STORM, stats(trust = 10, energy = 5))
        result shouldBe false
    }
}

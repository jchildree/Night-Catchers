package com.nightcatchers.core.domain.minigames

import com.nightcatchers.core.domain.model.EvolutionStage
import com.nightcatchers.core.domain.model.Mood
import com.nightcatchers.core.domain.model.PetState
import com.nightcatchers.core.domain.model.PetStats
import com.nightcatchers.core.domain.model.minigames.GameRewards
import com.nightcatchers.core.domain.model.minigames.MiniGameId
import com.nightcatchers.core.domain.repository.PetRepository
import com.nightcatchers.core.domain.usecase.minigames.ApplyGameRewardUseCase
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class ApplyGameRewardUseCaseTest {

    private val petRepository: PetRepository = mockk()
    private val useCase = ApplyGameRewardUseCase(petRepository)

    private val monsterId = "test-monster-id"

    private fun petState(stats: PetStats) = PetState(
        monsterId = monsterId,
        stats = stats,
        mood = Mood.CONTENT,
        stage = EvolutionStage.BABY,
        lastInteractedAt = Instant.now(),
        updatedAt = Instant.now(),
    )

    private fun stats(
        hunger: Int = 50,
        happiness: Int = 50,
        energy: Int = 70,
        spookiness: Int = 50,
        trust: Int = 20,
    ) = PetStats(hunger = hunger, happiness = happiness, energy = energy, spookiness = spookiness, trust = trust)

    @BeforeEach
    fun setUp() {
        coEvery { petRepository.savePetState(any()) } returns Unit
    }

    // ── FOOD_TOSS ───────────────────────────────────────────────────────────

    @Test
    fun `Food Toss applies correct stat delta`() = runTest {
        val initial = stats(hunger = 50, happiness = 50, energy = 70)
        coEvery { petRepository.getPetState(monsterId) } returns petState(initial)

        val result = useCase(monsterId, MiniGameId.FOOD_TOSS)

        val delta = GameRewards.FOOD_TOSS
        result.stats.hunger shouldBe (initial.hunger + delta.hunger).coerceIn(0, 100)
        result.stats.happiness shouldBe (initial.happiness + delta.happiness).coerceIn(0, 100)
        result.stats.energy shouldBe (initial.energy + delta.energy).coerceIn(0, 100)
    }

    // ── GHOST_DASH ──────────────────────────────────────────────────────────

    @Test
    fun `Ghost Dash applies correct stat delta`() = runTest {
        val initial = stats(happiness = 40, spookiness = 30, trust = 10, energy = 60)
        coEvery { petRepository.getPetState(monsterId) } returns petState(initial)

        val result = useCase(monsterId, MiniGameId.GHOST_DASH)

        val delta = GameRewards.GHOST_DASH
        result.stats.happiness shouldBe (initial.happiness + delta.happiness).coerceIn(0, 100)
        result.stats.spookiness shouldBe (initial.spookiness + delta.spookiness).coerceIn(0, 100)
        result.stats.trust shouldBe (initial.trust + delta.trust).coerceIn(0, 100)
        result.stats.energy shouldBe (initial.energy + delta.energy).coerceIn(0, 100)
    }

    // ── Stat clamping ───────────────────────────────────────────────────────

    @Test
    fun `stats are clamped to 100 maximum`() = runTest {
        val initial = stats(hunger = 95, happiness = 95, energy = 95, spookiness = 95, trust = 95)
        coEvery { petRepository.getPetState(monsterId) } returns petState(initial)

        val result = useCase(monsterId, MiniGameId.FOOD_TOSS)

        result.stats.hunger shouldBe 100
        result.stats.happiness shouldBe 100
    }

    @Test
    fun `stats are clamped to 0 minimum`() = runTest {
        val initial = stats(energy = 5, spookiness = 0)
        coEvery { petRepository.getPetState(monsterId) } returns petState(initial)

        // Cuddle Storm has spookiness -10 and energy -15
        val result = useCase(monsterId, MiniGameId.CUDDLE_STORM)

        result.stats.energy shouldBe 0
        result.stats.spookiness shouldBe 0
    }

    // ── Daily bonus ─────────────────────────────────────────────────────────

    @Test
    fun `daily bonus adds 5 extra happiness`() = runTest {
        val initial = stats(happiness = 50)
        coEvery { petRepository.getPetState(monsterId) } returns petState(initial)

        val withBonus = useCase(monsterId, MiniGameId.FOOD_TOSS, dailyBonusActive = true)
        val withoutBonus = useCase(monsterId, MiniGameId.FOOD_TOSS, dailyBonusActive = false)

        withBonus.stats.happiness shouldBe (withoutBonus.stats.happiness + 5).coerceIn(0, 100)
    }

    @Test
    fun `daily bonus happiness is clamped at 100`() = runTest {
        val initial = stats(happiness = 98)
        coEvery { petRepository.getPetState(monsterId) } returns petState(initial)

        // FOOD_TOSS adds +10 happiness + 5 bonus = +15 total → clamped to 100
        val result = useCase(monsterId, MiniGameId.FOOD_TOSS, dailyBonusActive = true)

        result.stats.happiness shouldBe 100
    }

    @Test
    fun `no daily bonus when dailyBonusActive is false`() = runTest {
        val initial = stats(happiness = 50)
        coEvery { petRepository.getPetState(monsterId) } returns petState(initial)

        val result = useCase(monsterId, MiniGameId.FOOD_TOSS, dailyBonusActive = false)

        val delta = GameRewards.FOOD_TOSS
        result.stats.happiness shouldBe (initial.happiness + delta.happiness).coerceIn(0, 100)
    }

    // ── Repository interaction ──────────────────────────────────────────────

    @Test
    fun `saves updated pet state to repository`() = runTest {
        val initial = stats()
        coEvery { petRepository.getPetState(monsterId) } returns petState(initial)

        useCase(monsterId, MiniGameId.SLIME_SORT)

        coVerify(exactly = 1) { petRepository.savePetState(any()) }
    }

    @Test
    fun `saved state contains monsterId`() = runTest {
        val initial = stats()
        coEvery { petRepository.getPetState(monsterId) } returns petState(initial)

        val savedSlot = slot<PetState>()
        coEvery { petRepository.savePetState(capture(savedSlot)) } returns Unit

        useCase(monsterId, MiniGameId.PROTON_WRANGLE)

        savedSlot.captured.monsterId shouldBe monsterId
    }

    @Test
    fun `throws error when monster not found`() = runTest {
        coEvery { petRepository.getPetState(monsterId) } returns null

        runCatching { useCase(monsterId, MiniGameId.FOOD_TOSS) }
            .isFailure shouldBe true
    }
}

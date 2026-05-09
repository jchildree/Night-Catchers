package com.nightcatchers.core.domain.minigames

import com.nightcatchers.core.domain.model.EvolutionStage
import com.nightcatchers.core.domain.model.Mood
import com.nightcatchers.core.domain.model.PetState
import com.nightcatchers.core.domain.model.PetStats
import com.nightcatchers.core.domain.model.minigames.FoodItem
import com.nightcatchers.core.domain.usecase.minigames.FoodTossUseCase
import com.nightcatchers.core.testing.FakePetRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class FoodTossUseCaseTest {

    private lateinit var repository: FakePetRepository
    private lateinit var useCase: FoodTossUseCase

    @BeforeEach
    fun setUp() {
        repository = FakePetRepository()
        useCase = FoodTossUseCase(repository)
    }

    private fun seedState(monsterId: String, stats: PetStats) {
        val state = PetState(
            monsterId = monsterId,
            stats = stats,
            mood = Mood.CONTENT,
            stage = EvolutionStage.BABY,
            lastInteractedAt = Instant.now(),
            updatedAt = Instant.now(),
        )
        kotlinx.coroutines.runBlocking { repository.savePetState(state) }
    }

    private fun stats(
        hunger: Int = 50,
        happiness: Int = 50,
        energy: Int = 50,
        spookiness: Int = 50,
        trust: Int = 20,
    ) = PetStats(hunger = hunger, happiness = happiness, energy = energy, spookiness = spookiness, trust = trust)

    // ── checkCanPlay ─────────────────────────────────────────────────────────

    @Test
    fun `checkCanPlay returns false when hunger is exactly 90`() = runTest {
        seedState("m1", stats(hunger = 90))
        useCase.checkCanPlay("m1") shouldBe false
    }

    @Test
    fun `checkCanPlay returns false when hunger is above 90`() = runTest {
        seedState("m1", stats(hunger = 95))
        useCase.checkCanPlay("m1") shouldBe false
    }

    @Test
    fun `checkCanPlay returns true when hunger is 89`() = runTest {
        seedState("m1", stats(hunger = 89))
        useCase.checkCanPlay("m1") shouldBe true
    }

    @Test
    fun `checkCanPlay returns false when monster not found`() = runTest {
        useCase.checkCanPlay("unknown-id") shouldBe false
    }

    // ── applyResult — 3 hits ─────────────────────────────────────────────────

    @Test
    fun `applyResult with 3 hits applies full hunger and happiness gain`() = runTest {
        seedState("m1", stats(hunger = 40, happiness = 40, energy = 50))
        val result = useCase.applyResult("m1", hits = 3, foodItems = listOf(FoodItem.GLOW_SNACK))

        result.stats.hunger shouldBe 65     // 40 + 25
        result.stats.happiness shouldBe 50  // 40 + 10
        result.stats.energy shouldBe 40     // 50 - 10
    }

    // ── applyResult — 0 hits ─────────────────────────────────────────────────

    @Test
    fun `applyResult with 0 hits applies only energy cost, no hunger or happiness gain`() = runTest {
        seedState("m1", stats(hunger = 40, happiness = 40, energy = 50))
        val result = useCase.applyResult("m1", hits = 0, foodItems = listOf(FoodItem.BUG))

        result.stats.hunger shouldBe 40     // unchanged (0 * ratio = 0)
        result.stats.happiness shouldBe 40  // unchanged
        result.stats.energy shouldBe 40     // 50 - 10
    }

    // ── applyResult — partial hits ───────────────────────────────────────────

    @Test
    fun `applyResult with 1 hit applies partial gain (ratio 1 of 3)`() = runTest {
        seedState("m1", stats(hunger = 40, happiness = 40, energy = 50))
        val result = useCase.applyResult("m1", hits = 1, foodItems = listOf(FoodItem.SLIMEBALL))

        // ratio = 1/3 ≈ 0.333; floor(25 * 0.333) = 8; floor(10 * 0.333) = 3
        result.stats.hunger shouldBe 48     // 40 + 8
        result.stats.happiness shouldBe 43  // 40 + 3
        result.stats.energy shouldBe 40     // 50 - 10
    }

    // ── SockBall bonus ───────────────────────────────────────────────────────

    @Test
    fun `SockBall bonus adds 5 happiness when at least 1 hit with SOCK_BALL`() = runTest {
        seedState("m1", stats(hunger = 40, happiness = 40, energy = 50))
        val result = useCase.applyResult("m1", hits = 3, foodItems = listOf(FoodItem.SOCK_BALL))

        result.stats.happiness shouldBe 55  // 40 + 10 (full) + 5 (sock bonus)
    }

    @Test
    fun `SockBall bonus is NOT applied when hits are 0 even if SOCK_BALL is in food list`() = runTest {
        seedState("m1", stats(hunger = 40, happiness = 40, energy = 50))
        val result = useCase.applyResult("m1", hits = 0, foodItems = listOf(FoodItem.SOCK_BALL))

        result.stats.happiness shouldBe 40  // no gain, no sock bonus
    }

    @Test
    fun `SockBall bonus is NOT applied when SOCK_BALL is not in the food list`() = runTest {
        seedState("m1", stats(hunger = 40, happiness = 40, energy = 50))
        val result = useCase.applyResult("m1", hits = 3, foodItems = listOf(FoodItem.BUG, FoodItem.MOON_COOKIE))

        result.stats.happiness shouldBe 50  // 40 + 10, no sock bonus
    }

    // ── Stat clamping ────────────────────────────────────────────────────────

    @Test
    fun `hunger is clamped at 100 after gain`() = runTest {
        seedState("m1", stats(hunger = 95, happiness = 40, energy = 50))
        val result = useCase.applyResult("m1", hits = 3, foodItems = listOf(FoodItem.GLOW_SNACK))

        result.stats.hunger shouldBe 100    // 95 + 25 clamped to 100
    }

    @Test
    fun `energy is clamped at 0 when already low`() = runTest {
        seedState("m1", stats(hunger = 50, happiness = 50, energy = 5))
        val result = useCase.applyResult("m1", hits = 0, foodItems = listOf(FoodItem.BUG))

        result.stats.energy shouldBe 0      // 5 - 10 clamped to 0
    }

    @Test
    fun `happiness is clamped at 100 with SockBall bonus`() = runTest {
        seedState("m1", stats(hunger = 40, happiness = 98, energy = 50))
        val result = useCase.applyResult("m1", hits = 3, foodItems = listOf(FoodItem.SOCK_BALL))

        result.stats.happiness shouldBe 100 // 98 + 10 + 5 clamped to 100
    }

    // ── Persisted state ──────────────────────────────────────────────────────

    @Test
    fun `applyResult saves the updated state to the repository`() = runTest {
        seedState("m1", stats(hunger = 40, happiness = 40, energy = 50))
        val result = useCase.applyResult("m1", hits = 3, foodItems = listOf(FoodItem.BUG))

        val persisted = repository.getPetState("m1")
        persisted shouldNotBe null
        persisted!!.stats shouldBe result.stats
    }
}

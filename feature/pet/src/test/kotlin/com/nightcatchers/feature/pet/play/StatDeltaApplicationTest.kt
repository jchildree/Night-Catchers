package com.nightcatchers.feature.pet.play

import com.nightcatchers.core.domain.model.EvolutionStage
import com.nightcatchers.core.domain.model.Mood
import com.nightcatchers.core.domain.model.PetState
import com.nightcatchers.core.domain.model.PetStats
import com.nightcatchers.core.domain.model.StatDelta
import com.nightcatchers.core.testing.FakePetRepository
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * Exercises the [com.nightcatchers.core.domain.repository.PetRepository.applyStatDelta]
 * contract via [FakePetRepository] — the same clamping math is mirrored in
 * `PetRepositoryImpl`. Lives in this module because feature/pet already test-depends on
 * core:testing.
 */
class StatDeltaApplicationTest {

    private val monsterId = "m"

    @Test
    fun `positive delta clamps at 100`() = runTest {
        val repo = FakePetRepository()
        repo.savePetState(seed(stats = PetStats(95, 95, 95, 95, 95)))

        val updated = repo.applyStatDelta(monsterId, StatDelta(hunger = 50, happiness = 50))

        updated.stats.hunger shouldBe 100
        updated.stats.happiness shouldBe 100
    }

    @Test
    fun `negative delta clamps at 0`() = runTest {
        val repo = FakePetRepository()
        repo.savePetState(seed(stats = PetStats(5, 5, 5, 5, 5)))

        val updated = repo.applyStatDelta(monsterId, StatDelta(energy = -50, spookiness = -50, trust = -50))

        updated.stats.energy shouldBe 0
        updated.stats.spookiness shouldBe 0
        updated.stats.trust shouldBe 0
    }

    @Test
    fun `zero delta leaves stats unchanged`() = runTest {
        val repo = FakePetRepository()
        val seedStats = PetStats(40, 50, 60, 70, 30)
        repo.savePetState(seed(stats = seedStats))

        val updated = repo.applyStatDelta(monsterId, StatDelta())

        updated.stats shouldBe seedStats
    }

    @Test
    fun `mixed delta updates each stat independently`() = runTest {
        val repo = FakePetRepository()
        repo.savePetState(seed(stats = PetStats(50, 50, 50, 50, 50)))

        val updated = repo.applyStatDelta(
            monsterId,
            StatDelta(hunger = 10, happiness = -5, energy = -20, spookiness = 15, trust = 8),
        )

        updated.stats.hunger shouldBe 60
        updated.stats.happiness shouldBe 45
        updated.stats.energy shouldBe 30
        updated.stats.spookiness shouldBe 65
        updated.stats.trust shouldBe 58
    }

    @Test
    fun `applyStatDelta updates lastInteractedAt`() = runTest {
        val repo = FakePetRepository()
        val before = Instant.now().minusSeconds(60)
        repo.savePetState(seed(stats = PetStats(50, 50, 50, 50, 50), lastInteractedAt = before))

        val updated = repo.applyStatDelta(monsterId, StatDelta(happiness = 5))

        (updated.lastInteractedAt > before) shouldBe true
    }

    private fun seed(
        stats: PetStats,
        lastInteractedAt: Instant = Instant.now(),
    ) = PetState(
        monsterId = monsterId,
        stats = stats,
        mood = Mood.CONTENT,
        stage = EvolutionStage.BABY,
        lastInteractedAt = lastInteractedAt,
        updatedAt = Instant.now(),
    )
}

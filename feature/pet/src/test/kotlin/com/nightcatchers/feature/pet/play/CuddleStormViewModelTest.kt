package com.nightcatchers.feature.pet.play

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.nightcatchers.core.domain.model.EvolutionStage
import com.nightcatchers.core.domain.model.MiniGameId
import com.nightcatchers.core.domain.model.Mood
import com.nightcatchers.core.domain.model.PetState
import com.nightcatchers.core.domain.model.PetStats
import com.nightcatchers.core.domain.usecase.ApplyMiniGameOutcomeUseCase
import com.nightcatchers.core.testing.FakePetRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class CuddleStormViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var petRepository: FakePetRepository
    private lateinit var applyOutcome: ApplyMiniGameOutcomeUseCase

    private val monsterId = "monster-1"

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        petRepository = FakePetRepository()
        applyOutcome = ApplyMiniGameOutcomeUseCase(petRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `taps accumulate and game ends after 10 seconds`() = runTest(dispatcher) {
        seedPet(stats = PetStats(hunger = 50, happiness = 50, energy = 80, spookiness = 50, trust = 30))
        val vm = newViewModel()

        // Tap 50 times — beyond Overjoyed threshold (46) — and advance just enough to
        // process taps without firing the timer.
        repeat(50) { vm.onTap() }
        advanceTimeBy(100)

        vm.state.value.tapCount shouldBe 50
        vm.state.value.emotionTier shouldBe EmotionTier.OVERJOYED
        vm.state.value.phase shouldBe GamePhase.RUNNING

        // Run out the rest of the timer.
        advanceTimeBy(10_000)
        advanceUntilIdle()

        vm.state.value.phase shouldBe GamePhase.FINISHED
    }

    @Test
    fun `session emits SessionComplete event with correct outcome and persists stat delta`() =
        runTest(dispatcher) {
            seedPet(stats = PetStats(hunger = 50, happiness = 40, energy = 80, spookiness = 60, trust = 30))
            val vm = newViewModel()

            vm.events.test {
                repeat(46) { vm.onTap() } // perfect score
                advanceTimeBy(10_500)
                advanceUntilIdle()

                val event = awaitItem()
                event.shouldBeInstanceOf<CuddleStormEvent.SessionComplete>()
                event.outcome.gameId shouldBe MiniGameId.CUDDLE_STORM
                event.outcome.rawScore shouldBe 46
                event.outcome.scoreFraction shouldBe 1.0f
            }

            // Stat delta applied: happiness +30, trust +8, spookiness -10, energy -15
            val updated = petRepository.getPetState(monsterId)!!
            updated.stats.happiness shouldBe 70 // 40 + 30
            updated.stats.trust shouldBe 38     // 30 + 8
            updated.stats.spookiness shouldBe 50 // 60 - 10
            updated.stats.energy shouldBe 65     // 80 - 15
        }

    @Test
    fun `taps after game finished are ignored`() = runTest(dispatcher) {
        seedPet()
        val vm = newViewModel()

        advanceTimeBy(10_500)
        advanceUntilIdle()
        vm.state.value.phase shouldBe GamePhase.FINISHED

        val finalCount = vm.state.value.tapCount
        repeat(20) { vm.onTap() }
        vm.state.value.tapCount shouldBe finalCount
    }

    @Test
    fun `zero taps still applies floor reward and full energy cost`() = runTest(dispatcher) {
        seedPet(stats = PetStats(hunger = 50, happiness = 40, energy = 80, spookiness = 60, trust = 30))
        val vm = newViewModel()

        vm.events.test {
            advanceTimeBy(10_500)
            advanceUntilIdle()

            val event = awaitItem()
            event.shouldBeInstanceOf<CuddleStormEvent.SessionComplete>()
            event.outcome.scoreFraction shouldBe 0f
            event.outcome.rawScore shouldBe 0
        }

        // 25% floor: happiness +7 (30 * 0.25), trust +2 (8 * 0.25), spookiness -2 (-10 * 0.25), energy -15 full
        val updated = petRepository.getPetState(monsterId)!!
        updated.stats.happiness shouldBe 47
        updated.stats.trust shouldBe 32
        updated.stats.spookiness shouldBe 58
        updated.stats.energy shouldBe 65
    }

    private suspend fun seedPet(
        stats: PetStats = PetStats(hunger = 50, happiness = 50, energy = 80, spookiness = 50, trust = 30),
    ) {
        petRepository.savePetState(
            PetState(
                monsterId = monsterId,
                stats = stats,
                mood = Mood.CONTENT,
                stage = EvolutionStage.BABY,
                lastInteractedAt = Instant.now(),
                updatedAt = Instant.now(),
            ),
        )
    }

    private fun newViewModel(): CuddleStormViewModel = CuddleStormViewModel(
        savedStateHandle = SavedStateHandle(mapOf("monsterId" to monsterId)),
        applyMiniGameOutcome = applyOutcome,
    )
}

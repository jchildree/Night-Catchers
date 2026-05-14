package com.nightcatchers.feature.pet.play

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcatchers.core.domain.model.MiniGameId
import com.nightcatchers.core.domain.model.MiniGameOutcome
import com.nightcatchers.core.domain.usecase.ApplyMiniGameOutcomeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

/**
 * Spook Tag (Hide & Seek) — 5-round shell game.
 *
 * Round flow: SHOWING → SHUFFLING → CHOOSING → REVEALING → (next round or FINISHED)
 *
 * During SHOWING the monster is visible behind its door for SHOW_MS, so the player
 * knows where to watch. During SHUFFLING the doors close and swap positions [swapCount]
 * times at [swapDelayForRound] speed. The player then taps in CHOOSING.
 * REVEALING shows both the chosen door and (if different) the monster's real door.
 *
 * Swap speed: 1200ms (round 1) → 600ms (round 5), stepping by 150ms per round.
 * Swap count: 4 (round 1) → 8 (round 5), stepping by 1 per round.
 */
@HiltViewModel
class SpookTagViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val applyMiniGameOutcome: ApplyMiniGameOutcomeUseCase,
) : ViewModel() {

    private val monsterId: String = checkNotNull(savedStateHandle["monsterId"])

    private val _state = MutableStateFlow(SpookTagState())
    val state: StateFlow<SpookTagState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<SpookTagEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<SpookTagEvent> = _events.asSharedFlow()

    init { startRound() }

    fun onDoorTap(slot: Int) {
        if (_state.value.phase != SpookTagPhase.CHOOSING) return
        val contents = _state.value.slotContents
        val hit = contents.getOrElse(slot) { DoorContent.EMPTY } == DoorContent.MONSTER
        val newScore = _state.value.score + if (hit) 1 else 0
        val monsterSlot = contents.indexOfFirst { it == DoorContent.MONSTER }

        _state.update {
            it.copy(
                phase = SpookTagPhase.REVEALING,
                playerChoice = slot,
                score = newScore,
                // Open both: the chosen door and, if wrong, the monster's actual door
                doorsOpen = List(3) { i -> i == slot || i == monsterSlot },
            )
        }

        viewModelScope.launch {
            delay(REVEAL_MS)
            val nextRound = _state.value.currentRound + 1
            if (nextRound > TOTAL_ROUNDS) {
                finishGame()
            } else {
                _state.update { it.copy(currentRound = nextRound, playerChoice = null) }
                startRound()
            }
        }
    }

    private fun startRound() {
        val monsterSlot = Random.nextInt(3)
        val contents = MutableList(3) { DoorContent.EMPTY }
        contents[monsterSlot] = DoorContent.MONSTER
        _state.update {
            it.copy(
                phase = SpookTagPhase.SHOWING,
                slotContents = contents.toList(),
                doorsOpen = List(3) { true },
                swappingSlots = null,
            )
        }
        viewModelScope.launch {
            delay(SHOW_MS)
            shuffle()
        }
    }

    private suspend fun shuffle() {
        val round = _state.value.currentRound
        val swapDelayMs = swapDelayForRound(round)
        val swapCount = 3 + round // 4 swaps round 1 → 8 swaps round 5
        val contents = _state.value.slotContents.toMutableList()

        _state.update { it.copy(phase = SpookTagPhase.SHUFFLING, doorsOpen = List(3) { false }) }
        delay(swapDelayMs / 2)

        repeat(swapCount) {
            // Pick two distinct slots to swap
            val a = Random.nextInt(3)
            val b = (a + 1 + Random.nextInt(2)) % 3

            // Highlight the pair briefly before swapping
            _state.update { it.copy(swappingSlots = Pair(a, b)) }
            delay(swapDelayMs / 3)

            val tmp = contents[a]; contents[a] = contents[b]; contents[b] = tmp
            _state.update { it.copy(slotContents = contents.toList(), swappingSlots = null) }
            delay(swapDelayMs * 2 / 3)
        }

        _state.update { it.copy(phase = SpookTagPhase.CHOOSING) }
    }

    private suspend fun finishGame() {
        val score = _state.value.score
        val fraction = score.toFloat() / TOTAL_ROUNDS
        val outcome = MiniGameOutcome(
            gameId = MiniGameId.SPOOK_TAG,
            scoreFraction = fraction,
            rawScore = score,
        )
        runCatching { applyMiniGameOutcome(monsterId, outcome) }
        _state.update { it.copy(phase = SpookTagPhase.FINISHED) }
        _events.emit(SpookTagEvent.SessionComplete(outcome))
    }

    private fun swapDelayForRound(round: Int): Long =
        (1200L - (round - 1) * 150L).coerceAtLeast(600L)

    internal companion object {
        const val TOTAL_ROUNDS = 5
        const val SHOW_MS = 1500L
        const val REVEAL_MS = 1200L
    }
}

data class SpookTagState(
    val phase: SpookTagPhase = SpookTagPhase.SHOWING,
    val currentRound: Int = 1,
    val score: Int = 0,
    val slotContents: List<DoorContent> = List(3) { DoorContent.EMPTY },
    val doorsOpen: List<Boolean> = List(3) { false },
    val playerChoice: Int? = null,
    val swappingSlots: Pair<Int, Int>? = null,
)

enum class SpookTagPhase { SHOWING, SHUFFLING, CHOOSING, REVEALING, FINISHED }

enum class DoorContent { MONSTER, EMPTY }

sealed interface SpookTagEvent {
    data class SessionComplete(val outcome: MiniGameOutcome) : SpookTagEvent
}

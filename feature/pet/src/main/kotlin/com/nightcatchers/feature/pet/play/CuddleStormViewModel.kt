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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

/**
 * Cuddle Storm — 10s tap frenzy.
 *
 * Score curve from the design doc:
 *  0–10  taps → Neutral
 *  11–25 taps → Pleased
 *  26–45 taps → Happy
 *  46+   taps → Overjoyed (confetti rain)
 *
 * scoreFraction is `tapCount / OVERJOYED_TAPS` clamped to [0,1] — that scales the base
 * stat reward in [MiniGameOutcome.statDelta].
 */
@HiltViewModel
class CuddleStormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val applyMiniGameOutcome: ApplyMiniGameOutcomeUseCase,
) : ViewModel() {

    private val monsterId: String = checkNotNull(savedStateHandle["monsterId"])

    private val _state = MutableStateFlow(CuddleStormState())
    val state: StateFlow<CuddleStormState> = _state.asStateFlow()

    // extraBufferCapacity = 1 so finishGame()'s emit doesn't suspend if the screen has
    // navigated away before the terminal event arrives (or in tests with no subscriber).
    private val _events = MutableSharedFlow<CuddleStormEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<CuddleStormEvent> = _events.asSharedFlow()

    init { startGame() }

    fun onTap() {
        val current = _state.value
        if (current.phase != GamePhase.RUNNING) return
        val nextCount = current.tapCount + 1
        _state.update {
            it.copy(
                tapCount = nextCount,
                emotionTier = emotionTierFor(nextCount),
            )
        }
    }

    private fun startGame() {
        _state.update { CuddleStormState(phase = GamePhase.RUNNING) }
        viewModelScope.launch {
            val tickMs = 50L
            var elapsed = 0L
            while (isActive && elapsed < SESSION_DURATION_MS) {
                delay(tickMs.milliseconds)
                elapsed += tickMs
                val remaining = (SESSION_DURATION_MS - elapsed).coerceAtLeast(0L)
                _state.update { it.copy(timeRemainingMs = remaining) }
            }
            finishGame()
        }
    }

    private suspend fun finishGame() {
        val finalState = _state.value
        val fraction = (finalState.tapCount.toFloat() / OVERJOYED_TAPS_THRESHOLD)
            .coerceIn(0f, 1f)
        val outcome = MiniGameOutcome(
            gameId = MiniGameId.CUDDLE_STORM,
            scoreFraction = fraction,
            rawScore = finalState.tapCount,
        )
        runCatching { applyMiniGameOutcome(monsterId, outcome) }
        _state.update { it.copy(phase = GamePhase.FINISHED) }
        _events.emit(CuddleStormEvent.SessionComplete(outcome))
    }

    private fun emotionTierFor(taps: Int): EmotionTier = when {
        taps >= OVERJOYED_TAPS_THRESHOLD -> EmotionTier.OVERJOYED
        taps >= 26 -> EmotionTier.HAPPY
        taps >= 11 -> EmotionTier.PLEASED
        else -> EmotionTier.NEUTRAL
    }

    private companion object {
        const val SESSION_DURATION_MS = 10_000L
        const val OVERJOYED_TAPS_THRESHOLD = 46
    }
}

data class CuddleStormState(
    val phase: GamePhase = GamePhase.RUNNING,
    val tapCount: Int = 0,
    val timeRemainingMs: Long = 10_000L,
    val emotionTier: EmotionTier = EmotionTier.NEUTRAL,
)

enum class GamePhase { RUNNING, FINISHED }

enum class EmotionTier { NEUTRAL, PLEASED, HAPPY, OVERJOYED }

sealed interface CuddleStormEvent {
    data class SessionComplete(val outcome: MiniGameOutcome) : CuddleStormEvent
}

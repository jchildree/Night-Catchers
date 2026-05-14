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

/**
 * Food Toss — 3-throw drag-to-aim session.
 *
 * scoreFraction = hits / TOTAL_THROWS, so 0 hits = 0.0, 3 hits = 1.0.
 * The Screen drives the throw animation independently; this VM only records
 * the result of each throw and transitions phase accordingly.
 */
@HiltViewModel
class FoodTossViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val applyMiniGameOutcome: ApplyMiniGameOutcomeUseCase,
) : ViewModel() {

    private val monsterId: String = checkNotNull(savedStateHandle["monsterId"])

    private val _state = MutableStateFlow(FoodTossState())
    val state: StateFlow<FoodTossState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<FoodTossEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<FoodTossEvent> = _events.asSharedFlow()

    fun onThrow(hit: Boolean) {
        val current = _state.value
        if (current.phase != FoodTossPhase.AIMING) return
        val newHits = current.hits + if (hit) 1 else 0
        val remaining = current.throwsRemaining - 1
        _state.update {
            it.copy(
                phase = FoodTossPhase.ANIMATING,
                hits = newHits,
                throwsRemaining = remaining,
                lastThrowHit = hit,
            )
        }
        viewModelScope.launch {
            delay(THROW_ANIM_MS)
            if (remaining == 0) {
                finishGame(newHits)
            } else {
                _state.update { it.copy(phase = FoodTossPhase.AIMING, lastThrowHit = null) }
            }
        }
    }

    private suspend fun finishGame(hits: Int) {
        val fraction = hits.toFloat() / TOTAL_THROWS
        val outcome = MiniGameOutcome(
            gameId = MiniGameId.FOOD_TOSS,
            scoreFraction = fraction,
            rawScore = hits,
        )
        runCatching { applyMiniGameOutcome(monsterId, outcome) }
        _state.update { it.copy(phase = FoodTossPhase.FINISHED) }
        _events.emit(FoodTossEvent.SessionComplete(outcome))
    }

    internal companion object {
        const val TOTAL_THROWS = 3
        const val THROW_ANIM_MS = 900L
    }
}

data class FoodTossState(
    val phase: FoodTossPhase = FoodTossPhase.AIMING,
    val throwsRemaining: Int = FoodTossViewModel.TOTAL_THROWS,
    val hits: Int = 0,
    val lastThrowHit: Boolean? = null,
)

enum class FoodTossPhase { AIMING, ANIMATING, FINISHED }

sealed interface FoodTossEvent {
    data class SessionComplete(val outcome: MiniGameOutcome) : FoodTossEvent
}

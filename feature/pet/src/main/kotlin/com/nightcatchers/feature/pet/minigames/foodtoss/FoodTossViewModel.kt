package com.nightcatchers.feature.pet.minigames.foodtoss

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcatchers.core.domain.model.minigames.FoodItem
import com.nightcatchers.core.domain.usecase.minigames.FoodTossUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val MAX_THROWS = 3
private const val MAX_DRAG_POWER_DP = 120f

@HiltViewModel
class FoodTossViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val foodTossUseCase: FoodTossUseCase,
) : ViewModel() {

    private val monsterId: String = checkNotNull(savedStateHandle["monsterId"])

    private val _uiState = MutableStateFlow<FoodTossUiState>(FoodTossUiState.Loading)
    val uiState: StateFlow<FoodTossUiState> = _uiState.asStateFlow()

    private val _dragOffset = MutableStateFlow(Offset.Zero)
    val dragOffset: StateFlow<Offset> = _dragOffset.asStateFlow()

    private val _isFlying = MutableStateFlow(false)
    val isFlying: StateFlow<Boolean> = _isFlying.asStateFlow()

    // Tracks accumulated hits and thrown food items during a session
    private var sessionHits = 0
    private val sessionFoodItems = mutableListOf<FoodItem>()
    private var throwsRemaining = MAX_THROWS

    init {
        viewModelScope.launch {
            val canPlay = foodTossUseCase.checkCanPlay(monsterId)
            if (!canPlay) {
                _uiState.update { FoodTossUiState.TooFull() }
            } else {
                startSession()
            }
        }
    }

    private fun startSession() {
        sessionHits = 0
        sessionFoodItems.clear()
        throwsRemaining = MAX_THROWS
        _uiState.update {
            FoodTossUiState.Playing(
                throwsRemaining = throwsRemaining,
                hits = 0,
                currentFood = pickRandomFood(),
                monsterEmoji = "🎃",
                isFlying = false,
                dragOffset = Offset.Zero,
            )
        }
    }

    fun onDragStart(offset: Offset) {
        if (_isFlying.value) return
        _dragOffset.update { Offset.Zero }
    }

    fun onDragUpdate(delta: Offset) {
        if (_isFlying.value) return
        _dragOffset.update { current ->
            val next = current + delta
            // Clamp the drag power to MAX_DRAG_POWER_DP
            val magnitude = kotlin.math.sqrt(next.x * next.x + next.y * next.y)
            if (magnitude > MAX_DRAG_POWER_DP) {
                val scale = MAX_DRAG_POWER_DP / magnitude
                Offset(next.x * scale, next.y * scale)
            } else {
                next
            }
        }
    }

    /** Called when the player releases the drag — triggers the throw animation. */
    fun onDragRelease() {
        if (_isFlying.value) return
        val drag = _dragOffset.value
        // Need some minimum drag to count as a throw
        val magnitude = kotlin.math.sqrt(drag.x * drag.x + drag.y * drag.y)
        if (magnitude < 10f) {
            _dragOffset.update { Offset.Zero }
            return
        }
        _isFlying.update { true }
        val playing = _uiState.value as? FoodTossUiState.Playing ?: return
        _uiState.update { playing.copy(isFlying = true, dragOffset = drag) }
        _dragOffset.update { Offset.Zero }
    }

    /**
     * Called by the UI when the throw animation completes.
     * [isHit] indicates whether the food landed in the target zone.
     */
    fun onThrowComplete(isHit: Boolean) {
        val playing = _uiState.value as? FoodTossUiState.Playing ?: return
        if (isHit) sessionHits++
        sessionFoodItems += playing.currentFood
        throwsRemaining--
        _isFlying.update { false }

        if (throwsRemaining <= 0) {
            onSessionComplete(sessionHits, sessionFoodItems.toList())
        } else {
            _uiState.update {
                playing.copy(
                    throwsRemaining = throwsRemaining,
                    hits = sessionHits,
                    currentFood = pickRandomFood(),
                    isFlying = false,
                    dragOffset = Offset.Zero,
                )
            }
        }
    }

    fun onSessionComplete(hits: Int, foodItems: List<FoodItem>) {
        viewModelScope.launch {
            runCatching { foodTossUseCase.applyResult(monsterId, hits, foodItems) }
        }
        _uiState.update { FoodTossUiState.Complete(hits = hits, totalThrows = MAX_THROWS) }
    }

    private fun pickRandomFood(): FoodItem = FoodItem.entries.random()
}

sealed interface FoodTossUiState {
    data object Loading : FoodTossUiState

    data class TooFull(val message: String = "Monster is too full!") : FoodTossUiState

    data class Playing(
        val throwsRemaining: Int,
        val hits: Int,
        val currentFood: FoodItem,
        val monsterEmoji: String,
        val isFlying: Boolean,
        val dragOffset: Offset,
    ) : FoodTossUiState

    data class Complete(val hits: Int, val totalThrows: Int) : FoodTossUiState
}

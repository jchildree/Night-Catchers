package com.nightcatchers.feature.pet.minigames

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcatchers.core.domain.model.minigames.GameRewards
import com.nightcatchers.core.domain.model.minigames.GameStatDelta
import com.nightcatchers.core.domain.model.minigames.MiniGameId
import com.nightcatchers.core.domain.usecase.minigames.ApplyGameRewardUseCase
import com.nightcatchers.core.domain.usecase.minigames.GetFeaturedGameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface GameResultUiState {
    data object Loading : GameResultUiState
    data class Error(val message: String) : GameResultUiState
    data class Ready(
        val game: MiniGameId,
        val delta: GameStatDelta,
        val monsterEmoji: String,
    ) : GameResultUiState
}

@HiltViewModel
class GameResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val applyGameReward: ApplyGameRewardUseCase,
    private val getFeaturedGame: GetFeaturedGameUseCase,
) : ViewModel() {

    private val monsterId: String = checkNotNull(savedStateHandle["monsterId"])
    private val gameId: String = checkNotNull(savedStateHandle["game"])

    private val _uiState = MutableStateFlow<GameResultUiState>(GameResultUiState.Loading)
    val uiState: StateFlow<GameResultUiState> = _uiState.asStateFlow()

    init {
        applyReward()
    }

    private fun applyReward() {
        viewModelScope.launch {
            val game = MiniGameId.fromId(gameId)
            if (game == null) {
                _uiState.value = GameResultUiState.Error("Unknown game: $gameId")
                return@launch
            }
            val featuredGame = getFeaturedGame()
            val dailyBonusActive = game == featuredGame
            runCatching { applyGameReward(monsterId, game, dailyBonusActive) }
                .fold(
                    onSuccess = {
                        _uiState.value = GameResultUiState.Ready(
                            game = game,
                            delta = GameRewards.forGame(game),
                            monsterEmoji = game.emoji,
                        )
                    },
                    onFailure = { e ->
                        _uiState.value = GameResultUiState.Error(e.message ?: "Failed to apply reward")
                    },
                )
        }
    }
}

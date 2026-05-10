package com.nightcatchers.feature.pet.minigames

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcatchers.core.domain.model.minigames.MiniGameId
import com.nightcatchers.core.domain.repository.PetRepository
import com.nightcatchers.core.domain.usecase.minigames.CheckEnergyGateUseCase
import com.nightcatchers.core.domain.usecase.minigames.GetFeaturedGameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PlayMenuViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val petRepository: PetRepository,
    private val getFeaturedGame: GetFeaturedGameUseCase,
    private val checkEnergyGate: CheckEnergyGateUseCase,
) : ViewModel() {

    private val monsterId: String = checkNotNull(savedStateHandle["monsterId"])
    val featuredGame: MiniGameId = getFeaturedGame()

    val uiState: StateFlow<PlayMenuUiState> = petRepository
        .observePetState(monsterId)
        .map { petState ->
            if (petState == null) return@map PlayMenuUiState.Error("Monster not found")
            val availability = MiniGameId.entries.associateWith { game ->
                checkEnergyGate(game, petState.stats)
            }
            PlayMenuUiState.Ready(
                petStats = petState.stats,
                featuredGame = featuredGame,
                gamesAvailability = availability,
            )
        }
        .catch { emit(PlayMenuUiState.Error(it.message ?: "Unknown error")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PlayMenuUiState.Loading,
        )
}

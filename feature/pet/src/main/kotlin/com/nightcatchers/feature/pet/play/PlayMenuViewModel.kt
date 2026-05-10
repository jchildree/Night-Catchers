package com.nightcatchers.feature.pet.play

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcatchers.core.domain.model.MiniGameId
import com.nightcatchers.core.domain.model.PetStats
import com.nightcatchers.core.domain.repository.PetRepository
import com.nightcatchers.core.domain.usecase.GetFeaturedMiniGameUseCase
import com.nightcatchers.core.domain.usecase.IsMiniGameUnlockedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PlayMenuViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    petRepository: PetRepository,
    private val isMiniGameUnlocked: IsMiniGameUnlockedUseCase,
    getFeaturedMiniGame: GetFeaturedMiniGameUseCase,
) : ViewModel() {

    private val monsterId: String = checkNotNull(savedStateHandle["monsterId"])
    private val featured: MiniGameId = getFeaturedMiniGame()

    val uiState: StateFlow<PlayMenuUiState> = petRepository.observePetState(monsterId)
        .map { petState ->
            val stats = petState?.stats ?: return@map PlayMenuUiState.Loading
            val cards = MiniGameId.entries
                .map { id -> id.toCard(stats) }
                .sortedWith(featuredFirst)
            PlayMenuUiState.Ready(monsterId = monsterId, featured = featured, cards = cards)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PlayMenuUiState.Loading,
        )

    private fun MiniGameId.toCard(stats: PetStats): MiniGameCard {
        val unlock = isMiniGameUnlocked(this, stats)
        return MiniGameCard(
            id = this,
            isFeatured = this == featured,
            unlockState = unlock,
        )
    }

    private val featuredFirst: Comparator<MiniGameCard> =
        compareByDescending<MiniGameCard> { it.isFeatured }
            .thenBy { it.id.tier.ordinal }
            .thenBy { it.id.ordinal }
}

sealed interface PlayMenuUiState {
    data object Loading : PlayMenuUiState
    data class Ready(
        val monsterId: String,
        val featured: MiniGameId,
        val cards: List<MiniGameCard>,
    ) : PlayMenuUiState
}

data class MiniGameCard(
    val id: MiniGameId,
    val isFeatured: Boolean,
    val unlockState: IsMiniGameUnlockedUseCase.UnlockState,
)

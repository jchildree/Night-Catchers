package com.nightcatchers.feature.pet.minigames

import com.nightcatchers.core.domain.model.PetStats
import com.nightcatchers.core.domain.model.minigames.MiniGameId

sealed interface PlayMenuUiState {
    data object Loading : PlayMenuUiState
    data class Error(val message: String) : PlayMenuUiState
    data class Ready(
        val petStats: PetStats,
        val featuredGame: MiniGameId,
        val gamesAvailability: Map<MiniGameId, Boolean>,
    ) : PlayMenuUiState
}

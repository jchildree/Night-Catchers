package com.nightcatchers.feature.pet.play

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcatchers.core.domain.model.MonsterArchetypeCatalog
import com.nightcatchers.core.domain.repository.MonsterRepository
import com.nightcatchers.core.domain.usecase.GetFeaturedMiniGameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Top-level "Games" tab. Lists captured monsters so the child can pick which friend to
 * play with — taps route to the per-monster [PlayMenuScreen].
 */
@HiltViewModel
class GamesPickerViewModel @Inject constructor(
    monsterRepository: MonsterRepository,
    getFeaturedMiniGame: GetFeaturedMiniGameUseCase,
) : ViewModel() {

    val uiState: StateFlow<GamesPickerUiState> = monsterRepository.observeAll()
        .map { monsters ->
            val entries = monsters
                .filter { !it.isReleased }
                .mapNotNull { monster ->
                    val archetype = MonsterArchetypeCatalog.findById(monster.archetypeId)
                        ?: return@mapNotNull null
                    GamesPickerEntry(
                        monsterId = monster.id,
                        displayName = monster.nickname?.takeIf { it.isNotBlank() }
                            ?: archetype.name,
                        emoji = archetype.emoji,
                        subtitle = archetype.subtitle,
                    )
                }
            GamesPickerUiState.Ready(
                featuredGameLabel = getFeaturedMiniGame().displayName,
                entries = entries,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = GamesPickerUiState.Loading,
        )
}

sealed interface GamesPickerUiState {
    data object Loading : GamesPickerUiState
    data class Ready(
        val featuredGameLabel: String,
        val entries: List<GamesPickerEntry>,
    ) : GamesPickerUiState
}

data class GamesPickerEntry(
    val monsterId: String,
    val displayName: String,
    val emoji: String,
    val subtitle: String,
)

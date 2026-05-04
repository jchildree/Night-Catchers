package com.nightcatchers.feature.pet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcatchers.core.domain.model.MonsterArchetypeCatalog
import com.nightcatchers.core.domain.model.PetInteraction
import com.nightcatchers.core.domain.repository.MonsterRepository
import com.nightcatchers.core.domain.repository.PetRepository
import com.nightcatchers.core.domain.usecase.EvolvePetUseCase
import com.nightcatchers.core.domain.usecase.GetBondStageUseCase
import com.nightcatchers.core.domain.usecase.GetMoodStateUseCase
import com.nightcatchers.core.domain.usecase.GetRoomStageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PetViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val monsterRepository: MonsterRepository,
    private val petRepository: PetRepository,
    private val getMoodState: GetMoodStateUseCase,
    private val getBondStage: GetBondStageUseCase,
    private val getRoomStage: GetRoomStageUseCase,
    private val evolvePet: EvolvePetUseCase,
) : ViewModel() {

    private val monsterId: String = checkNotNull(savedStateHandle["monsterId"])

    private val _interacting = MutableStateFlow(false)
    private val _interactionResult = MutableStateFlow<InteractionResult?>(null)

    private val _events = MutableSharedFlow<PetEvent>()
    val events: SharedFlow<PetEvent> = _events.asSharedFlow()

    val uiState: StateFlow<PetUiState> = combine(
        monsterRepository.observeById(monsterId),
        petRepository.observePetState(monsterId),
        _interacting,
        _interactionResult,
    ) { monster, petState, isInteracting, result ->
        if (monster == null || petState == null) {
            return@combine PetUiState.Error("Monster not found")
        }
        val archetype = MonsterArchetypeCatalog.findById(monster.archetypeId)
            ?: return@combine PetUiState.Error("Unknown archetype: ${monster.archetypeId}")

        PetUiState.Ready(
            monster = monster,
            archetype = archetype,
            stats = petState.stats,
            mood = getMoodState(petState.stats, petState.lastInteractedAt),
            evolutionStage = petState.stage,
            bondStage = getBondStage(petState.stats.trust),
            roomStage = getRoomStage(petState.stats.trust),
            isInteracting = isInteracting,
            lastInteractionResult = result,
        )
    }
        .catch { emit(PetUiState.Error(it.message ?: "Unknown error")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PetUiState.Loading,
        )

    fun onInteract(interaction: PetInteraction) {
        if (_interacting.value) return
        viewModelScope.launch {
            _interacting.update { true }
            _interactionResult.update { null }
            val newState = runCatching { petRepository.applyInteraction(monsterId, interaction) }.getOrNull()
            if (newState != null) {
                _interactionResult.update { interactionResultFor(interaction) }
                if (newState.stats.trust >= 80) {
                    val evolved = runCatching { evolvePet(monsterId) }.getOrNull()
                    if (evolved != null && evolved.stage != newState.stage) {
                        _events.emit(PetEvent.NavigateToEvolve(monsterId))
                    }
                }
            }
            _interacting.update { false }
        }
    }

    fun dismissInteractionResult() {
        _interactionResult.update { null }
    }

    private fun interactionResultFor(interaction: PetInteraction): InteractionResult = when (interaction) {
        PetInteraction.Feed -> InteractionResult("Fed!", "🍖")
        PetInteraction.Play -> InteractionResult("Played!", "🎮")
        PetInteraction.Train -> InteractionResult("Trained!", "🏋️")
        PetInteraction.Story -> InteractionResult("Story time!", "📖")
        PetInteraction.Comfort -> InteractionResult("Comforted!", "🤗")
        PetInteraction.Praise -> InteractionResult("Praised!", "⭐")
    }
}

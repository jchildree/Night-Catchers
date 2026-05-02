package com.nightcatchers.feature.dex

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcatchers.core.domain.model.MonsterArchetype
import com.nightcatchers.core.domain.model.MonsterArchetypeCatalog
import com.nightcatchers.core.domain.repository.MonsterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class DexDetailState(
    val archetype: MonsterArchetype? = null,
    val isDiscovered: Boolean = false,
    val captureCount: Int = 0,
    val isLoading: Boolean = true,
)

@HiltViewModel
class DexDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val monsterRepository: MonsterRepository,
) : ViewModel() {

    private val archetypeId: String = checkNotNull(savedStateHandle["archetypeId"])

    val uiState: StateFlow<DexDetailState> = monsterRepository.observeAll()
        .map { monsters ->
            val archetype = MonsterArchetypeCatalog.all.find { it.id == archetypeId }
            if (archetype == null) {
                DexDetailState(isLoading = false)
            } else {
                val capturedMonsters = monsters.filter { !it.isReleased && it.archetypeId == archetypeId }
                DexDetailState(
                    archetype = archetype,
                    isDiscovered = capturedMonsters.isNotEmpty(),
                    captureCount = capturedMonsters.size,
                    isLoading = false,
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DexDetailState(),
        )
}
package com.nightcatchers.feature.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcatchers.core.domain.model.MonsterArchetypeCatalog
import com.nightcatchers.core.domain.repository.MonsterRepository
import com.nightcatchers.core.domain.repository.PetRepository
import com.nightcatchers.core.domain.usecase.GetMoodStateUseCase
import com.nightcatchers.core.domain.usecase.ReleaseMonsterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VaultViewModel @Inject constructor(
    private val monsterRepository: MonsterRepository,
    private val petRepository: PetRepository,
    private val getMoodState: GetMoodStateUseCase,
    private val releaseMonster: ReleaseMonsterUseCase,
) : ViewModel() {

    private val _pendingReleaseId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<VaultUiState> = combine(
        monsterRepository.observeAll(),
        _pendingReleaseId,
    ) { monsters, pendingId ->
        val active = monsters.filter { !it.isReleased }
        val entries = active.mapNotNull { monster ->
            val archetype = MonsterArchetypeCatalog.findById(monster.archetypeId) ?: return@mapNotNull null
            val petState = petRepository.getPetState(monster.id) ?: return@mapNotNull null
            VaultEntry(
                monster = monster,
                archetype = archetype,
                stats = petState.stats,
                mood = getMoodState(petState.stats),
            )
        }
        VaultUiState.Ready(entries = entries, pendingReleaseId = pendingId)
    }
        .catch { emit(VaultUiState.Error(it.message ?: "Unknown error")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = VaultUiState.Loading,
        )

    fun requestRelease(monsterId: String) {
        _pendingReleaseId.update { monsterId }
    }

    fun cancelRelease() {
        _pendingReleaseId.update { null }
    }

    fun confirmRelease() {
        val id = _pendingReleaseId.value ?: return
        viewModelScope.launch {
            runCatching { releaseMonster(id) }
            _pendingReleaseId.update { null }
        }
    }
}

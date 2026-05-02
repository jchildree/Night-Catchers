package com.nightcatchers.feature.dex

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcatchers.core.domain.model.MonsterArchetype
import com.nightcatchers.core.domain.model.MonsterArchetypeCatalog
import com.nightcatchers.core.domain.model.Rarity
import com.nightcatchers.core.domain.repository.MonsterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class DexState(
    val entries: List<DexEntry> = emptyList(),
    val selectedRarity: Rarity? = null,
    val sortBy: DexSort = DexSort.NUMBER,
    val showOnlyDiscovered: Boolean = false,
)

data class DexEntry(
    val archetype: MonsterArchetype,
    val isDiscovered: Boolean,
    val captureCount: Int,
)

enum class DexSort {
    NUMBER, NAME, RARITY
}

@HiltViewModel
class DexViewModel @Inject constructor(
    private val monsterRepository: MonsterRepository,
) : ViewModel() {

    private val _selectedRarity = MutableStateFlow<Rarity?>(null)
    private val _sortBy = MutableStateFlow(DexSort.NUMBER)
    private val _showOnlyDiscovered = MutableStateFlow(false)

    val uiState: StateFlow<DexState> = combine(
        monsterRepository.observeAll(),
        _selectedRarity,
        _sortBy,
        _showOnlyDiscovered,
    ) { monsters, selectedRarity, sortBy, showOnlyDiscovered ->
        val capturedArchetypes = monsters
            .filter { !it.isReleased }
            .groupBy { it.archetypeId }
            .mapValues { it.value.size }

        val entries = MonsterArchetypeCatalog.all.map { archetype ->
            DexEntry(
                archetype = archetype,
                isDiscovered = archetype.id in capturedArchetypes,
                captureCount = capturedArchetypes[archetype.id] ?: 0,
            )
        }
        .let { allEntries ->
            // Apply filters
            allEntries
                .filter { entry ->
                    (selectedRarity == null || entry.archetype.rarity == selectedRarity) &&
                    (!showOnlyDiscovered || entry.isDiscovered)
                }
                .sortedWith(getComparator(sortBy))
        }

        DexState(
            entries = entries,
            selectedRarity = selectedRarity,
            sortBy = sortBy,
            showOnlyDiscovered = showOnlyDiscovered,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DexState(),
        )

    fun setRarityFilter(rarity: Rarity?) {
        _selectedRarity.update { rarity }
    }

    fun setSortBy(sort: DexSort) {
        _sortBy.update { sort }
    }

    fun toggleShowOnlyDiscovered() {
        _showOnlyDiscovered.update { !it }
    }

    private fun getComparator(sort: DexSort): Comparator<DexEntry> = when (sort) {
        DexSort.NUMBER -> compareBy { it.archetype.number }
        DexSort.NAME -> compareBy { it.archetype.name }
        DexSort.RARITY -> compareBy<DexEntry> { it.archetype.rarity.ordinal }.thenBy { it.archetype.number }
    }
}
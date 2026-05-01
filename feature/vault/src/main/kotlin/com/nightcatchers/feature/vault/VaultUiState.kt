package com.nightcatchers.feature.vault

import com.nightcatchers.core.domain.model.Monster
import com.nightcatchers.core.domain.model.MonsterArchetype
import com.nightcatchers.core.domain.model.PetStats
import com.nightcatchers.core.domain.model.Mood
import com.nightcatchers.core.domain.model.Rarity

sealed interface VaultUiState {
    data object Loading : VaultUiState
    data class Error(val message: String) : VaultUiState
    data class Ready(
        val entries: List<VaultEntry>,
        val pendingReleaseId: String? = null,
    ) : VaultUiState {
        val isEmpty: Boolean get() = entries.isEmpty()
    }
}

data class VaultEntry(
    val monster: Monster,
    val archetype: MonsterArchetype,
    val stats: PetStats,
    val mood: Mood,
) {
    val rarity: Rarity get() = monster.rarity
    val emoji: String get() = archetype.emoji
    val displayName: String get() = monster.nickname ?: monster.name
}

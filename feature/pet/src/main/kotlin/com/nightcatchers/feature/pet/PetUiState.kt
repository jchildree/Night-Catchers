package com.nightcatchers.feature.pet

import com.nightcatchers.core.domain.model.BondStage
import com.nightcatchers.core.domain.model.EvolutionStage
import com.nightcatchers.core.domain.model.Mood
import com.nightcatchers.core.domain.model.Monster
import com.nightcatchers.core.domain.model.MonsterArchetype
import com.nightcatchers.core.domain.model.PetStats
import com.nightcatchers.core.domain.model.Rarity
import com.nightcatchers.core.domain.model.RoomStage

sealed interface PetUiState {
    data object Loading : PetUiState
    data class Error(val message: String) : PetUiState
    data class Ready(
        val monster: Monster,
        val archetype: MonsterArchetype,
        val stats: PetStats,
        val mood: Mood,
        val evolutionStage: EvolutionStage,
        val bondStage: BondStage,
        val roomStage: RoomStage,
        val isInteracting: Boolean = false,
        val lastInteractionResult: InteractionResult? = null,
    ) : PetUiState {
        val rarity: Rarity get() = monster.rarity
        val emoji: String get() = archetype.emoji
        val displayName: String get() = monster.nickname ?: monster.name
        val trustToNextStage: Int get() = roomStage.nextStageTrust()
    }
}

data class InteractionResult(
    val label: String,
    val emoji: String,
)

private fun RoomStage.nextStageTrust(): Int = when (this) {
    RoomStage.HOLDING_PEN -> RoomStage.COSY_CORNER.trustMin
    RoomStage.COSY_CORNER -> RoomStage.BEDROOM.trustMin
    RoomStage.BEDROOM -> RoomStage.SANCTUARY.trustMin
    RoomStage.SANCTUARY -> RoomStage.DREAM_ROOM.trustMin
    RoomStage.DREAM_ROOM -> 100
}

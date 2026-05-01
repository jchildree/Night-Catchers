package com.nightcatchers.feature.ar

import com.nightcatchers.core.domain.model.MonsterArchetype

sealed interface MonsterSpawnEvent {
    data class MonsterDetected(
        val archetype: MonsterArchetype,
        val screenX: Float,
        val screenY: Float,
    ) : MonsterSpawnEvent

    data object NoMonsterVisible : MonsterSpawnEvent
    data object Scanning : MonsterSpawnEvent
}

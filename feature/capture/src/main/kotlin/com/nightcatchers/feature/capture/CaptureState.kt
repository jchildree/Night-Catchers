package com.nightcatchers.feature.capture

import com.nightcatchers.core.domain.model.MonsterArchetype

/**
 * Capture ritual state machine (section 17 UX flow):
 * IDLE → SCANNING → MONSTER_SPAWNED → CAPTURING → SUCCESS | MISS
 */
sealed interface CaptureState {
    data object Idle : CaptureState
    data object Scanning : CaptureState
    data class MonsterSpawned(
        val archetype: MonsterArchetype,
        val anchorX: Float,
        val anchorY: Float,
    ) : CaptureState
    data class Capturing(
        val archetype: MonsterArchetype,
        val anchorX: Float,
        val anchorY: Float,
        val holdProgress: Float,    // 0.0 → 1.0
    ) : CaptureState
    data class Success(val monsterId: String) : CaptureState
    data object Miss : CaptureState
}

package com.nightcatchers.core.domain.model

import java.time.Instant

data class PetState(
    val monsterId: String,
    val stats: PetStats,
    val mood: Mood,
    val stage: EvolutionStage,
    val lastInteractedAt: Instant,
    val updatedAt: Instant,
)

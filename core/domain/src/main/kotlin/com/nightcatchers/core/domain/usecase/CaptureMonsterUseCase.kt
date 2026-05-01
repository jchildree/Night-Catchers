package com.nightcatchers.core.domain.usecase

import com.nightcatchers.core.domain.model.EvolutionStage
import com.nightcatchers.core.domain.model.Monster
import com.nightcatchers.core.domain.model.MonsterArchetype
import com.nightcatchers.core.domain.model.PetState
import com.nightcatchers.core.domain.model.PetStats
import com.nightcatchers.core.domain.repository.MonsterRepository
import com.nightcatchers.core.domain.repository.PetRepository
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class CaptureMonsterUseCase @Inject constructor(
    private val monsterRepository: MonsterRepository,
    private val petRepository: PetRepository,
    private val getMoodState: GetMoodStateUseCase,
) {
    suspend operator fun invoke(
        archetype: MonsterArchetype,
        roomLabel: String? = null,
    ): Monster {
        val now = Instant.now()
        val monster = Monster(
            id = UUID.randomUUID().toString(),
            archetypeId = archetype.id,
            name = archetype.name,
            rarity = archetype.rarity,
            captureDate = now,
            captureRoomLabel = roomLabel,
        )

        val initialStats = archetype.defaultStats.copy(
            trust = archetype.rarity.trustBonus,
        )
        val petState = PetState(
            monsterId = monster.id,
            stats = initialStats,
            mood = getMoodState(initialStats),
            stage = EvolutionStage.BABY,
            lastInteractedAt = now,
            updatedAt = now,
        )

        monsterRepository.save(monster)
        petRepository.savePetState(petState)

        return monster
    }
}

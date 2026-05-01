package com.nightcatchers.core.domain.usecase

import com.nightcatchers.core.domain.model.EvolutionStage
import com.nightcatchers.core.domain.model.PetState
import com.nightcatchers.core.domain.repository.PetRepository
import java.time.Instant
import javax.inject.Inject

class EvolvePetUseCase @Inject constructor(
    private val petRepository: PetRepository,
) {
    suspend operator fun invoke(monsterId: String): PetState? {
        val current = petRepository.getPetState(monsterId) ?: return null
        val nextStage = nextStage(current) ?: return current
        val evolved = current.copy(stage = nextStage, updatedAt = Instant.now())
        petRepository.savePetState(evolved)
        return evolved
    }

    private fun nextStage(state: PetState): EvolutionStage? {
        val trust = state.stats.trust
        return when (state.stage) {
            EvolutionStage.BABY -> if (trust >= EvolutionStage.TEEN.trustGate) EvolutionStage.TEEN else null
            EvolutionStage.TEEN -> if (trust >= EvolutionStage.ADULT.trustGate) EvolutionStage.ADULT else null
            EvolutionStage.ADULT -> null
        }
    }
}

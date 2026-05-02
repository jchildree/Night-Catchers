package com.nightcatchers.core.domain.usecase

import com.nightcatchers.core.domain.model.PetInteraction
import com.nightcatchers.core.domain.model.PetState
import com.nightcatchers.core.domain.repository.PetRepository
import javax.inject.Inject

class ComfortPetUseCase @Inject constructor(
    private val petRepository: PetRepository,
) {
    suspend operator fun invoke(monsterId: String): PetState =
        petRepository.applyInteraction(monsterId, PetInteraction.Comfort)
}

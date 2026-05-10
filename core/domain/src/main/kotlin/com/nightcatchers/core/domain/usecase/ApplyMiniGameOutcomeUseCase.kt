package com.nightcatchers.core.domain.usecase

import com.nightcatchers.core.domain.model.MiniGameOutcome
import com.nightcatchers.core.domain.model.PetState
import com.nightcatchers.core.domain.repository.PetRepository
import javax.inject.Inject

class ApplyMiniGameOutcomeUseCase @Inject constructor(
    private val petRepository: PetRepository,
) {
    suspend operator fun invoke(monsterId: String, outcome: MiniGameOutcome): PetState =
        petRepository.applyStatDelta(monsterId, outcome.statDelta())
}

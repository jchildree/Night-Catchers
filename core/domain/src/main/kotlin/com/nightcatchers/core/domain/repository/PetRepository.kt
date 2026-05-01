package com.nightcatchers.core.domain.repository

import com.nightcatchers.core.domain.model.PetInteraction
import com.nightcatchers.core.domain.model.PetState
import kotlinx.coroutines.flow.Flow

interface PetRepository {
    fun observePetState(monsterId: String): Flow<PetState?>
    suspend fun getPetState(monsterId: String): PetState?
    suspend fun savePetState(state: PetState)
    suspend fun applyInteraction(monsterId: String, interaction: PetInteraction): PetState
    suspend fun applyDecay(monsterId: String): PetState
}

package com.nightcatchers.core.domain.usecase

import com.nightcatchers.core.domain.repository.MonsterRepository
import javax.inject.Inject

class ReleaseMonsterUseCase @Inject constructor(
    private val monsterRepository: MonsterRepository,
) {
    suspend operator fun invoke(monsterId: String) {
        monsterRepository.release(monsterId)
    }
}

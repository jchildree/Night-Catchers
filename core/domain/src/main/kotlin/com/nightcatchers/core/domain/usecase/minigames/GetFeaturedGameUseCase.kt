package com.nightcatchers.core.domain.usecase.minigames

import com.nightcatchers.core.domain.model.minigames.MiniGameId
import java.time.LocalDate
import javax.inject.Inject

class GetFeaturedGameUseCase @Inject constructor() {
    operator fun invoke(date: LocalDate = LocalDate.now()): MiniGameId {
        val index = date.dayOfYear % MiniGameId.entries.size
        return MiniGameId.entries[index]
    }
}

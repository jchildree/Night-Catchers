package com.nightcatchers.core.domain.usecase

import com.nightcatchers.core.domain.model.MiniGameId
import java.time.LocalDate
import javax.inject.Inject

/**
 * Deterministic daily rotation: `dayOfYear % 6` indexes into the 6-game catalogue,
 * matching the design doc spec. The featured game appears first in the menu and gets a
 * glow highlight in [com.nightcatchers.feature.pet.play.PlayMenuScreen].
 */
class GetFeaturedMiniGameUseCase @Inject constructor() {

    operator fun invoke(date: LocalDate = LocalDate.now()): MiniGameId {
        val games = MiniGameId.entries
        val index = ((date.dayOfYear - 1).mod(games.size))
        return games[index]
    }
}

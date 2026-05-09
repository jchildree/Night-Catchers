package com.nightcatchers.core.domain.model.minigames

data class GameStatDelta(
    val hunger: Int = 0,
    val happiness: Int = 0,
    val energy: Int = 0,
    val spookiness: Int = 0,
    val trust: Int = 0,
)

object GameRewards {
    val FOOD_TOSS = GameStatDelta(hunger = 25, happiness = 10, energy = -10)
    val SPOOK_TAG = GameStatDelta(happiness = 20, spookiness = 15, energy = -20)
    val CUDDLE_STORM = GameStatDelta(happiness = 30, trust = 8, spookiness = -10, energy = -15)
    val SLIME_SORT = GameStatDelta(happiness = 20, trust = 10, spookiness = -5, energy = -15)
    val GHOST_DASH = GameStatDelta(happiness = 15, spookiness = 25, trust = 5, energy = -20)
    val PROTON_WRANGLE = GameStatDelta(happiness = 10, trust = 20, spookiness = 10, energy = -25)

    fun forGame(id: MiniGameId): GameStatDelta = when (id) {
        MiniGameId.FOOD_TOSS -> FOOD_TOSS
        MiniGameId.SPOOK_TAG -> SPOOK_TAG
        MiniGameId.CUDDLE_STORM -> CUDDLE_STORM
        MiniGameId.SLIME_SORT -> SLIME_SORT
        MiniGameId.GHOST_DASH -> GHOST_DASH
        MiniGameId.PROTON_WRANGLE -> PROTON_WRANGLE
    }
}

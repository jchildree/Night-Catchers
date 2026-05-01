package com.nightcatchers.core.domain.model

/**
 * Emotional bond arc (separate from EvolutionStage which is about visual form).
 * Controls which interactions are unlocked and how the monster behaves.
 */
enum class BondStage(
    val trustMin: Int,
    val label: String,
    val unlockedInteractions: List<String>,
) {
    STRANGER(0, "Stranger", listOf("Feed (throws only)", "Watch")),
    CURIOUS(20, "Curious", listOf("Feed (any method)", "Shadow Dance")),
    FRIENDLY(40, "Friendly", listOf("Feed", "Play (all)", "Lullaby Hum", "Name")),
    BONDED(60, "Bonded", listOf("Spook Training", "Diary", "Accessories", "Photo Mode")),
    BEST_FRIENDS(80, "Best Friends", listOf("Evolve", "Greeting", "Comfort Mode", "Friendship Card")),
    ;

    companion object {
        fun fromTrust(trust: Int): BondStage =
            entries.lastOrNull { trust >= it.trustMin } ?: STRANGER
    }
}

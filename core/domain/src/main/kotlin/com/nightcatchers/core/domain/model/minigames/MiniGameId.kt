package com.nightcatchers.core.domain.model.minigames

enum class MiniGameId(
    val gameId: String,
    val displayName: String,
    val emoji: String,
    val tier: MiniGameTier,
    val energyCost: Int,
) {
    FOOD_TOSS("food-toss", "Food Toss", "🍖", MiniGameTier.BONDING, 10),
    SPOOK_TAG("spook-tag", "Spook Tag", "🚪", MiniGameTier.BONDING, 20),
    CUDDLE_STORM("cuddle-storm", "Cuddle Storm", "💚", MiniGameTier.BONDING, 15),
    SLIME_SORT("slime-sort", "Slime Sort", "🫧", MiniGameTier.SKILL, 15),
    GHOST_DASH("ghost-dash", "Ghost Dash", "👻", MiniGameTier.SKILL, 20),
    PROTON_WRANGLE("proton-wrangle", "Proton Wrangle", "⚡", MiniGameTier.SKILL, 25),
    ;

    companion object {
        @JvmStatic
        fun fromId(id: String): MiniGameId? = entries.firstOrNull { it.gameId == id }
    }
}

enum class MiniGameTier { BONDING, SKILL }

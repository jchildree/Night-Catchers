package com.nightcatchers.core.domain.model

/**
 * Section 19 — Play Feature mini-games.
 *
 * Two tiers:
 * - BONDING games (Food Toss, Spook Tag, Cuddle Storm) — low energy cost, always available
 *   when stats are critical.
 * - SKILL games (Slime Sort, Ghost Dash, Proton Wrangle) — higher energy cost, locked when
 *   energy < 25, larger stat rewards.
 */
enum class MiniGameId(
    val slug: String,
    val displayName: String,
    val emoji: String,
    val tier: MiniGameTier,
    val energyCost: Int,
    val baseRewards: StatDelta,
) {
    FOOD_TOSS(
        slug = "food-toss",
        displayName = "Food Toss",
        emoji = "🍖",
        tier = MiniGameTier.BONDING,
        energyCost = 10,
        baseRewards = StatDelta(hunger = 25, happiness = 10),
    ),
    SPOOK_TAG(
        slug = "spook-tag",
        displayName = "Spook Tag",
        emoji = "🚪",
        tier = MiniGameTier.BONDING,
        energyCost = 20,
        baseRewards = StatDelta(happiness = 20, spookiness = 15),
    ),
    CUDDLE_STORM(
        slug = "cuddle-storm",
        displayName = "Cuddle Storm",
        emoji = "💚",
        tier = MiniGameTier.BONDING,
        energyCost = 15,
        baseRewards = StatDelta(happiness = 30, trust = 8, spookiness = -10),
    ),
    SLIME_SORT(
        slug = "slime-sort",
        displayName = "Slime Sort",
        emoji = "🫧",
        tier = MiniGameTier.SKILL,
        energyCost = 15,
        baseRewards = StatDelta(happiness = 20, trust = 10, spookiness = -5),
    ),
    GHOST_DASH(
        slug = "ghost-dash",
        displayName = "Ghost Dash",
        emoji = "👻",
        tier = MiniGameTier.SKILL,
        energyCost = 20,
        baseRewards = StatDelta(happiness = 15, trust = 5, spookiness = 25),
    ),
    PROTON_WRANGLE(
        slug = "proton-wrangle",
        displayName = "Proton Wrangle",
        emoji = "⚡",
        tier = MiniGameTier.SKILL,
        energyCost = 25,
        baseRewards = StatDelta(happiness = 10, trust = 20, spookiness = 10),
    );

    companion object {
        fun fromSlug(slug: String): MiniGameId? = entries.firstOrNull { it.slug == slug }
    }
}

enum class MiniGameTier { BONDING, SKILL }

/**
 * Signed stat changes applied to [PetStats] after a mini-game session.
 * Energy is always negative (cost). Other stats may be positive or negative depending
 * on the game (Cuddle Storm drains spookiness; Ghost Dash raises it).
 */
data class StatDelta(
    val hunger: Int = 0,
    val happiness: Int = 0,
    val energy: Int = 0,
    val spookiness: Int = 0,
    val trust: Int = 0,
) {
    operator fun plus(other: StatDelta): StatDelta = StatDelta(
        hunger = hunger + other.hunger,
        happiness = happiness + other.happiness,
        energy = energy + other.energy,
        spookiness = spookiness + other.spookiness,
        trust = trust + other.trust,
    )

    operator fun times(scale: Float): StatDelta = StatDelta(
        hunger = (hunger * scale).toInt(),
        happiness = (happiness * scale).toInt(),
        energy = (energy * scale).toInt(),
        spookiness = (spookiness * scale).toInt(),
        trust = (trust * scale).toInt(),
    )
}

/**
 * Result of a completed mini-game. [scoreFraction] is in [0f, 1f] — used to scale
 * non-energy rewards (energy cost is paid in full regardless of performance).
 */
data class MiniGameOutcome(
    val gameId: MiniGameId,
    val scoreFraction: Float,
    val rawScore: Int,
) {
    init {
        require(scoreFraction in 0f..1f) { "scoreFraction must be in [0,1]: $scoreFraction" }
    }

    /**
     * Reward delta = baseRewards × scoreFraction (clamped at 25% min so a child always
     * gets *something*) plus the full energy cost.
     */
    fun statDelta(): StatDelta {
        val scaled = baseRewardScale().let { scale -> gameId.baseRewards * scale }
        return scaled.copy(energy = -gameId.energyCost)
    }

    private fun baseRewardScale(): Float = (0.25f + 0.75f * scoreFraction).coerceIn(0.25f, 1f)
}

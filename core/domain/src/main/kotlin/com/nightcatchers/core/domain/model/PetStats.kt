package com.nightcatchers.core.domain.model

/**
 * All stat values are clamped [0, 100].
 */
data class PetStats(
    val hunger: Int,       // 0 = starving, 100 = full
    val happiness: Int,
    val energy: Int,
    val spookiness: Int,   // 0 = calm, 100 = peak spook
    val trust: Int,        // gates evolution; never decays
) {
    init {
        require(hunger in 0..100)
        require(happiness in 0..100)
        require(energy in 0..100)
        require(spookiness in 0..100)
        require(trust in 0..100)
    }

    companion object {
        val DEFAULT = PetStats(
            hunger = 80,
            happiness = 60,
            energy = 70,
            spookiness = 50,
            trust = 0,
        )
    }
}

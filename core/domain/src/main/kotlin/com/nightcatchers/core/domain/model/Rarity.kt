package com.nightcatchers.core.domain.model

enum class Rarity(
    val spawnWeight: Int,
    val captureHoldSeconds: Float,
    val trustBonus: Int,
) {
    COMMON(45, 1.5f, 8),
    UNCOMMON(30, 2.0f, 12),
    RARE(20, 2.5f, 18),
    LEGENDARY(5, 3.5f, 30),
}

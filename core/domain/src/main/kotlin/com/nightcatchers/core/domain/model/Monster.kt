package com.nightcatchers.core.domain.model

import java.time.Instant

data class Monster(
    val id: String,
    val archetypeId: String,
    val name: String,
    val nickname: String? = null,
    val rarity: Rarity,
    val captureDate: Instant,
    val captureRoomLabel: String? = null,   // Room label only — never raw GPS
    val isReleased: Boolean = false,
)

package com.nightcatchers.core.domain.model

import com.nightcatchers.core.common.DeviceTier

/**
 * AR lens identifiers per section 17 (Snapchat Lens Catalog).
 * Each lens has supported device tiers and a max-simultaneous-active limit of 2.
 */
enum class LensId(
    val supportedTiers: Set<DeviceTier>,
    val isCelebration: Boolean = false,
) {
    PROTON_PACK(setOf(DeviceTier.A, DeviceTier.B)),
    NIGHT_VISION(setOf(DeviceTier.A, DeviceTier.B, DeviceTier.C)),
    ECTO_GOGGLES(setOf(DeviceTier.A, DeviceTier.B)),
    EVOLUTION_BURST(setOf(DeviceTier.A, DeviceTier.B), isCelebration = true),
    BIRTHDAY_MODE(setOf(DeviceTier.A, DeviceTier.B, DeviceTier.C), isCelebration = true),
}

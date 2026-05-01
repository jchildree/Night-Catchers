package com.nightcatchers.core.common

/**
 * Device capability tiers that gate AR/shader features.
 * Tier A: flagship — full AR + all FBO shader passes
 * Tier B: mid-range — ARCore with max 2 FBO passes, half resolution
 * Tier C: low-end — no ARCore, 2D Lottie sprite overlay only
 */
enum class DeviceTier { A, B, C }

data class DeviceCapabilities(
    val tier: DeviceTier,
    val totalRamMb: Int,
    val supportsArCore: Boolean,
    val openGlEsVersion: Float,
    val maxTextureSize: Int,
)

package com.nightcatchers.core.common

/**
 * Device capability tiers that gate AR/shader features.
 * Tier A: flagship — ≥6 GB RAM, ARCore required, OpenGL ES ≥ 3.1; full AR + all FBO shader passes
 * Tier B: mid-range — ≥3 GB RAM, OpenGL ES ≥ 3.0 (ARCore not required); max 2 FBO passes, half resolution
 * Tier C: low-end — everything else; no OpenGL — Lottie fallback only
 */
enum class DeviceTier { A, B, C }

data class DeviceCapabilities(
    val tier: DeviceTier,
    val totalRamMb: Int,
    val supportsArCore: Boolean,
    val openGlEsVersion: Float,
    val maxTextureSize: Int,
)

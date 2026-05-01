package com.nightcatchers.feature.filters

enum class FilterLayer(val shaderAsset: String) {
    PROTON_BEAM_GLOW("shaders/proton_beam.frag"),
    SLIME_VIGNETTE("shaders/slime_vignette.frag"),
    GHOST_RADAR_HUD("shaders/ghost_radar.frag"),
    NIGHT_VISION("shaders/night_vision.frag"),
    ECTOPLASM_SPLATTER("shaders/ectoplasm.frag"),
}

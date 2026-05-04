package com.nightcatchers.app.navigation

import kotlinx.serialization.Serializable

/** Type-safe navigation destinations (section 10). */
sealed interface Dest {

    @Serializable object Splash : Dest
    @Serializable object Onboarding : Dest
    @Serializable object Home : Dest

    // ── Scan nested graph ──
    @Serializable object ScanCamera : Dest
    @Serializable object ScanFilters : Dest
    @Serializable data class ScanCapture(val archetypeId: String) : Dest
    @Serializable data class ScanResult(val monsterId: String) : Dest

    // ── Vault nested graph ──
    @Serializable object Vault : Dest
    @Serializable data class VaultDetail(val monsterId: String) : Dest
    @Serializable data class VaultRelease(val monsterId: String) : Dest

    // ── Pet nested graph ──
    @Serializable data class PetRoom(val monsterId: String) : Dest
    @Serializable data class PetPlay(val monsterId: String, val game: String) : Dest
    @Serializable data class PetEvolve(val monsterId: String) : Dest

    // ── Dex nested graph ──
    @Serializable object Dex : Dest
    @Serializable data class DexDetail(val archetypeId: String) : Dest
    @Serializable data class DexAchievement(val achievementId: String) : Dest
    @Serializable data class DexShare(val monsterId: String) : Dest

    // ── Settings nested graph ──
    @Serializable object Settings : Dest
    @Serializable object SettingsParent : Dest
    @Serializable object SettingsParentTime : Dest
    @Serializable object SettingsParentPinChange : Dest
}

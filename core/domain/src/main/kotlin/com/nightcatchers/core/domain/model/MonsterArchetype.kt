package com.nightcatchers.core.domain.model

data class MonsterArchetype(
    val id: String,
    val number: Int,
    val name: String,
    val subtitle: String,
    val emoji: String,
    val rarity: Rarity,
    val spawnBias: SpawnBias,
    val personality: Personality,
    val defaultStats: PetStats,
    val decayRates: DecayRates,
    val favFood: String,
    val favGame: String,
    val soothingAction: String,
    val captureHoldSeconds: Float = rarity.captureHoldSeconds,
)

data class Personality(
    val core: String,
    val loves: String,
    val hates: String,
    val catchphrase: String,
)

data class SpawnBias(
    val location: String,     // e.g. "bedroom_floor", "closet"
    val timeOfDay: String,    // e.g. "night", "any"
)

data class DecayRates(
    val hungerPerHour: Int,
    val happinessPerHour: Int,
    val energyPerHour: Int,
    val spookinessPerHour: Int,
)

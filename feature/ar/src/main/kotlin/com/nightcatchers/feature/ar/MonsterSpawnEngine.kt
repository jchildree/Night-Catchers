package com.nightcatchers.feature.ar

import com.nightcatchers.core.domain.model.MonsterArchetype
import com.nightcatchers.core.domain.model.MonsterArchetypeCatalog
import com.nightcatchers.core.domain.model.Rarity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class MonsterSpawnEngine @Inject constructor() {

    private val _spawnEvents = MutableSharedFlow<MonsterSpawnEvent>(extraBufferCapacity = 8)
    val spawnEvents: SharedFlow<MonsterSpawnEvent> = _spawnEvents.asSharedFlow()

    private var lastSpawnCheckMs = 0L
    private val spawnCooldownMs = 8_000L
    private val spawnProbabilityPerCheck = 0.15f

    fun onFrameAvailable(timestampMs: Long, hasValidPlane: Boolean) {
        if (!hasValidPlane) {
            _spawnEvents.tryEmit(MonsterSpawnEvent.Scanning)
            return
        }

        val timeSinceLast = timestampMs - lastSpawnCheckMs
        if (timeSinceLast < spawnCooldownMs) return

        lastSpawnCheckMs = timestampMs

        if (Random.nextFloat() < spawnProbabilityPerCheck) {
            val archetype = pickWeightedRandom()
            _spawnEvents.tryEmit(
                MonsterSpawnEvent.MonsterDetected(
                    archetype = archetype,
                    screenX = Random.nextFloat(),
                    screenY = Random.nextFloat(),
                ),
            )
        } else {
            _spawnEvents.tryEmit(MonsterSpawnEvent.NoMonsterVisible)
        }
    }

    private fun pickWeightedRandom(): MonsterArchetype {
        val pool = MonsterArchetypeCatalog.all
        val totalWeight = pool.sumOf { it.rarity.spawnWeight }
        var roll = Random.nextInt(totalWeight)
        for (archetype in pool) {
            roll -= archetype.rarity.spawnWeight
            if (roll < 0) return archetype
        }
        return pool.first()
    }
}

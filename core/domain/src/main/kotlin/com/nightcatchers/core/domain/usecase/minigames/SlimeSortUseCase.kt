package com.nightcatchers.core.domain.usecase.minigames

import com.nightcatchers.core.domain.model.PetState
import com.nightcatchers.core.domain.model.minigames.BlobColor
import com.nightcatchers.core.domain.model.minigames.BlobEntity
import com.nightcatchers.core.domain.model.minigames.BlobShape
import com.nightcatchers.core.domain.model.minigames.JarEntity
import com.nightcatchers.core.domain.model.minigames.MiniGameId
import com.nightcatchers.core.domain.repository.PetRepository
import java.util.UUID
import javax.inject.Inject

sealed class MatchResult {
    object Hit : MatchResult()
    object WrongColor : MatchResult()
    object WrongShape : MatchResult()
    object WrongBoth : MatchResult()
    object Phantom : MatchResult()
}

class SlimeSortUseCase @Inject constructor(
    private val petRepository: PetRepository,
) {

    /**
     * Generates 2 jars for round 1, 3 for round 2, 4 for round 3.
     * Each jar has a unique color+shape combination.
     */
    fun generateJars(round: Int): List<JarEntity> {
        val count = when {
            round >= 3 -> 4
            round == 2 -> 3
            else -> 2
        }
        val colors = BlobColor.entries.take(count)
        val shapes = BlobShape.entries.take(count)
        return colors.zip(shapes).map { (color, shape) ->
            JarEntity(
                id = UUID.randomUUID().toString(),
                acceptsColor = color,
                acceptsShape = shape,
                label = "${color.emoji}${shape.label}",
            )
        }
    }

    /**
     * Generates a random blob for the given round.
     * Round 1: 2 colors, 2 shapes. Round 2: 3 colors, 3 shapes. Round 3: 4 colors, 4 shapes.
     */
    fun generateBlob(round: Int, isPhantom: Boolean = false): BlobEntity {
        val poolSize = when {
            round >= 3 -> 4
            round == 2 -> 3
            else -> 2
        }
        val color = BlobColor.entries.random().let { BlobColor.entries[BlobColor.entries.indexOf(it) % poolSize] }
        val shape = BlobShape.entries.random().let { BlobShape.entries[BlobShape.entries.indexOf(it) % poolSize] }
        return BlobEntity(
            id = UUID.randomUUID().toString(),
            color = color,
            shape = shape,
            isPhantom = isPhantom,
        )
    }

    /**
     * Checks whether a blob matches a jar.
     * Phantom blobs always return [MatchResult.Phantom] regardless of jar.
     */
    fun checkMatch(blob: BlobEntity, jar: JarEntity): MatchResult {
        if (blob.isPhantom) return MatchResult.Phantom
        val colorMatch = blob.color == jar.acceptsColor
        val shapeMatch = blob.shape == jar.acceptsShape
        return when {
            colorMatch && shapeMatch -> MatchResult.Hit
            colorMatch && !shapeMatch -> MatchResult.WrongShape
            !colorMatch && shapeMatch -> MatchResult.WrongColor
            else -> MatchResult.WrongBoth
        }
    }

    /**
     * Applies stat rewards to the monster after a session.
     * Base: Happiness +20, Trust +10, Spookiness −5, Energy −15.
     * Happiness scales with session score fraction; energy cost is always applied in full.
     *
     * @param score session score achieved
     * @param combo maximum combo achieved (unused in delta but available for callers)
     */
    suspend fun applyResult(monsterId: String, score: Int, combo: Int): PetState {
        val maxScore = 300 // approximate max for a full session
        val scoreFraction = (score.toFloat() / maxScore).coerceIn(0f, 1f)
        val happinessGain = (20 * scoreFraction).toInt().coerceAtLeast(5)

        val current = petRepository.getPetState(monsterId)
            ?: error("Monster $monsterId not found")
        val stats = current.stats
        val updated = current.copy(
            stats = stats.copy(
                happiness = (stats.happiness + happinessGain).coerceIn(0, 100),
                trust = (stats.trust + 10).coerceIn(0, 100),
                spookiness = (stats.spookiness - 5).coerceIn(0, 100),
                energy = (stats.energy - 15).coerceIn(0, 100),
            ),
        )
        petRepository.savePetState(updated)
        return updated
    }
}

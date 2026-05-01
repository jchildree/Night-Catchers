package com.nightcatchers.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nightcatchers.core.domain.model.EvolutionStage
import com.nightcatchers.core.domain.model.Mood
import com.nightcatchers.core.domain.model.PetState
import com.nightcatchers.core.domain.model.PetStats
import java.time.Instant

@Entity(
    tableName = "pet_states",
    foreignKeys = [
        ForeignKey(
            entity = MonsterEntity::class,
            parentColumns = ["id"],
            childColumns = ["monsterId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("monsterId")],
)
data class PetStateEntity(
    @PrimaryKey val monsterId: String,
    val hunger: Int,
    val happiness: Int,
    val energy: Int,
    val spookiness: Int,
    val trust: Int,
    val mood: String,
    val stage: String,
    val lastInteractedAtMs: Long,
    val updatedAtMs: Long,
)

fun PetStateEntity.toDomain() = PetState(
    monsterId = monsterId,
    stats = PetStats(
        hunger = hunger,
        happiness = happiness,
        energy = energy,
        spookiness = spookiness,
        trust = trust,
    ),
    mood = Mood.valueOf(mood),
    stage = EvolutionStage.valueOf(stage),
    lastInteractedAt = Instant.ofEpochMilli(lastInteractedAtMs),
    updatedAt = Instant.ofEpochMilli(updatedAtMs),
)

fun PetStateEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
    "monsterId" to monsterId,
    "hunger" to hunger,
    "happiness" to happiness,
    "energy" to energy,
    "spookiness" to spookiness,
    "trust" to trust,
    "mood" to mood,
    "stage" to stage,
    "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
)

fun PetState.toEntity() = PetStateEntity(
    monsterId = monsterId,
    hunger = stats.hunger,
    happiness = stats.happiness,
    energy = stats.energy,
    spookiness = stats.spookiness,
    trust = stats.trust,
    mood = mood.name,
    stage = stage.name,
    lastInteractedAtMs = lastInteractedAt.toEpochMilli(),
    updatedAtMs = updatedAt.toEpochMilli(),
)

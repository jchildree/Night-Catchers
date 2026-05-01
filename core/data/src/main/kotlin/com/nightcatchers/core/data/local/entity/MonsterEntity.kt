package com.nightcatchers.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nightcatchers.core.domain.model.Monster
import com.nightcatchers.core.domain.model.Rarity
import java.time.Instant

@Entity(tableName = "monsters")
data class MonsterEntity(
    @PrimaryKey val id: String,
    val archetypeId: String,
    val name: String,
    val nickname: String?,
    val rarity: String,
    val captureDateEpochMs: Long,
    val captureRoomLabel: String?,
    val isReleased: Boolean,
)

fun MonsterEntity.toDomain() = Monster(
    id = id,
    archetypeId = archetypeId,
    name = name,
    nickname = nickname,
    rarity = Rarity.valueOf(rarity),
    captureDate = Instant.ofEpochMilli(captureDateEpochMs),
    captureRoomLabel = captureRoomLabel,
    isReleased = isReleased,
)

fun Monster.toEntity() = MonsterEntity(
    id = id,
    archetypeId = archetypeId,
    name = name,
    nickname = nickname,
    rarity = rarity.name,
    captureDateEpochMs = captureDate.toEpochMilli(),
    captureRoomLabel = captureRoomLabel,
    isReleased = isReleased,
)

fun MonsterEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
    "archetypeId" to archetypeId,
    "name" to name,
    "nickname" to nickname,
    "rarity" to rarity,
    "captureDateEpochMs" to captureDateEpochMs,
    "captureRoomLabel" to captureRoomLabel,
    "isReleased" to isReleased,
    // captureLatLng is intentionally excluded — COPPA / privacy
)

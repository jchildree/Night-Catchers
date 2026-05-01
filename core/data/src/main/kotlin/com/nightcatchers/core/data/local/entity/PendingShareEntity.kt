package com.nightcatchers.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nightcatchers.core.domain.model.PendingShare
import com.nightcatchers.core.domain.model.ShareStatus
import com.nightcatchers.core.domain.model.ShareType
import java.time.Instant

@Entity(tableName = "pending_shares")
data class PendingShareEntity(
    @PrimaryKey val id: String,
    val monsterId: String,
    val monsterName: String,
    val type: String,
    val status: String,
    val localFilePath: String,
    val createdAtMs: Long,
    val expiresAtMs: Long,
    val parentUid: String,
)

fun PendingShareEntity.toDomain() = PendingShare(
    id = id,
    monsterId = monsterId,
    monsterName = monsterName,
    type = ShareType.valueOf(type),
    status = ShareStatus.valueOf(status),
    localFilePath = localFilePath,
    createdAt = Instant.ofEpochMilli(createdAtMs),
    expiresAt = Instant.ofEpochMilli(expiresAtMs),
    parentUid = parentUid,
)

fun PendingShare.toEntity() = PendingShareEntity(
    id = id,
    monsterId = monsterId,
    monsterName = monsterName,
    type = type.name,
    status = status.name,
    localFilePath = localFilePath,
    createdAtMs = createdAt.toEpochMilli(),
    expiresAtMs = expiresAt.toEpochMilli(),
    parentUid = parentUid,
)

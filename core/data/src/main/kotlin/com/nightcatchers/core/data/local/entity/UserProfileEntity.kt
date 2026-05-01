package com.nightcatchers.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nightcatchers.core.domain.model.UserProfile

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val parentUid: String,
    val childId: String,
    val childFirstName: String,
    val pinHash: String,
    val createdAt: Long,
)

fun UserProfileEntity.toDomain() = UserProfile(
    parentUid = parentUid,
    childId = childId,
    childFirstName = childFirstName,
    pinHash = pinHash,
    createdAt = createdAt,
)

fun UserProfile.toEntity() = UserProfileEntity(
    parentUid = parentUid,
    childId = childId,
    childFirstName = childFirstName,
    pinHash = pinHash,
    createdAt = createdAt,
)

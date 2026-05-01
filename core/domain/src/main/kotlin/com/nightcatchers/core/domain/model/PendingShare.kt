package com.nightcatchers.core.domain.model

import java.time.Instant

enum class ShareType { CAPTURE_CLIP, CAPTURE_CARD, EVOLUTION_CARD, FRIENDSHIP_CARD }
enum class ShareStatus { PENDING_REVIEW, APPROVED, DECLINED, EXPIRED }

data class PendingShare(
    val id: String,
    val monsterId: String,
    val monsterName: String,
    val type: ShareType,
    val status: ShareStatus,
    val localFilePath: String,
    val createdAt: Instant,
    val expiresAt: Instant,           // auto-declines after 48h per privacy model
    val parentUid: String,
)

package com.nightcatchers.core.domain.model

import java.time.Instant

data class AuditEvent(
    val id: String,
    val eventType: String,
    val actor: String,            // parentUID or childId
    val parentUid: String,
    val childUid: String,
    val payload: String,          // JSON string — no PII
    val deviceTimestamp: Instant,
    val serverTimestamp: Instant?,
    val prevHash: String,         // SHA-256 of previous event — tamper chain
    val seqNum: Long,
)

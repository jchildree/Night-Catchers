package com.nightcatchers.core.domain.model

data class UserProfile(
    val parentUid: String,
    val childId: String,          // locally-generated UUID, never Firebase Auth
    val childFirstName: String,   // first name only — COPPA
    val pinHash: String,          // bcrypt hash
    val createdAt: Long,
)

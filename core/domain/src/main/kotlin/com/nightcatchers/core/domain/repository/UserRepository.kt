package com.nightcatchers.core.domain.repository

import com.nightcatchers.core.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeUserProfile(): Flow<UserProfile?>
    suspend fun getUserProfile(): UserProfile?
    suspend fun saveUserProfile(profile: UserProfile)
    suspend fun verifyPin(rawPin: String): Boolean
    suspend fun createPin(rawPin: String)
    suspend fun clearSession()
}

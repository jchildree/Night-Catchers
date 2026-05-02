package com.nightcatchers.core.testing

import com.nightcatchers.core.domain.model.UserProfile
import com.nightcatchers.core.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserRepository : UserRepository {

    private val profileFlow = MutableStateFlow<UserProfile?>(null)
    var profile: UserProfile?
        get() = profileFlow.value
        set(value) { profileFlow.value = value }

    var pinVerifyResult: Boolean = true

    override fun observeUserProfile(): Flow<UserProfile?> = profileFlow

    override suspend fun getUserProfile(): UserProfile? = profileFlow.value

    override suspend fun saveUserProfile(profile: UserProfile) {
        profileFlow.value = profile
    }

    override suspend fun verifyPin(rawPin: String): Boolean = pinVerifyResult

    override suspend fun createPin(rawPin: String) {
        profileFlow.value = profileFlow.value?.copy(pinHash = "bcrypt:$rawPin")
    }

    override suspend fun clearSession() {
        profileFlow.value = null
    }
}

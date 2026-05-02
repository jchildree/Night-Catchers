package com.nightcatchers.core.data.repository

import at.favre.lib.crypto.bcrypt.BCrypt
import com.nightcatchers.core.common.Dispatcher
import com.nightcatchers.core.common.NightCatchersDispatchers
import com.nightcatchers.core.data.local.dao.UserDao
import com.nightcatchers.core.data.local.entity.toDomain
import com.nightcatchers.core.data.local.entity.toEntity
import com.nightcatchers.core.domain.model.UserProfile
import com.nightcatchers.core.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val dao: UserDao,
    @Dispatcher(NightCatchersDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : UserRepository {

    override fun observeUserProfile(): Flow<UserProfile?> =
        dao.observeProfile().map { it?.toDomain() }

    override suspend fun getUserProfile(): UserProfile? = withContext(ioDispatcher) {
        dao.getProfile()?.toDomain()
    }

    override suspend fun saveUserProfile(profile: UserProfile): Unit = withContext(ioDispatcher) {
        dao.insert(profile.toEntity())
    }

    override suspend fun verifyPin(rawPin: String): Boolean = withContext(ioDispatcher) {
        val profile = dao.getProfile() ?: return@withContext false
        BCrypt.verifyer().verify(rawPin.toCharArray(), profile.pinHash).verified
    }

    override suspend fun createPin(rawPin: String): Unit = withContext(ioDispatcher) {
        val profile = dao.getProfile() ?: return@withContext
        val hash = BCrypt.withDefaults().hashToString(12, rawPin.toCharArray())
        dao.insert(profile.copy(pinHash = hash))
    }

    override suspend fun clearSession(): Unit = withContext(ioDispatcher) {
        dao.clear()
    }
}

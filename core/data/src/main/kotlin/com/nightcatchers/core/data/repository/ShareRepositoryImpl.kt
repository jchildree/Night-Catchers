package com.nightcatchers.core.data.repository

import com.nightcatchers.core.common.Dispatcher
import com.nightcatchers.core.common.NightCatchersDispatchers
import com.nightcatchers.core.data.local.dao.PendingShareDao
import com.nightcatchers.core.data.local.entity.toDomain
import com.nightcatchers.core.data.local.entity.toEntity
import com.nightcatchers.core.domain.model.PendingShare
import com.nightcatchers.core.domain.model.ShareStatus
import com.nightcatchers.core.domain.repository.ShareRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject

class ShareRepositoryImpl @Inject constructor(
    private val dao: PendingShareDao,
    @Dispatcher(NightCatchersDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : ShareRepository {

    override fun observePendingShares(): Flow<List<PendingShare>> =
        dao.observePending().map { list -> list.map { it.toDomain() } }

    override suspend fun enqueue(share: PendingShare): Unit = withContext(ioDispatcher) {
        dao.insert(share.toEntity())
    }

    override suspend fun updateStatus(shareId: String, status: ShareStatus): Unit =
        withContext(ioDispatcher) {
            dao.updateStatus(shareId, status.name)
        }

    override suspend fun expireOlderThan48Hours(): Unit = withContext(ioDispatcher) {
        dao.expireOlderThan(Instant.now().toEpochMilli())
    }
}

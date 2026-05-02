package com.nightcatchers.core.testing

import com.nightcatchers.core.domain.model.PendingShare
import com.nightcatchers.core.domain.model.ShareStatus
import com.nightcatchers.core.domain.repository.ShareRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.Instant

class FakeShareRepository : ShareRepository {

    private val shares = MutableStateFlow<List<PendingShare>>(emptyList())

    override fun observePendingShares(): Flow<List<PendingShare>> =
        shares.map { list -> list.filter { it.status == ShareStatus.PENDING_REVIEW } }

    override suspend fun enqueue(share: PendingShare) {
        shares.update { it + share }
    }

    override suspend fun updateStatus(shareId: String, status: ShareStatus) {
        shares.update { list ->
            list.map { if (it.id == shareId) it.copy(status = status) else it }
        }
    }

    override suspend fun expireOlderThan48Hours() {
        val cutoff = Instant.now().minusSeconds(48 * 3600)
        shares.update { list ->
            list.map { share ->
                if (share.createdAt.isBefore(cutoff) && share.status == ShareStatus.PENDING_REVIEW) {
                    share.copy(status = ShareStatus.EXPIRED)
                } else {
                    share
                }
            }
        }
    }
}

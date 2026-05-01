package com.nightcatchers.core.domain.repository

import com.nightcatchers.core.domain.model.PendingShare
import com.nightcatchers.core.domain.model.ShareStatus
import kotlinx.coroutines.flow.Flow

interface ShareRepository {
    fun observePendingShares(): Flow<List<PendingShare>>
    suspend fun enqueue(share: PendingShare)
    suspend fun updateStatus(shareId: String, status: ShareStatus)
    suspend fun expireOlderThan48Hours()
}

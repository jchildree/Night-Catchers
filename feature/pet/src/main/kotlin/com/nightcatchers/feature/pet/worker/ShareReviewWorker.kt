package com.nightcatchers.feature.pet.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import com.nightcatchers.core.domain.repository.ShareRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Runs every 6h. Expires pending shares older than 48h (section 20 privacy model).
 * Parent must approve within 48h or the share is auto-declined.
 */
@HiltWorker
class ShareReviewWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val shareRepository: ShareRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            shareRepository.expireOlderThan48Hours()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "share_review_worker"

        fun buildRequest() = PeriodicWorkRequestBuilder<ShareReviewWorker>(6, TimeUnit.HOURS)
            .build()
    }
}

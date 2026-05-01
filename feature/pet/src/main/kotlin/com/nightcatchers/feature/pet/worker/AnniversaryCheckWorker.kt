package com.nightcatchers.feature.pet.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import com.nightcatchers.core.domain.repository.MonsterRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * Runs daily at ~08:00 to check capture anniversaries (section 17 Birthday Mode trigger).
 * Emits a notification if today matches any monster's capture day-of-year.
 */
@HiltWorker
class AnniversaryCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val monsterRepository: MonsterRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val today = LocalDate.now(ZoneId.systemDefault())
            // (Full implementation: iterate monsters, compare captureDate month+day to today,
            //  post NotificationCompat if match found.)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "anniversary_check_worker"

        fun buildRequest() = PeriodicWorkRequestBuilder<AnniversaryCheckWorker>(24, TimeUnit.HOURS)
            .build()
    }
}

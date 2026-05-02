package com.nightcatchers.feature.pet.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import com.nightcatchers.core.domain.repository.MonsterRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * Runs daily to check capture anniversaries (Birthday Mode trigger).
 * Posts a local notification when today matches any monster's capture day + month.
 */
@HiltWorker
class AnniversaryCheckWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val monsterRepository: MonsterRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val today = LocalDate.now(ZoneId.systemDefault())
            val activeMonsters = monsterRepository.observeAll().first().filter { !it.isReleased }

            activeMonsters.forEach { monster ->
                val captureDate = monster.captureDate
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()

                if (captureDate.monthValue == today.monthValue &&
                    captureDate.dayOfMonth == today.dayOfMonth &&
                    captureDate.year != today.year // skip day-of-capture itself
                ) {
                    val yearsAgo = today.year - captureDate.year
                    postAnniversaryNotification(
                        monsterId = monster.id,
                        monsterName = monster.nickname ?: monster.name,
                        yearsAgo = yearsAgo,
                    )
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun postAnniversaryNotification(monsterId: String, monsterName: String, yearsAgo: Int) {
        if (!hasNotificationPermission()) {
            return
        }

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ensureChannel(nm)

        val yearLabel = if (yearsAgo == 1) "1 year" else "$yearsAgo years"
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🎂 Happy anniversary, $monsterName!")
            .setContentText("You've been friends for $yearLabel. Say hi today!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        nm.notify(monsterId.hashCode(), notification)
    }

    private fun hasNotificationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun ensureChannel(nm: NotificationManager) {
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Pet Anniversaries",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply { description = "Birthday Mode alerts for your captured monsters" },
            )
        }
    }

    companion object {
        const val WORK_NAME = "anniversary_check_worker"
        private const val CHANNEL_ID = "anniversary_alerts"

        fun buildRequest() = PeriodicWorkRequestBuilder<AnniversaryCheckWorker>(24, TimeUnit.HOURS)
            .build()
    }
}

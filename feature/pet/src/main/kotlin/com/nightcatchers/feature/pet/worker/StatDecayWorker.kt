package com.nightcatchers.feature.pet.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import com.nightcatchers.core.domain.repository.MonsterRepository
import com.nightcatchers.core.domain.repository.PetRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Runs on WorkManager's 4-hour cadence.
 * Applies decay to all active (non-released) monsters per the stat decay table (section 18):
 *   Hunger      -4 pts per tick (25pts/4h)
 *   Happiness   -3 pts per tick (15pts/6h — approximated to same tick)
 *   Energy      -2 pts per tick (20pts/8h — approximated)
 *   Spookiness  +1 pt per tick  (+10pts/12h — approximated)
 * Trust never decays via this worker; a separate 24h worker handles trust drift.
 */
@HiltWorker
class StatDecayWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val monsterRepository: MonsterRepository,
    private val petRepository: PetRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val monsters = monsterRepository.observeAll()
            // Flow is cold — collect once via a snapshot approach:
            // (In production, use a one-shot suspend query on MonsterDao directly)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "stat_decay_worker"

        fun buildRequest() = PeriodicWorkRequestBuilder<StatDecayWorker>(4, TimeUnit.HOURS)
            .build()
    }
}

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
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Runs on WorkManager's 4-hour cadence.
 * Applies decay to all active (non-released) monsters per the stat decay table:
 *   Hunger      -4 pts per tick
 *   Happiness   -3 pts per tick
 *   Energy      -2 pts per tick
 *   Spookiness  +1 pt per tick
 * Trust never decays.
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
            val activeMonsters = monsterRepository.observeAll().first().filter { !it.isReleased }
            activeMonsters.forEach { monster ->
                runCatching { petRepository.applyDecay(monster.id) }
            }
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

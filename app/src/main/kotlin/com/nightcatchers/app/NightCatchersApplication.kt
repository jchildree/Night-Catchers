package com.nightcatchers.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.nightcatchers.feature.pet.worker.AnniversaryCheckWorker
import com.nightcatchers.feature.pet.worker.ShareReviewWorker
import com.nightcatchers.feature.pet.worker.StatDecayWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class NightCatchersApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        initFirebaseAppCheck()
        scheduleBackgroundWorkers()
    }

    private fun initFirebaseAppCheck() {
        val provider: AppCheckProviderFactory = if (BuildConfig.DEBUG) {
            getDebugAppCheckProviderFactory() ?: PlayIntegrityAppCheckProviderFactory.getInstance()
        } else {
            PlayIntegrityAppCheckProviderFactory.getInstance()
        }
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(provider)
    }

    private fun getDebugAppCheckProviderFactory(): AppCheckProviderFactory? {
        return try {
            val clazz = Class.forName("com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory")
            val method = clazz.getMethod("getInstance")
            method.invoke(null) as? AppCheckProviderFactory
        } catch (ignored: Throwable) {
            null
        }
    }

    private fun scheduleBackgroundWorkers() {
        val wm = WorkManager.getInstance(this)
        wm.enqueueUniquePeriodicWork(
            "stat_decay",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<StatDecayWorker>(4, TimeUnit.HOURS).build(),
        )
        wm.enqueueUniquePeriodicWork(
            "anniversary_check",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<AnniversaryCheckWorker>(24, TimeUnit.HOURS).build(),
        )
        wm.enqueueUniquePeriodicWork(
            "share_review",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<ShareReviewWorker>(6, TimeUnit.HOURS).build(),
        )
    }
}

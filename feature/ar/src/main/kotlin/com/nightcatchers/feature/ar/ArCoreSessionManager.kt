package com.nightcatchers.feature.ar

import android.content.Context
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Session
import javax.inject.Inject

class ArCoreSessionManager @Inject constructor() {

    private var session: Session? = null

    fun isArCoreAvailable(context: Context): Boolean =
        ArCoreApk.getInstance().checkAvailability(context).isSupported

    fun createSession(context: Context): Session? {
        return try {
            Session(context).also { s ->
                val config = Config(s).apply {
                    planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                    lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                    depthMode = if (s.isDepthModeSupported(Config.DepthMode.AUTOMATIC))
                        Config.DepthMode.AUTOMATIC else Config.DepthMode.DISABLED
                }
                s.configure(config)
                session = s
            }
        } catch (e: Exception) {
            null
        }
    }

    fun resume() = session?.resume()
    fun pause() = session?.pause()

    fun destroy() {
        session?.close()
        session = null
    }
}

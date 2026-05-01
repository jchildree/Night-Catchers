package com.nightcatchers.core.common

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.opengl.EGL14
import android.opengl.GLES20
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceTierDetector @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun detect(): DeviceCapabilities {
        val ram = getRamMb()
        val hasArCore = hasArCoreSupport()
        val glVersion = getOpenGlEsVersion()

        val tier = when {
            ram >= 6_000 && hasArCore && glVersion >= 3.0f -> DeviceTier.A
            ram >= 3_000 && hasArCore && glVersion >= 2.0f -> DeviceTier.B
            else -> DeviceTier.C
        }

        return DeviceCapabilities(
            tier = tier,
            totalRamMb = ram,
            supportsArCore = hasArCore,
            openGlEsVersion = glVersion,
            maxTextureSize = getMaxTextureSize(),
        )
    }

    private fun getRamMb(): Int {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        return (info.totalMem / 1_048_576L).toInt()
    }

    private fun hasArCoreSupport(): Boolean =
        context.packageManager.hasSystemFeature("android.hardware.camera.ar") ||
            try {
                context.packageManager.getPackageInfo("com.google.ar.core", 0)
                true
            } catch (_: PackageManager.NameNotFoundException) {
                false
            }

    private fun getOpenGlEsVersion(): Float {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val config = am.deviceConfigurationInfo
        return config.reqGlEsVersion.toFloat() / 0x10000
    }

    private fun getMaxTextureSize(): Int {
        val result = IntArray(1)
        GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, result, 0)
        return if (result[0] > 0) result[0] else 2048
    }
}

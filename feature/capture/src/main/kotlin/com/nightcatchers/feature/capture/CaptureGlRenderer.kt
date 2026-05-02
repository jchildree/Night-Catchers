package com.nightcatchers.feature.capture

import android.opengl.GLES30
import com.nightcatchers.feature.filters.FilterLayerManager
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * GLSurfaceView.Renderer that drives the FilterLayerManager shader pipeline.
 * Runs on the GL thread; beam coordinates are published from the UI thread.
 */
internal class CaptureGlRenderer(
    private val filterLayerManager: FilterLayerManager,
) : android.opengl.GLSurfaceView.Renderer {

    @Volatile var beamX: Float = 0.5f
    @Volatile var beamY: Float = 0.5f

    private val startMs = System.currentTimeMillis()

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        GLES30.glClearColor(0f, 0f, 0f, 0f)
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        val elapsedSecs = (System.currentTimeMillis() - startMs) / 1000f
        filterLayerManager.renderFrame(elapsedSecs, beamX, beamY)
    }
}

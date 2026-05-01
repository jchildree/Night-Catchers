package com.nightcatchers.feature.filters

import android.content.Context
import com.nightcatchers.core.common.DeviceTier
import com.nightcatchers.core.domain.model.LensId
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val MAX_SIMULTANEOUS_LENSES = 2
private const val FRAME_BUDGET_DROP_THRESHOLD_MS = 20L

/**
 * Manages the active AR lens stack.
 *
 * Rules (section 17):
 * - Maximum 2 lenses active simultaneously.
 * - Proton Pack is always top-most when a monster anchor is live.
 * - Celebration lenses are solo — they suspend all others via replaceAll().
 * - Night Vision and Ecto-Goggles share a filter slot (selecting one pops the other).
 * - Shaders are compiled in ShaderProgramCache on idle frames; never on the main thread.
 */
@Singleton
class FilterLayerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val shaderCache: ShaderProgramCache,
    private val uniformBinder: GlslUniformBinder,
) {
    private val lensStack = ArrayDeque<LensId>(MAX_SIMULTANEOUS_LENSES)
    private var savedStack: List<LensId> = emptyList()
    private var deviceTier: DeviceTier = DeviceTier.A
    private var consecutiveSlowFrames = 0

    fun configure(tier: DeviceTier) {
        deviceTier = tier
    }

    /** Push a lens onto the stack (max 2 simultaneously). */
    fun push(lensId: LensId): Boolean {
        if (!canPush(lensId)) return false

        // Night Vision and Ecto-Goggles share a slot — evict the other.
        if (lensId == LensId.NIGHT_VISION) lensStack.remove(LensId.ECTO_GOGGLES)
        if (lensId == LensId.ECTO_GOGGLES) lensStack.remove(LensId.NIGHT_VISION)

        // Keep Proton Pack at the top.
        if (lensId == LensId.PROTON_PACK) {
            lensStack.remove(LensId.PROTON_PACK)
            lensStack.addLast(LensId.PROTON_PACK)
        } else {
            if (lensStack.size >= MAX_SIMULTANEOUS_LENSES) lensStack.removeFirst()
            lensStack.addLast(lensId)
            // Re-raise Proton Pack to top if present.
            if (LensId.PROTON_PACK in lensStack) {
                lensStack.remove(LensId.PROTON_PACK)
                lensStack.addLast(LensId.PROTON_PACK)
            }
        }
        return true
    }

    /** Pop the top lens. */
    fun pop(): LensId? = lensStack.removeLastOrNull()

    /** Replace the entire stack with a celebration lens. Saves the previous stack for restoring. */
    fun replaceAll(lensId: LensId) {
        savedStack = lensStack.toList()
        lensStack.clear()
        lensStack.addLast(lensId)
    }

    /** Restore the stack that existed before a celebration lens. */
    fun restorePrevious() {
        lensStack.clear()
        savedStack.forEach { lensStack.addLast(it) }
        savedStack = emptyList()
    }

    /** Returns true if the lens can be pushed given current device tier + perf budget. */
    fun canPush(lensId: LensId): Boolean {
        if (deviceTier == DeviceTier.C && lensId != LensId.NIGHT_VISION && lensId != LensId.BIRTHDAY_MODE) return false
        if (deviceTier !in lensId.supportedTiers) return false
        if (lensId.isCelebration) return true   // celebration lenses always allowed (replaceAll is used)
        return lensStack.size < MAX_SIMULTANEOUS_LENSES || lensId in lensStack
    }

    fun activeLenses(): List<LensId> = lensStack.toList()

    fun renderFrame(elapsedSecs: Float, beamOriginX: Float, beamOriginY: Float) {
        if (deviceTier == DeviceTier.C) return

        val frameStart = System.nanoTime()

        lensStack.forEach { lensId ->
            val filterLayer = lensId.toFilterLayer() ?: return@forEach
            val program = shaderCache.getOrCreate(context, filterLayer)
            bindLensUniforms(program.id, lensId, elapsedSecs, beamOriginX, beamOriginY)
            drawFullscreenQuad(program.id)
        }

        val frameMs = (System.nanoTime() - frameStart) / 1_000_000L
        if (frameMs > FRAME_BUDGET_DROP_THRESHOLD_MS) {
            consecutiveSlowFrames++
            if (consecutiveSlowFrames >= 3) {
                deviceTier = DeviceTier.C   // emergency downgrade
                consecutiveSlowFrames = 0
            }
        } else {
            consecutiveSlowFrames = 0
        }
    }

    fun release() = shaderCache.releaseAll()

    private fun LensId.toFilterLayer(): FilterLayer? = when (this) {
        LensId.PROTON_PACK -> FilterLayer.PROTON_BEAM_GLOW
        LensId.NIGHT_VISION -> FilterLayer.NIGHT_VISION
        LensId.ECTO_GOGGLES -> FilterLayer.ECTOPLASM_SPLATTER
        LensId.EVOLUTION_BURST -> FilterLayer.SLIME_VIGNETTE
        LensId.BIRTHDAY_MODE -> null    // Tier C Lottie; no GL pass
    }

    private fun bindLensUniforms(
        programId: Int,
        lensId: LensId,
        time: Float,
        beamX: Float,
        beamY: Float,
    ) {
        uniformBinder.bindFloat(programId, "u_Time", time)
        uniformBinder.bindVec2(programId, "u_Resolution", 1080f, 1920f)
        when (lensId) {
            LensId.PROTON_PACK -> {
                uniformBinder.bindVec2(programId, "u_BeamOrigin", beamX, beamY)
                uniformBinder.bindVec4(programId, "u_BeamColor", 0.25f, 0.75f, 1.0f, 1.0f)
            }
            LensId.NIGHT_VISION -> {
                uniformBinder.bindFloat(programId, "u_GreenTint", 0.85f)
                uniformBinder.bindFloat(programId, "u_ScanlineIntensity", 0.18f)
            }
            LensId.ECTO_GOGGLES -> {
                uniformBinder.bindFloat(programId, "u_TrailIntensity", 0.5f)
            }
            LensId.EVOLUTION_BURST -> {
                uniformBinder.bindFloat(programId, "u_BurstProgress", (time % 4f) / 4f)
            }
            LensId.BIRTHDAY_MODE -> { /* handled by Lottie */ }
        }
    }

    private fun drawFullscreenQuad(programId: Int) {
        android.opengl.GLES30.glUseProgram(programId)
        android.opengl.GLES30.glDrawArrays(android.opengl.GLES30.GL_TRIANGLES, 0, 3)
    }
}

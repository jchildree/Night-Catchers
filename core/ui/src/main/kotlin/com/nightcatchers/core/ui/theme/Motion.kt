package com.nightcatchers.core.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

// ── Duration tokens (section 09) ──
object MotionDuration {
    const val INSTANT = 0
    const val FAST = 150
    const val NORMAL = 250
    const val SLOW = 400
    const val DRAMATIC = 800
}

// ── Spring specs (section 09) ──
object MotionSpring {
    fun button() = spring<Float>(stiffness = 400f, dampingRatio = 0.7f)
    fun monster() = spring<Float>(stiffness = 180f, dampingRatio = 0.6f)
}

// ── Tween specs ──
fun fastTween() = tween<Float>(MotionDuration.FAST)
fun normalTween() = tween<Float>(MotionDuration.NORMAL)
fun dramaticTween() = tween<Float>(MotionDuration.DRAMATIC)

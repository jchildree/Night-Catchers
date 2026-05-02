package com.nightcatchers.core.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nightcatchers.core.domain.model.Mood

@Composable
fun MonsterAvatar(
    emoji: String,
    size: Dp = 96.dp,
    mood: Mood? = null,
    isAnimated: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "avatar_idle")
    val durationMs = if (!isAnimated) 1 else mood.animDuration()

    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (isAnimated) mood.scaleTarget() else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "avatar_scale",
    )

    // SPOOKED: fast horizontal shake
    val shakeOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (isAnimated && mood == Mood.SPOOKED) 6f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(80, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "avatar_shake",
    )

    // SLEEPY / LONELY: slow vertical droop
    val droopOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (isAnimated && (mood == Mood.SLEEPY || mood == Mood.LONELY)) 4f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "avatar_droop",
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = emoji,
            fontSize = (size.value * 0.65f).sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .scale(scale)
                .offset(x = shakeOffset.dp, y = droopOffset.dp),
        )

        // Small mood badge in bottom-right corner
        if (mood != null) {
            Text(
                text = mood.badgeEmoji(),
                fontSize = (size.value * 0.22f).sp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 2.dp, y = 2.dp),
            )
        }
    }
}

private fun Mood?.animDuration(): Int = when (this) {
    Mood.EXCITED -> 600
    Mood.PLAYFUL -> 700
    Mood.SPOOKED -> 80
    Mood.SLEEPY  -> 2200
    Mood.LONELY  -> 2000
    Mood.GRUMPY  -> 900
    Mood.CONTENT -> 1200
    null         -> 1200
}

private fun Mood?.scaleTarget(): Float = when (this) {
    Mood.EXCITED -> 1.10f
    Mood.PLAYFUL -> 1.08f
    Mood.SPOOKED -> 1.04f
    Mood.SLEEPY  -> 1.02f
    Mood.LONELY  -> 1.02f
    Mood.GRUMPY  -> 1.05f
    Mood.CONTENT -> 1.06f
    null         -> 1.06f
}

private fun Mood.badgeEmoji(): String = when (this) {
    Mood.CONTENT -> "😊"
    Mood.EXCITED -> "🤩"
    Mood.LONELY  -> "😢"
    Mood.GRUMPY  -> "😤"
    Mood.SLEEPY  -> "😴"
    Mood.PLAYFUL -> "😜"
    Mood.SPOOKED -> "😱"
}

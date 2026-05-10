package com.nightcatchers.feature.pet.play

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcatchers.core.domain.model.MiniGameOutcome
import com.nightcatchers.core.ui.theme.DeepNight
import com.nightcatchers.core.ui.theme.MintFresh
import com.nightcatchers.core.ui.theme.PeachWarm
import com.nightcatchers.core.ui.theme.PetRoomBgTop
import com.nightcatchers.core.ui.theme.RarityGold
import com.nightcatchers.core.ui.theme.SoftLavender
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun CuddleStormScreen(
    onSessionComplete: (MiniGameOutcome) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CuddleStormViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CuddleStormEvent.SessionComplete -> onSessionComplete(event.outcome)
            }
        }
    }

    val particles = remember { mutableStateListOf<HeartParticle>() }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PetRoomBgTop, DeepNight)))
            .pointerInput(state.phase) {
                if (state.phase != GamePhase.RUNNING) return@pointerInput
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Press) {
                            event.changes.forEach { change ->
                                viewModel.onTap()
                                particles.spawnBurst(change.position)
                                scope.launch {
                                    delay(800)
                                    if (particles.size > MAX_PARTICLES) {
                                        repeat(particles.size - MAX_PARTICLES) {
                                            if (particles.isNotEmpty()) particles.removeAt(0)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
    ) {
        ParticleLayer(particles)
        GameContent(state = state)
    }
}

@Composable
private fun GameContent(state: CuddleStormState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TimeBar(timeRemainingMs = state.timeRemainingMs)
        Spacer(Modifier.height(24.dp))
        EmotionMonster(tier = state.emotionTier, tapCount = state.tapCount)
        Spacer(Modifier.height(24.dp))
        TapCounter(state = state)
        Spacer(Modifier.height(16.dp))
        InstructionText(state = state)
    }
}

@Composable
private fun TimeBar(timeRemainingMs: Long) {
    val progress = (timeRemainingMs / 10_000f).coerceIn(0f, 1f)
    val animated by animateFloatAsState(targetValue = progress, label = "time_bar")
    LinearProgressIndicator(
        progress = { animated },
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
        color = SoftLavender,
        trackColor = SoftLavender.copy(alpha = 0.18f),
    )
}

@Composable
private fun EmotionMonster(tier: EmotionTier, tapCount: Int) {
    val targetScale = when (tier) {
        EmotionTier.NEUTRAL -> 1f
        EmotionTier.PLEASED -> 1.06f
        EmotionTier.HAPPY -> 1.12f
        EmotionTier.OVERJOYED -> 1.2f
    }
    val scale by animateFloatAsState(targetValue = targetScale, label = "monster_scale")
    val emoji = when (tier) {
        EmotionTier.NEUTRAL -> "👾"
        EmotionTier.PLEASED -> "😊"
        EmotionTier.HAPPY -> "🥰"
        EmotionTier.OVERJOYED -> "🤩"
    }
    val tierColor = when (tier) {
        EmotionTier.NEUTRAL -> Color.White
        EmotionTier.PLEASED -> SoftLavender
        EmotionTier.HAPPY -> PeachWarm
        EmotionTier.OVERJOYED -> RarityGold
    }
    val haloAlpha = when (tier) {
        EmotionTier.NEUTRAL -> 0.18f
        EmotionTier.PLEASED -> 0.25f
        EmotionTier.HAPPY -> 0.3f
        EmotionTier.OVERJOYED -> 0.35f
    }
    Box(
        modifier = Modifier
            .padding(8.dp)
            .background(
                brush = Brush.radialGradient(
                    listOf(tierColor.copy(alpha = haloAlpha), Color.Transparent),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = emoji,
            fontSize = (96 * scale).sp,
        )
    }
    Text(
        text = tier.label(),
        style = MaterialTheme.typography.labelMedium,
        color = tierColor,
    )
    Text(
        text = "$tapCount hugs",
        style = MaterialTheme.typography.labelSmall,
        color = Color.White.copy(alpha = 0.5f),
    )
}

@Composable
private fun TapCounter(state: CuddleStormState) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Black.copy(alpha = 0.35f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "${state.timeRemainingMs / 1000}s",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = SoftLavender,
            )
            Text(
                text = "left",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun InstructionText(state: CuddleStormState) {
    val message = when (state.phase) {
        GamePhase.RUNNING -> "Tap as fast as you can — every tap is a hug!"
        GamePhase.FINISHED -> "Time's up. ${state.tapCount} hugs delivered ✨"
    }
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MintFresh.copy(alpha = 0.85f),
    )
}

@Composable
private fun ParticleLayer(particles: List<HeartParticle>) {
    var nowMs by remember { mutableStateOf(System.currentTimeMillis()) }
    val hasParticles = particles.isNotEmpty()
    LaunchedEffect(hasParticles) {
        if (!hasParticles) return@LaunchedEffect
        while (true) {
            delay(16)
            nowMs = System.currentTimeMillis()
        }
    }
    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { p ->
            val age = nowMs - p.spawnedAt
            if (age < 0 || age > p.lifetimeMs) return@forEach
            val t = age / p.lifetimeMs.toFloat()
            val x = p.origin.x + cos(p.angleRad) * p.speed * t
            val y = p.origin.y + sin(p.angleRad) * p.speed * t - 200f * t * t
            val alpha = (1f - t).coerceIn(0f, 1f)
            drawCircle(
                color = p.color.copy(alpha = alpha),
                radius = 10f + 6f * (1f - t),
                center = Offset(x, y),
            )
        }
    }
}

private fun MutableList<HeartParticle>.spawnBurst(at: Offset) {
    val nowMs = System.currentTimeMillis()
    repeat(BURST_COUNT) {
        val angle = Random.nextDouble(0.0, Math.PI * 2).toFloat()
        val speed = Random.nextFloat() * 240f + 120f
        val color = HEART_COLORS.random()
        add(HeartParticle(origin = at, angleRad = angle, speed = speed, color = color, spawnedAt = nowMs))
    }
}

private fun EmotionTier.label(): String = when (this) {
    EmotionTier.NEUTRAL -> "Curious"
    EmotionTier.PLEASED -> "Pleased"
    EmotionTier.HAPPY -> "Happy"
    EmotionTier.OVERJOYED -> "Overjoyed!"
}

private data class HeartParticle(
    val origin: Offset,
    val angleRad: Float,
    val speed: Float,
    val color: Color,
    val spawnedAt: Long,
    val lifetimeMs: Long = 800L,
)

private val HEART_COLORS = listOf(
    Color(0xFFFF6B9D), // rose
    SoftLavender,
    RarityGold,
    MintFresh,
)

private const val BURST_COUNT = 7
private const val MAX_PARTICLES = 80

package com.nightcatchers.feature.pet.play

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.nightcatchers.core.ui.theme.SlimeGreen
import com.nightcatchers.core.ui.theme.SoftLavender
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.sqrt

@Composable
fun FoodTossScreen(
    onSessionComplete: (MiniGameOutcome) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FoodTossViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is FoodTossEvent.SessionComplete -> onSessionComplete(event.outcome)
            }
        }
    }

    // Drag aim state
    var dragStart by remember { mutableStateOf<Offset?>(null) }
    var dragCurrent by remember { mutableStateOf<Offset?>(null) }

    // Flight animation state (purely visual — VM drives phase independently)
    var throwInFlight by remember { mutableStateOf(false) }
    var flightOrigin by remember { mutableStateOf(Offset.Zero) }
    var flightTarget by remember { mutableStateOf(Offset.Zero) }
    var flightProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(throwInFlight) {
        if (!throwInFlight) return@LaunchedEffect
        val startMs = System.currentTimeMillis()
        while (flightProgress < 1f) {
            delay(16)
            flightProgress = ((System.currentTimeMillis() - startMs) / FoodTossViewModel.THROW_ANIM_MS.toFloat())
                .coerceIn(0f, 1f)
        }
        throwInFlight = false
        flightProgress = 0f
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PetRoomBgTop, DeepNight))),
    ) {
        // HUD pinned to top
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ThrowHud(state = state)
        }

        // Canvas: geometry, aim line, flight arc
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(state.phase) {
                    if (state.phase != FoodTossPhase.AIMING) return@pointerInput
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: continue
                            when (event.type) {
                                PointerEventType.Press -> {
                                    // Only accept touches from the lower half
                                    if (change.position.y > size.height * 0.5f) {
                                        dragStart = change.position
                                        dragCurrent = change.position
                                    }
                                }
                                PointerEventType.Move -> {
                                    if (dragStart != null) {
                                        dragCurrent = change.position
                                        change.consume()
                                    }
                                }
                                PointerEventType.Release -> {
                                    val start = dragStart
                                    val current = dragCurrent
                                    if (start != null && current != null) {
                                        val w = size.width.toFloat()
                                        val h = size.height.toFloat()
                                        val foodPos = Offset(w / 2f, h * FOOD_Y_FRAC)
                                        val mouthCenter = Offset(w / 2f, h * MOUTH_Y_FRAC)
                                        val mouthRadius = w * MOUTH_RADIUS_FRAC

                                        val rawVec = current - start
                                        val len = sqrt(rawVec.x * rawVec.x + rawVec.y * rawVec.y)
                                        if (len > MIN_DRAG_PX) {
                                            val aimVec = rawVec / len
                                            // Only accept upward throws
                                            if (aimVec.y < -0.1f) {
                                                val t = (mouthCenter.y - foodPos.y) / aimVec.y
                                                val landX = foodPos.x + aimVec.x * t
                                                val landTarget = Offset(landX, mouthCenter.y)
                                                val hit = abs(landX - mouthCenter.x) < mouthRadius

                                                flightOrigin = foodPos
                                                flightTarget = landTarget
                                                flightProgress = 0f
                                                throwInFlight = true
                                                viewModel.onThrow(hit)
                                            }
                                        }
                                        dragStart = null
                                        dragCurrent = null
                                    }
                                }
                                else -> Unit
                            }
                        }
                    }
                },
        ) {
            val w = size.width
            val h = size.height
            val mouthCenter = Offset(w / 2f, h * MOUTH_Y_FRAC)
            val mouthRadius = w * MOUTH_RADIUS_FRAC
            val foodPos = Offset(w / 2f, h * FOOD_Y_FRAC)

            // Monster halo
            val haloColor = when (state.lastThrowHit) {
                true -> SlimeGreen.copy(alpha = 0.22f)
                false -> Color(0xFFFF4D4D).copy(alpha = 0.15f)
                null -> SoftLavender.copy(alpha = 0.10f)
            }
            drawCircle(color = haloColor, radius = w * 0.18f, center = mouthCenter)

            // Mouth target ring
            val ringColor = when {
                throwInFlight && state.lastThrowHit == true -> SlimeGreen.copy(alpha = 0.7f)
                throwInFlight && state.lastThrowHit == false -> Color(0xFFFF4D4D).copy(alpha = 0.5f)
                else -> SoftLavender.copy(alpha = 0.30f)
            }
            drawCircle(
                color = ringColor,
                radius = mouthRadius,
                center = mouthCenter,
                style = Stroke(
                    width = 4f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 9f)),
                ),
            )

            // Aim line during drag
            val ds = dragStart
            val dc = dragCurrent
            if (!throwInFlight && ds != null && dc != null) {
                val rawVec = dc - ds
                val len = sqrt(rawVec.x * rawVec.x + rawVec.y * rawVec.y)
                if (len > MIN_DRAG_PX) {
                    val aimVec = rawVec / len
                    if (aimVec.y < -0.1f) {
                        val t = (mouthCenter.y - foodPos.y) / aimVec.y
                        val landTarget = Offset(foodPos.x + aimVec.x * t, mouthCenter.y)
                        // Dotted line food → projected landing
                        drawLine(
                            color = RarityGold.copy(alpha = 0.65f),
                            start = foodPos,
                            end = landTarget,
                            strokeWidth = 4f,
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f)),
                        )
                        // Landing indicator
                        val inMouth = abs(landTarget.x - mouthCenter.x) < mouthRadius
                        drawCircle(
                            color = if (inMouth) SlimeGreen.copy(alpha = 0.55f) else RarityGold.copy(alpha = 0.40f),
                            radius = 14f,
                            center = landTarget,
                        )
                    }
                }
            }

            // Food in flight — parabolic arc
            if (throwInFlight) {
                val t = flightProgress
                val lerped = Offset(
                    x = flightOrigin.x + (flightTarget.x - flightOrigin.x) * t,
                    y = flightOrigin.y + (flightTarget.y - flightOrigin.y) * t,
                )
                val arcBulge = 90f * 4f * t * (1f - t)
                val pos = lerped.copy(y = lerped.y - arcBulge)
                // Shadow
                drawCircle(color = Color.Black.copy(alpha = 0.25f), radius = 18f, center = pos.copy(y = pos.y + 6f))
                drawCircle(color = PeachWarm, radius = 18f, center = pos)
                drawCircle(color = Color.White.copy(alpha = 0.4f), radius = 18f, center = pos, style = Stroke(width = 2.5f))
            }
        }

        // Monster emoji overlay
        val monsterEmoji = when (state.lastThrowHit) {
            true -> "😋"
            false -> "😅"
            null -> "👾"
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 72.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            Text(text = monsterEmoji, fontSize = 84.sp)
        }

        // Food + instruction overlay (hidden while ball is in flight)
        if (!throwInFlight && state.phase == FoodTossPhase.AIMING) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🍖", fontSize = 42.sp)
                    Text(
                        text = "Drag up to aim • release to throw",
                        style = MaterialTheme.typography.labelMedium,
                        color = MintFresh.copy(alpha = 0.8f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ThrowHud(state: FoodTossState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.Black.copy(alpha = 0.40f),
        ) {
            Text(
                text = "Throw ${FoodTossViewModel.TOTAL_THROWS - state.throwsRemaining + 1} of ${FoodTossViewModel.TOTAL_THROWS}",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = SoftLavender,
            )
        }
        Spacer(Modifier.width(12.dp))
        // Hit pips
        Row {
            repeat(FoodTossViewModel.TOTAL_THROWS) { i ->
                val filled = i < state.hits
                val isCurrent = !filled && i == state.hits && state.phase == FoodTossPhase.ANIMATING
                Text(
                    text = when {
                        filled -> "🟢"
                        isCurrent && state.lastThrowHit == false -> "🔴"
                        else -> "⚪"
                    },
                    fontSize = 20.sp,
                    modifier = Modifier.padding(end = 4.dp),
                )
            }
        }
    }
}

private const val MOUTH_Y_FRAC = 0.24f
private const val FOOD_Y_FRAC = 0.74f
private const val MOUTH_RADIUS_FRAC = 0.12f
private const val MIN_DRAG_PX = 20f

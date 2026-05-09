package com.nightcatchers.feature.pet.minigames.foodtoss

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcatchers.core.ui.theme.DeepNight
import com.nightcatchers.core.ui.theme.DeepVoid
import com.nightcatchers.core.ui.theme.EctoplasmCyan
import com.nightcatchers.core.ui.theme.MonsterPurple
import com.nightcatchers.core.ui.theme.PeachWarm
import com.nightcatchers.core.ui.theme.SlimeGreen
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.math.sqrt
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput

private const val THROW_ANIM_DURATION_MS = 700
private const val HIT_ZONE_HALF_WIDTH_DP = 100f
private const val FEEDBACK_DISPLAY_MS = 800L

@Composable
fun FoodTossGameScreen(
    monsterId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FoodTossViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DeepNight, DeepVoid))),
    ) {
        when (val state = uiState) {
            FoodTossUiState.Loading -> {
                CircularProgressIndicator(
                    color = SlimeGreen,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            is FoodTossUiState.TooFull -> {
                TooFullScreen(
                    message = state.message,
                    onBack = onNavigateBack,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            is FoodTossUiState.Playing -> {
                PlayingScreen(
                    state = state,
                    onDragStart = viewModel::onDragStart,
                    onDragUpdate = viewModel::onDragUpdate,
                    onDragRelease = viewModel::onDragRelease,
                    onThrowComplete = viewModel::onThrowComplete,
                )
            }

            is FoodTossUiState.Complete -> {
                CompleteScreen(
                    hits = state.hits,
                    totalThrows = state.totalThrows,
                    onDone = onNavigateBack,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}

// ── TooFull ───────────────────────────────────────────────────────────────────

@Composable
private fun TooFullScreen(
    message: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = "🤰", fontSize = 72.sp)
        Text(
            text = message,
            style = MaterialTheme.typography.headlineSmall,
            color = PeachWarm,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Come back when your monster is hungry again!",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = MonsterPurple),
        ) {
            Text("Go Back")
        }
    }
}

// ── Complete ──────────────────────────────────────────────────────────────────

@Composable
private fun CompleteScreen(
    hits: Int,
    totalThrows: Int,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val resultEmoji = when {
        hits == totalThrows -> "🌟"
        hits >= 2 -> "😋"
        hits >= 1 -> "😊"
        else -> "😅"
    }
    val resultText = when {
        hits == totalThrows -> "Perfect score!"
        hits >= 2 -> "Great throwing!"
        hits >= 1 -> "Not bad!"
        else -> "Better luck next time!"
    }

    Surface(
        modifier = modifier.padding(24.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.Black.copy(alpha = 0.75f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 36.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = resultEmoji, fontSize = 64.sp)
            Text(
                text = resultText,
                style = MaterialTheme.typography.headlineSmall,
                color = SlimeGreen,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Hits: $hits / $totalThrows",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(totalThrows) { i ->
                    Text(text = if (i < hits) "✅" else "⭕", fontSize = 24.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onDone,
                colors = ButtonDefaults.buttonColors(containerColor = SlimeGreen),
            ) {
                Text(text = "Done", color = DeepNight, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── Playing ───────────────────────────────────────────────────────────────────

@Composable
private fun PlayingScreen(
    state: FoodTossUiState.Playing,
    onDragStart: (Offset) -> Unit,
    onDragUpdate: (Offset) -> Unit,
    onDragRelease: () -> Unit,
    onThrowComplete: (Boolean) -> Unit,
) {
    val density = LocalDensity.current
    var feedbackEmoji by remember { mutableStateOf<String?>(null) }
    var showFeedback by remember { mutableStateOf(false) }

    // Flying food animation state: 0f = at origin (bottom), 1f = at target (monster)
    val flyProgress = remember { Animatable(0f) }
    var flyingFoodOffset by remember { mutableStateOf(Offset.Zero) }
    // Starting position (food origin) and landing position computed from drag vector
    var throwOriginPx by remember { mutableStateOf(Offset.Zero) }
    var throwTargetPx by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(state.isFlying, state.dragOffset) {
        if (state.isFlying) {
            flyProgress.snapTo(0f)
            flyingFoodOffset = throwOriginPx
            flyProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(THROW_ANIM_DURATION_MS, easing = LinearEasing),
            )
            // Determine hit: landing x within HIT_ZONE_HALF_WIDTH_DP of screen center
            val hitZoneHalfPx = with(density) { HIT_ZONE_HALF_WIDTH_DP.dp.toPx() }
            val isHit = kotlin.math.abs(throwTargetPx.x) < hitZoneHalfPx

            feedbackEmoji = if (isHit) "😋" else "😢"
            showFeedback = true
            delay(FEEDBACK_DISPLAY_MS)
            showFeedback = false
            onThrowComplete(isHit)
            flyProgress.snapTo(0f)
        }
    }

    // Update the flying food visual offset each frame from animatable progress
    LaunchedEffect(flyProgress.value, throwOriginPx, throwTargetPx) {
        val t = flyProgress.value
        // Cubic Bézier: P0=origin, P1=control point above midpoint, P2=target
        val midX = (throwOriginPx.x + throwTargetPx.x) / 2f
        val controlY = minOf(throwOriginPx.y, throwTargetPx.y) - 300f
        val bx = lerp(lerp(throwOriginPx.x, midX, t), lerp(midX, throwTargetPx.x, t), t)
        val by = lerp(
            lerp(throwOriginPx.y, controlY, t),
            lerp(controlY, throwTargetPx.y, t),
            t,
        )
        flyingFoodOffset = Offset(bx, by)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ── Monster zone (top 40%) ────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize(0.4f)
                .align(Alignment.TopCenter),
            contentAlignment = Alignment.Center,
        ) {
            MonsterZone(monsterEmoji = state.monsterEmoji)
        }

        // ── HUD ───────────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ThrowCounter(throwsRemaining = state.throwsRemaining)
            HitCounter(hits = state.hits, total = MAX_THROWS)
        }

        // ── Slingshot / drag arc preview ─────────────────────────────────────
        if (!state.isFlying && state.dragOffset != Offset.Zero) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val foodOriginX = size.width / 2f
                val foodOriginY = size.height * 0.82f
                val drag = state.dragOffset
                // Launch direction is opposite to drag (slingshot)
                val targetX = foodOriginX - drag.x * 3f
                val targetY = foodOriginY - drag.y * 3f
                val midX = (foodOriginX + targetX) / 2f
                val controlY = minOf(foodOriginY, targetY) - 200f

                val path = Path()
                path.moveTo(foodOriginX, foodOriginY)
                path.quadraticBezierTo(midX, controlY, targetX, targetY)

                drawPath(
                    path = path,
                    color = EctoplasmCyan.copy(alpha = 0.5f),
                    style = Stroke(
                        width = 3f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f)),
                    ),
                )
            }
        }

        // ── Flying food ───────────────────────────────────────────────────────
        if (state.isFlying) {
            Text(
                text = state.currentFood.emoji,
                fontSize = 36.sp,
                modifier = Modifier
                    .offset {
                        IntOffset(
                            flyingFoodOffset.x.roundToInt(),
                            flyingFoodOffset.y.roundToInt(),
                        )
                    },
            )
        }

        // ── Food drag zone (bottom of screen) ────────────────────────────────
        if (!state.isFlying) {
            FoodDragZone(
                food = state.currentFood,
                onDragStart = { pos ->
                    val foodOriginX = pos.x
                    val foodOriginY = pos.y
                    throwOriginPx = Offset(foodOriginX, foodOriginY)
                    onDragStart(pos)
                },
                onDragUpdate = { delta ->
                    val drag = state.dragOffset + delta
                    val targetX = throwOriginPx.x - drag.x * 3f
                    val targetY = throwOriginPx.y - drag.y * 3f
                    throwTargetPx = Offset(targetX, targetY)
                    onDragUpdate(delta)
                },
                onDragRelease = onDragRelease,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp),
            )
        }

        // ── Hit/Miss feedback ─────────────────────────────────────────────────
        AnimatedVisibility(
            visible = showFeedback,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier.align(Alignment.Center),
        ) {
            feedbackEmoji?.let { emoji ->
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.6f),
                ) {
                    Text(
                        text = emoji,
                        fontSize = 72.sp,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
    }
}

// ── Monster zone ──────────────────────────────────────────────────────────────

@Composable
private fun MonsterZone(monsterEmoji: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = monsterEmoji, fontSize = 80.sp)
        // Mouth hit-zone visualised as a glowing circle hint
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SlimeGreen.copy(alpha = 0.3f),
                            SlimeGreen.copy(alpha = 0f),
                        ),
                    ),
                    shape = CircleShape,
                ),
        )
    }
}

// ── Food drag zone ────────────────────────────────────────────────────────────

@Composable
private fun FoodDragZone(
    food: com.nightcatchers.core.domain.model.minigames.FoodItem,
    onDragStart: (Offset) -> Unit,
    onDragUpdate: (Offset) -> Unit,
    onDragRelease: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Drag & Release to throw",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.5f),
        )
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    color = Color.White.copy(alpha = 0.08f),
                    shape = CircleShape,
                )
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            when (event.type) {
                                PointerEventType.Press -> {
                                    val pos = event.changes.firstOrNull()?.position ?: Offset.Zero
                                    onDragStart(pos)
                                }
                                PointerEventType.Move -> {
                                    val change = event.changes.firstOrNull()
                                    if (change != null) {
                                        val delta = change.position - change.previousPosition
                                        onDragUpdate(delta)
                                        change.consume()
                                    }
                                }
                                PointerEventType.Release -> {
                                    onDragRelease()
                                }
                                else -> Unit
                            }
                        }
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(text = food.emoji, fontSize = 36.sp)
        }
    }
}

// ── HUD components ────────────────────────────────────────────────────────────

@Composable
private fun ThrowCounter(throwsRemaining: Int) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Black.copy(alpha = 0.4f),
    ) {
        Text(
            text = "Throws left: $throwsRemaining",
            style = MaterialTheme.typography.labelLarge,
            color = PeachWarm,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun HitCounter(hits: Int, total: Int) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Black.copy(alpha = 0.4f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "Hits:",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.7f),
            )
            repeat(total) { i ->
                Text(text = if (i < hits) "✅" else "⭕", fontSize = 14.sp)
            }
        }
    }
}

// ── Utilities ─────────────────────────────────────────────────────────────────

private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

private const val MAX_THROWS = 3

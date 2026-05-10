package com.nightcatchers.feature.pet.minigames.slimesort

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcatchers.core.domain.model.minigames.BlobColor
import com.nightcatchers.core.domain.model.minigames.BlobEntity
import com.nightcatchers.core.domain.model.minigames.JarEntity
import com.nightcatchers.core.ui.theme.DeepNight
import com.nightcatchers.core.ui.theme.EctoplasmCyan
import com.nightcatchers.core.ui.theme.MonsterPurple
import com.nightcatchers.core.ui.theme.SlimeGreen
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun SlimeSortGameScreen(
    monsterId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SlimeSortViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Track which jar has a blob hovering over it for glow highlight
    var highlightedJarId by remember { mutableStateOf<String?>(null) }

    // Explosion overlay for phantom splats
    var showExplosion by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SlimeSortEvent.BlobSplatted -> {
                    if (event.isPhantom) {
                        showExplosion = true
                        delay(800)
                        showExplosion = false
                    }
                }
                else -> Unit
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNight),
        contentAlignment = Alignment.Center,
    ) {
        when (val s = state) {
            is SlimeSortUiState.Loading -> {
                CircularProgressIndicator(color = EctoplasmCyan)
            }

            is SlimeSortUiState.Playing -> {
                PlayingContent(
                    state = s,
                    highlightedJarId = highlightedJarId,
                    onBlobDragStart = viewModel::onBlobDragStart,
                    onBlobDragUpdate = { offset ->
                        viewModel.onBlobDragUpdate(offset)
                        // Simple hit-test: find the first jar whose rough area overlaps
                        // We update highlightedJarId based on vertical position (jars are at bottom)
                        // Actual per-jar hit-testing done at release time in onBlobReleased
                        highlightedJarId = null
                    },
                    onBlobReleased = { jarId ->
                        highlightedJarId = null
                        viewModel.onBlobReleased(jarId)
                    },
                    onJarHover = { jarId -> highlightedJarId = jarId },
                )
            }

            is SlimeSortUiState.Complete -> {
                CompleteContent(
                    score = s.score,
                    maxCombo = s.maxCombo,
                    onDone = onNavigateBack,
                )
            }
        }

        // Phantom explosion overlay
        AnimatedVisibility(
            visible = showExplosion,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier.align(Alignment.Center),
        ) {
            Text(
                text = "💥",
                fontSize = 80.sp,
            )
        }
    }
}

// ── Playing content ───────────────────────────────────────────────────────────

@Composable
private fun PlayingContent(
    state: SlimeSortUiState.Playing,
    highlightedJarId: String?,
    onBlobDragStart: (String, Offset) -> Unit,
    onBlobDragUpdate: (Offset) -> Unit,
    onBlobReleased: (String?) -> Unit,
    onJarHover: (String?) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header row — Round + Score
        HeaderRow(
            round = state.round,
            score = state.score,
            missesThisRound = state.missesThisRound,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        )

        // Monster area (top 20%)
        MonsterArea(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.2f),
        )

        // Blob play area (middle 50%)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            state.blobs.forEach { activeBlob ->
                BlobItem(
                    activeBlob = activeBlob,
                    onDragStart = { offset -> onBlobDragStart(activeBlob.entity.id, offset) },
                    onDragUpdate = onBlobDragUpdate,
                    onReleased = { jarId -> onBlobReleased(jarId) },
                )
            }

            // Combo badge
            AnimatedVisibility(
                visible = state.combo >= 3,
                enter = scaleIn() + fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
            ) {
                ComboBadge(combo = state.combo)
            }
        }

        // Jars area (bottom 30%)
        JarsRow(
            jars = state.jars,
            highlightedJarId = highlightedJarId,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun HeaderRow(
    round: Int,
    score: Int,
    missesThisRound: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Round $round / 3",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Score: $score",
            style = MaterialTheme.typography.titleMedium,
            color = SlimeGreen,
            fontWeight = FontWeight.Bold,
        )
        // Miss indicator — show 3 pips
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            color = if (index < missesThisRound) Color.Red else Color.White.copy(alpha = 0.3f),
                            shape = CircleShape,
                        ),
                )
            }
        }
    }
}

@Composable
private fun MonsterArea(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "👾",
            fontSize = 72.sp,
        )
    }
}

@Composable
private fun BlobItem(
    activeBlob: ActiveBlob,
    onDragStart: (Offset) -> Unit,
    onDragUpdate: (Offset) -> Unit,
    onReleased: (String?) -> Unit,
) {
    val density = LocalDensity.current
    // Animated position for newly spawned blobs (fall from top)
    val animatedPosition by animateOffsetAsState(
        targetValue = if (activeBlob.isBeingDragged) activeBlob.position else activeBlob.position,
        animationSpec = tween(durationMillis = 300),
        label = "blob_position_${activeBlob.entity.id}",
    )

    val blobAlpha = if (activeBlob.entity.isPhantom) 0.4f else 1f
    val blobColor = activeBlob.entity.color.toComposeColor()

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = animatedPosition.x.roundToInt(),
                    y = animatedPosition.y.roundToInt(),
                )
            }
            .size(64.dp)
            .pointerInput(activeBlob.entity.id) {
                detectDragGestures(
                    onDragStart = { offset ->
                        onDragStart(
                            Offset(
                                x = with(density) { activeBlob.position.x + offset.x },
                                y = with(density) { activeBlob.position.y + offset.y },
                            ),
                        )
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDragUpdate(
                            Offset(
                                x = activeBlob.position.x + dragAmount.x,
                                y = activeBlob.position.y + dragAmount.y,
                            ),
                        )
                    },
                    onDragEnd = { onReleased(null) },
                    onDragCancel = { onReleased(null) },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = blobColor.copy(alpha = blobAlpha),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = activeBlob.entity.shape.label,
                color = Color.White.copy(alpha = blobAlpha),
                fontSize = 22.sp,
            )
        }
    }
}

@Composable
private fun JarsRow(
    jars: List<JarEntity>,
    highlightedJarId: String?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        jars.forEach { jar ->
            JarCard(
                jar = jar,
                isHighlighted = jar.id == highlightedJarId,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun JarCard(
    jar: JarEntity,
    isHighlighted: Boolean,
    modifier: Modifier = Modifier,
) {
    val glowColor = if (isHighlighted) EctoplasmCyan.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.08f)
    Surface(
        modifier = modifier.height(88.dp),
        shape = RoundedCornerShape(16.dp),
        color = glowColor,
        tonalElevation = if (isHighlighted) 8.dp else 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = jar.acceptsColor.emoji,
                fontSize = 22.sp,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = jar.acceptsShape.label,
                fontSize = 18.sp,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun ComboBadge(combo: Int) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SlimeGreen.copy(alpha = 0.2f),
    ) {
        Text(
            text = "COMBO ×$combo",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = SlimeGreen,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
        )
    }
}

// ── Complete screen ───────────────────────────────────────────────────────────

@Composable
private fun CompleteContent(
    score: Int,
    maxCombo: Int,
    onDone: () -> Unit,
) {
    Column(
        modifier = Modifier
            .wrapContentSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "🫧",
            fontSize = 64.sp,
        )
        Text(
            text = "Slime Sorted!",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White.copy(alpha = 0.1f),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ScoreLine(label = "Score", value = "$score")
                ScoreLine(label = "Best Combo", value = "×$maxCombo")
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onDone,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EctoplasmCyan),
        ) {
            Text(
                text = "Done",
                color = DeepNight,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun ScoreLine(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(100.dp),
            textAlign = TextAlign.End,
        )
        Text(
            text = value,
            color = SlimeGreen,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

// ── Color helpers ─────────────────────────────────────────────────────────────

private fun BlobColor.toComposeColor(): Color = when (this) {
    BlobColor.RED -> Color(0xFFE53935)
    BlobColor.BLUE -> Color(0xFF1E88E5)
    BlobColor.GREEN -> SlimeGreen
    BlobColor.YELLOW -> Color(0xFFFDD835)
}

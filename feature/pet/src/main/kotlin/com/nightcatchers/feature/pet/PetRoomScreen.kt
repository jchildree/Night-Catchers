package com.nightcatchers.feature.pet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcatchers.core.domain.model.Mood
import com.nightcatchers.core.domain.model.PetInteraction
import com.nightcatchers.core.domain.model.RoomStage
import com.nightcatchers.core.ui.component.MonsterAvatar
import com.nightcatchers.core.ui.component.StatBar
import com.nightcatchers.core.ui.theme.ButteryYellow
import com.nightcatchers.core.ui.theme.DeepNight
import com.nightcatchers.core.ui.theme.DeepVoid
import com.nightcatchers.core.ui.theme.MintFresh
import com.nightcatchers.core.ui.theme.MonsterPurple
import com.nightcatchers.core.ui.theme.PeachWarm
import com.nightcatchers.core.ui.theme.PetRoomBgBottom
import com.nightcatchers.core.ui.theme.PetRoomBgTop
import com.nightcatchers.core.ui.theme.SkyBlue
import com.nightcatchers.core.ui.theme.SoftLavender
import com.nightcatchers.core.ui.theme.StatEnergy
import com.nightcatchers.core.ui.theme.StatHappiness
import com.nightcatchers.core.ui.theme.StatHunger
import com.nightcatchers.core.ui.theme.StatSpookiness
import com.nightcatchers.core.ui.theme.StatTrust
import kotlinx.coroutines.delay

@Composable
fun PetRoomScreen(
    monsterId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PetViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        when (val s = state) {
            is PetUiState.Loading -> CircularProgressIndicator(color = SoftLavender)
            is PetUiState.Error -> Text(
                text = s.message,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(24.dp),
            )
            is PetUiState.Ready -> PetRoomContent(
                state = s,
                onInteract = viewModel::onInteract,
                onDismissResult = viewModel::dismissInteractionResult,
            )
        }
    }
}

@Composable
private fun PetRoomContent(
    state: PetUiState.Ready,
    onInteract: (PetInteraction) -> Unit,
    onDismissResult: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        RoomBackground(roomStage = state.roomStage)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RoomStageHeader(state = state)
            Spacer(Modifier.height(16.dp))
            MonsterAvatarSection(state = state)
            Spacer(Modifier.height(20.dp))
            StatsPanel(state = state)
            Spacer(Modifier.height(20.dp))
            InteractionGrid(
                state = state,
                onInteract = onInteract,
            )
        }

        AnimatedVisibility(
            visible = state.lastInteractionResult != null,
            enter = scaleIn() + fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center),
        ) {
            state.lastInteractionResult?.let { result ->
                InteractionBurst(result = result)
                LaunchedEffect(result) {
                    delay(1_500)
                    onDismissResult()
                }
            }
        }
    }
}

@Composable
private fun RoomBackground(roomStage: RoomStage) {
    val (topColor, bottomColor) = roomStage.backgroundColors()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(topColor, bottomColor))),
    )

    if (roomStage == RoomStage.DREAM_ROOM) {
        FireflyOverlay()
    }
}

@Composable
private fun FireflyOverlay() {
    val transition = rememberInfiniteTransition(label = "firefly")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "firefly_alpha",
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF00FF88).copy(alpha = alpha * 0.04f)),
    )
}

@Composable
private fun RoomStageHeader(state: PetUiState.Ready) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = state.roomStage.label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.6f),
        )
        if (state.roomStage != RoomStage.DREAM_ROOM) {
            Spacer(Modifier.height(4.dp))
            val progress = (state.stats.trust - state.roomStage.trustMin).toFloat() /
                (state.trustToNextStage - state.roomStage.trustMin).toFloat()
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = SoftLavender,
                trackColor = SoftLavender.copy(alpha = 0.2f),
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Trust ${state.stats.trust} / ${state.trustToNextStage} → ${state.roomStage.nextLabel()}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.4f),
            )
        }
    }
}

@Composable
private fun MonsterAvatarSection(state: PetUiState.Ready) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            MonsterAvatar(
                emoji = state.emoji,
                size = 120.dp,
                mood = state.mood,
                isAnimated = !state.isInteracting,
            )
            if (state.isInteracting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(140.dp),
                    color = SoftLavender.copy(alpha = 0.6f),
                    strokeWidth = 2.dp,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = state.displayName,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
        )
        Text(
            text = state.archetype.subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.5f),
        )
        Spacer(Modifier.height(4.dp))
        MoodChip(mood = state.mood)
    }
}

@Composable
private fun MoodChip(mood: Mood) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = mood.chipColor().copy(alpha = 0.25f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = mood.emoji(), fontSize = 14.sp)
            Spacer(Modifier.width(4.dp))
            Text(
                text = mood.label(),
                style = MaterialTheme.typography.labelMedium,
                color = mood.chipColor(),
            )
        }
    }
}

@Composable
private fun StatsPanel(state: PetUiState.Ready) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.Black.copy(alpha = 0.3f),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Pet Stats",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.7f),
            )
            Spacer(Modifier.height(8.dp))
            StatBar(label = "Hunger",    value = state.stats.hunger,    color = StatHunger)
            StatBar(label = "Happiness", value = state.stats.happiness, color = StatHappiness)
            StatBar(label = "Energy",    value = state.stats.energy,    color = StatEnergy)
            StatBar(label = "Spookiness",value = state.stats.spookiness,color = StatSpookiness)
            StatBar(label = "Trust",     value = state.stats.trust,     color = StatTrust)
        }
    }
}

@Composable
private fun InteractionGrid(
    state: PetUiState.Ready,
    onInteract: (PetInteraction) -> Unit,
) {
    val interactions = listOf(
        Triple(PetInteraction.Feed,    "Feed",    "🍖"),
        Triple(PetInteraction.Play,    "Play",    "🎮"),
        Triple(PetInteraction.Train,   "Train",   "🏋️"),
        Triple(PetInteraction.Story,   "Story",   "📖"),
        Triple(PetInteraction.Comfort, "Comfort", "🤗"),
        Triple(PetInteraction.Praise,  "Praise",  "⭐"),
    )

    Column {
        Text(
            text = "Interact",
            style = MaterialTheme.typography.labelLarge,
            color = Color.White.copy(alpha = 0.7f),
        )
        Spacer(Modifier.height(10.dp))
        interactions.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                row.forEach { (interaction, label, emoji) ->
                    InteractionButton(
                        label = label,
                        emoji = emoji,
                        enabled = !state.isInteracting,
                        onClick = { onInteract(interaction) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun InteractionButton(
    label: String,
    emoji: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.12f),
            contentColor = Color.White,
            disabledContainerColor = Color.White.copy(alpha = 0.05f),
            disabledContentColor = Color.White.copy(alpha = 0.3f),
        ),
        contentPadding = PaddingValues(4.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = emoji, fontSize = 22.sp)
            Text(text = label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun InteractionBurst(result: InteractionResult) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Black.copy(alpha = 0.7f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = result.emoji, fontSize = 42.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                text = result.label,
                style = MaterialTheme.typography.titleMedium,
                color = SoftLavender,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun RoomStage.backgroundColors(): Pair<Color, Color> = when (this) {
    RoomStage.HOLDING_PEN -> Pair(Color(0xFF1A1A2E), Color(0xFF0D0D1A))
    RoomStage.COSY_CORNER -> Pair(Color(0xFF2D1B4E), Color(0xFF1A0F2E))
    RoomStage.BEDROOM     -> Pair(Color(0xFF1E1040), PetRoomBgBottom)
    RoomStage.SANCTUARY   -> Pair(PetRoomBgTop, Color(0xFF0A1628))
    RoomStage.DREAM_ROOM  -> Pair(Color(0xFF0D0A2E), Color(0xFF050318))
}

private fun RoomStage.nextLabel(): String = when (this) {
    RoomStage.HOLDING_PEN -> RoomStage.COSY_CORNER.label
    RoomStage.COSY_CORNER -> RoomStage.BEDROOM.label
    RoomStage.BEDROOM     -> RoomStage.SANCTUARY.label
    RoomStage.SANCTUARY   -> RoomStage.DREAM_ROOM.label
    RoomStage.DREAM_ROOM  -> "Max"
}

private fun Mood.emoji(): String = when (this) {
    Mood.CONTENT  -> "😊"
    Mood.EXCITED  -> "🤩"
    Mood.LONELY   -> "😢"
    Mood.GRUMPY   -> "😤"
    Mood.SLEEPY   -> "😴"
    Mood.PLAYFUL  -> "😜"
    Mood.SPOOKED  -> "😱"
}

private fun Mood.label(): String = when (this) {
    Mood.CONTENT  -> "Content"
    Mood.EXCITED  -> "Excited"
    Mood.LONELY   -> "Lonely"
    Mood.GRUMPY   -> "Grumpy"
    Mood.SLEEPY   -> "Sleepy"
    Mood.PLAYFUL  -> "Playful"
    Mood.SPOOKED  -> "Spooked"
}

private fun Mood.chipColor(): Color = when (this) {
    Mood.CONTENT  -> MintFresh
    Mood.EXCITED  -> ButteryYellow
    Mood.LONELY   -> SkyBlue
    Mood.GRUMPY   -> Color(0xFFFF6F61)
    Mood.SLEEPY   -> SoftLavender
    Mood.PLAYFUL  -> PeachWarm
    Mood.SPOOKED  -> MonsterPurple
}

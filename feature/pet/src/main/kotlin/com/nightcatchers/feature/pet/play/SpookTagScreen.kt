package com.nightcatchers.feature.pet.play

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcatchers.core.domain.model.MiniGameOutcome
import com.nightcatchers.core.ui.theme.DeepNight
import com.nightcatchers.core.ui.theme.MintFresh
import com.nightcatchers.core.ui.theme.PetRoomBgTop
import com.nightcatchers.core.ui.theme.RarityGold
import com.nightcatchers.core.ui.theme.SlimeGreen
import com.nightcatchers.core.ui.theme.SoftLavender
import com.nightcatchers.core.ui.theme.SurfaceDark

@Composable
fun SpookTagScreen(
    onSessionComplete: (MiniGameOutcome) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SpookTagViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SpookTagEvent.SessionComplete -> onSessionComplete(event.outcome)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PetRoomBgTop, DeepNight))),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SpookTagHud(state = state)
            Spacer(Modifier.height(16.dp))
            PhaseLabel(phase = state.phase)
            Spacer(modifier = Modifier.weight(1f))
            DoorRow(state = state, onDoorTap = viewModel::onDoorTap)
            Spacer(modifier = Modifier.weight(1f))
            InstructionText(phase = state.phase)
        }
    }
}

@Composable
private fun SpookTagHud(state: SpookTagState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(shape = RoundedCornerShape(12.dp), color = Color.Black.copy(alpha = 0.40f)) {
            Text(
                text = "Round ${state.currentRound} / ${SpookTagViewModel.TOTAL_ROUNDS}",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = SoftLavender,
            )
        }
        Surface(shape = RoundedCornerShape(12.dp), color = Color.Black.copy(alpha = 0.40f)) {
            Text(
                text = "✓ ${state.score} correct",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = SlimeGreen,
            )
        }
    }
}

@Composable
private fun PhaseLabel(phase: SpookTagPhase) {
    val (text, color) = when (phase) {
        SpookTagPhase.SHOWING -> "👀  Watch the monster!" to RarityGold
        SpookTagPhase.SHUFFLING -> "🔀  Shuffling..." to SoftLavender
        SpookTagPhase.CHOOSING -> "🚪  Where is it?" to MintFresh
        SpookTagPhase.REVEALING -> "✨  Revealing..." to SoftLavender
        SpookTagPhase.FINISHED -> "🎉  Done!" to RarityGold
    }
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = color,
    )
}

@Composable
private fun DoorRow(state: SpookTagState, onDoorTap: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        repeat(3) { slot ->
            DoorCard(
                slot = slot,
                content = state.slotContents.getOrElse(slot) { DoorContent.EMPTY },
                isOpen = state.doorsOpen.getOrElse(slot) { false },
                isSwapping = state.swappingSlots?.let { it.first == slot || it.second == slot } ?: false,
                isPlayerChoice = state.playerChoice == slot,
                phase = state.phase,
                onTap = onDoorTap,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DoorCard(
    slot: Int,
    content: DoorContent,
    isOpen: Boolean,
    isSwapping: Boolean,
    isPlayerChoice: Boolean,
    phase: SpookTagPhase,
    onTap: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val clickable = phase == SpookTagPhase.CHOOSING

    val borderWidth: Dp
    val borderColor: Color
    when {
        isSwapping -> {
            borderWidth = 2.dp
            borderColor = RarityGold
        }
        isPlayerChoice && phase == SpookTagPhase.REVEALING -> {
            borderWidth = 2.dp
            borderColor = if (content == DoorContent.MONSTER) SlimeGreen else Color(0xFFFF4D4D)
        }
        !isPlayerChoice && isOpen && phase == SpookTagPhase.REVEALING -> {
            // Monster's actual door revealed when player guessed wrong
            borderWidth = 2.dp
            borderColor = SlimeGreen.copy(alpha = 0.6f)
        }
        clickable -> {
            borderWidth = 1.dp
            borderColor = SoftLavender.copy(alpha = 0.45f)
        }
        else -> {
            borderWidth = 1.dp
            borderColor = SoftLavender.copy(alpha = 0.18f)
        }
    }

    val animatedBorderColor by animateColorAsState(
        targetValue = borderColor,
        animationSpec = tween(durationMillis = 160),
        label = "door_border_$slot",
    )

    val scale by animateFloatAsState(
        targetValue = if (isSwapping) 0.93f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "door_scale_$slot",
    )

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = SurfaceDark,
        modifier = modifier
            .scale(scale)
            .border(borderWidth, animatedBorderColor, RoundedCornerShape(18.dp))
            .clickable(enabled = clickable) { onTap(slot) },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            contentAlignment = Alignment.Center,
        ) {
            val emoji = when {
                !isOpen -> "🚪"
                content == DoorContent.MONSTER -> "👾"
                else -> "✨"
            }
            val resultBadge = when {
                isPlayerChoice && phase == SpookTagPhase.REVEALING && content == DoorContent.MONSTER -> "✓"
                isPlayerChoice && phase == SpookTagPhase.REVEALING && content == DoorContent.EMPTY -> "✗"
                else -> null
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = emoji, fontSize = 52.sp)
                if (resultBadge != null) {
                    Text(
                        text = resultBadge,
                        fontSize = 22.sp,
                        color = if (resultBadge == "✓") SlimeGreen else Color(0xFFFF4D4D),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun InstructionText(phase: SpookTagPhase) {
    val text = when (phase) {
        SpookTagPhase.SHOWING -> "Remember which door the monster is behind"
        SpookTagPhase.SHUFFLING -> "Keep your eyes on the monster's door!"
        SpookTagPhase.CHOOSING -> "Tap the door where you think the monster is hiding"
        SpookTagPhase.REVEALING -> ""
        SpookTagPhase.FINISHED -> ""
    }
    if (text.isNotEmpty()) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.55f),
        )
    } else {
        Spacer(Modifier.size(20.dp))
    }
}

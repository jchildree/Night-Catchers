package com.nightcatchers.feature.pet.play

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcatchers.core.domain.model.MiniGameId
import com.nightcatchers.core.domain.model.MiniGameTier
import com.nightcatchers.core.domain.usecase.IsMiniGameUnlockedUseCase
import com.nightcatchers.core.ui.theme.DeepNight
import com.nightcatchers.core.ui.theme.MintFresh
import com.nightcatchers.core.ui.theme.PetRoomBgTop
import com.nightcatchers.core.ui.theme.RarityGold
import com.nightcatchers.core.ui.theme.SlimeGreen
import com.nightcatchers.core.ui.theme.SoftLavender

@Composable
fun PlayMenuScreen(
    onPlayGame: (MiniGameId) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayMenuViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PetRoomBgTop, DeepNight))),
    ) {
        when (val s = state) {
            PlayMenuUiState.Loading -> CircularProgressIndicator(
                color = SoftLavender,
                modifier = Modifier.align(Alignment.Center),
            )
            is PlayMenuUiState.Ready -> PlayMenuContent(
                state = s,
                onPlayGame = onPlayGame,
                onNavigateBack = onNavigateBack,
            )
        }
    }
}

@Composable
private fun PlayMenuContent(
    state: PlayMenuUiState.Ready,
    onPlayGame: (MiniGameId) -> Unit,
    onNavigateBack: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { PlayMenuHeader(onNavigateBack = onNavigateBack) }
        item { SectionLabel(label = "FEATURED TODAY", color = RarityGold) }
        items(state.cards.filter { it.isFeatured }, key = { it.id.name }) { card ->
            MiniGameCardRow(card = card, onPlayGame = onPlayGame)
        }
        item { SectionLabel(label = "BONDING GAMES", color = SoftLavender) }
        items(
            state.cards.filter { !it.isFeatured && it.id.tier == MiniGameTier.BONDING },
            key = { it.id.name },
        ) { card ->
            MiniGameCardRow(card = card, onPlayGame = onPlayGame)
        }
        item { SectionLabel(label = "SKILL GAMES", color = SlimeGreen) }
        items(
            state.cards.filter { !it.isFeatured && it.id.tier == MiniGameTier.SKILL },
            key = { it.id.name },
        ) { card ->
            MiniGameCardRow(card = card, onPlayGame = onPlayGame)
        }
    }
}

@Composable
private fun PlayMenuHeader(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.12f),
            modifier = Modifier
                .size(40.dp)
                .clickable(onClick = onNavigateBack),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = "←", fontSize = 20.sp, color = Color.White)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = "Play",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
            )
            Text(
                text = "6 mini-games · pick your mood",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun SectionLabel(label: String, color: Color) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.6.sp),
        color = color,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun MiniGameCardRow(
    card: MiniGameCard,
    onPlayGame: (MiniGameId) -> Unit,
) {
    val isLocked = card.unlockState is IsMiniGameUnlockedUseCase.UnlockState.Locked
    val borderColor = when {
        card.isFeatured -> RarityGold
        isLocked -> Color.White.copy(alpha = 0.08f)
        card.id.tier == MiniGameTier.SKILL -> SlimeGreen.copy(alpha = 0.4f)
        else -> SoftLavender.copy(alpha = 0.4f)
    }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isLocked) Color.Black.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.3f),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (card.isFeatured) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(enabled = !isLocked) { onPlayGame(card.id) },
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EmojiBadge(emoji = card.id.emoji, tinted = card.isFeatured)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = card.id.displayName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                    if (card.isFeatured) {
                        Spacer(Modifier.width(6.dp))
                        Text(text = "★", color = RarityGold, fontSize = 16.sp)
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${card.id.tier.label()} · Energy ${card.id.energyCost}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f),
                )
                Spacer(Modifier.height(4.dp))
                LockedFooter(card.unlockState)
            }
        }
    }
}

@Composable
private fun EmojiBadge(emoji: String, tinted: Boolean) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (tinted) RarityGold.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.08f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = emoji, fontSize = 26.sp)
    }
}

@Composable
private fun LockedFooter(state: IsMiniGameUnlockedUseCase.UnlockState) {
    when (state) {
        IsMiniGameUnlockedUseCase.UnlockState.Unlocked -> {
            Text(
                text = "Tap to play",
                style = MaterialTheme.typography.labelSmall,
                color = MintFresh,
            )
        }
        is IsMiniGameUnlockedUseCase.UnlockState.Locked -> {
            val text = when (val r = state.reason) {
                is IsMiniGameUnlockedUseCase.LockReason.LowEnergy ->
                    "🔒 Needs Energy ≥ ${r.required} (now ${r.current})"

                IsMiniGameUnlockedUseCase.LockReason.TooFull -> "🔒 Too full to eat right now"
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.4f),
            )
        }
    }
}

private fun MiniGameTier.label(): String = when (this) {
    MiniGameTier.BONDING -> "Bonding"
    MiniGameTier.SKILL -> "Skill"
}

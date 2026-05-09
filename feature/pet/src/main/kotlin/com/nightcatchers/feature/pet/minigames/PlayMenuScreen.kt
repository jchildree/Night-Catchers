package com.nightcatchers.feature.pet.minigames

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcatchers.core.domain.model.minigames.MiniGameId
import com.nightcatchers.core.domain.model.minigames.MiniGameTier
import com.nightcatchers.core.ui.theme.DeepNight
import com.nightcatchers.core.ui.theme.EctoplasmCyan
import com.nightcatchers.core.ui.theme.MintFresh
import com.nightcatchers.core.ui.theme.MonsterPurple
import com.nightcatchers.core.ui.theme.PeachWarm
import com.nightcatchers.core.ui.theme.PetRoomBgBottom
import com.nightcatchers.core.ui.theme.PetRoomBgTop
import com.nightcatchers.core.ui.theme.SlimeGreen
import com.nightcatchers.core.ui.theme.SoftLavender

@Composable
fun PlayMenuScreen(
    monsterId: String,
    onNavigateToGame: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayMenuViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DeepNight, PetRoomBgBottom))),
    ) {
        when (val s = state) {
            is PlayMenuUiState.Loading -> CircularProgressIndicator(
                color = SoftLavender,
                modifier = Modifier.align(Alignment.Center),
            )
            is PlayMenuUiState.Error -> Text(
                text = s.message,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
            )
            is PlayMenuUiState.Ready -> PlayMenuContent(
                state = s,
                onNavigateToGame = onNavigateToGame,
                onNavigateBack = onNavigateBack,
            )
        }
    }
}

@Composable
private fun PlayMenuContent(
    state: PlayMenuUiState.Ready,
    onNavigateToGame: (String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp),
    ) {
        Text(
            text = "Mini Games",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Energy: ${state.petStats.energy}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.6f),
        )
        Spacer(Modifier.height(16.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(MiniGameId.entries) { game ->
                val isPlayable = state.gamesAvailability[game] ?: false
                val isFeatured = game == state.featuredGame
                GameCard(
                    game = game,
                    isPlayable = isPlayable,
                    isFeatured = isFeatured,
                    onClick = { if (isPlayable) onNavigateToGame(game.gameId) },
                )
            }
        }
    }
}

@Composable
private fun GameCard(
    game: MiniGameId,
    isPlayable: Boolean,
    isFeatured: Boolean,
    onClick: () -> Unit,
) {
    val borderModifier = if (isFeatured) {
        Modifier.border(
            width = 2.dp,
            brush = Brush.linearGradient(listOf(EctoplasmCyan, SoftLavender)),
            shape = RoundedCornerShape(16.dp),
        )
    } else {
        Modifier
    }

    Box(
        modifier = borderModifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = if (isPlayable) 0.1f else 0.05f))
            .clickable(enabled = isPlayable, onClick = onClick),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = game.emoji, fontSize = 36.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = game.displayName,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isPlayable) Color.White else Color.White.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            TierBadge(tier = game.tier)
            Spacer(Modifier.height(6.dp))
            GameRewardHints(game = game, isPlayable = isPlayable)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "⚡ ${game.energyCost}",
                style = MaterialTheme.typography.labelSmall,
                color = if (isPlayable) MintFresh else Color.White.copy(alpha = 0.3f),
            )
            if (isFeatured) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "⭐ Featured",
                    style = MaterialTheme.typography.labelSmall,
                    color = EctoplasmCyan,
                )
            }
        }

        if (!isPlayable) {
            LockOverlay()
        }
    }
}

@Composable
private fun TierBadge(tier: MiniGameTier) {
    val (label, color) = when (tier) {
        MiniGameTier.BONDING -> Pair("Bonding", PeachWarm)
        MiniGameTier.SKILL -> Pair("Skill", SlimeGreen)
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.2f),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun GameRewardHints(game: MiniGameId, isPlayable: Boolean) {
    val delta = com.nightcatchers.core.domain.model.minigames.GameRewards.forGame(game)
    val hints = buildList {
        if (delta.happiness != 0) add("Happiness ${delta.happiness.signedStr()}")
        if (delta.hunger != 0) add("Hunger ${delta.hunger.signedStr()}")
        if (delta.trust != 0) add("Trust ${delta.trust.signedStr()}")
        if (delta.spookiness != 0) add("Spookiness ${delta.spookiness.signedStr()}")
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        hints.take(2).forEach { hint ->
            Text(
                text = hint,
                style = MaterialTheme.typography.labelSmall,
                color = if (isPlayable) SoftLavender.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.25f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun LockOverlay() {
    Box(
        modifier = Modifier
            .matchParentSize()
            .background(Color.Black.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "🔒", fontSize = 24.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Need more energy",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun Int.signedStr(): String = if (this >= 0) "+$this" else "$this"

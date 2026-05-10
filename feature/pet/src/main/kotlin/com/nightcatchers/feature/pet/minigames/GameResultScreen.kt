package com.nightcatchers.feature.pet.minigames

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcatchers.core.domain.model.minigames.GameStatDelta
import com.nightcatchers.core.domain.model.minigames.MiniGameId
import com.nightcatchers.core.ui.theme.DeepNight
import com.nightcatchers.core.ui.theme.MintFresh
import com.nightcatchers.core.ui.theme.PetRoomBgBottom
import com.nightcatchers.core.ui.theme.PetRoomBgTop
import com.nightcatchers.core.ui.theme.SlimeGreen
import com.nightcatchers.core.ui.theme.SoftLavender

@Composable
fun GameResultScreen(
    onPlayAgain: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameResultViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PetRoomBgTop, PetRoomBgBottom))),
        contentAlignment = Alignment.Center,
    ) {
        when (val s = state) {
            is GameResultUiState.Loading -> CircularProgressIndicator(color = SoftLavender)
            is GameResultUiState.Error -> Text(
                text = s.message,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(24.dp),
            )
            is GameResultUiState.Ready -> GameResultContent(
                state = s,
                onPlayAgain = onPlayAgain,
                onBack = onBack,
            )
        }
    }
}

@Composable
private fun GameResultContent(
    state: GameResultUiState.Ready,
    onPlayAgain: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Game Over!",
            style = MaterialTheme.typography.labelLarge,
            color = SoftLavender,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = state.game.emoji,
            fontSize = 72.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = state.game.displayName,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
        )
        Spacer(Modifier.height(24.dp))
        StatDeltaCard(delta = state.delta)
        Spacer(Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(text = "Back to Pet", color = Color.White)
            }
            Button(
                onClick = onPlayAgain,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SoftLavender),
            ) {
                Text(
                    text = "Play Again",
                    color = DeepNight,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun StatDeltaCard(delta: GameStatDelta) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Black.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Rewards",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MintFresh,
            )
            Spacer(Modifier.height(10.dp))
            if (delta.hunger != 0) StatRow(label = "Hunger", value = delta.hunger)
            if (delta.happiness != 0) StatRow(label = "Happiness", value = delta.happiness)
            if (delta.energy != 0) StatRow(label = "Energy", value = delta.energy)
            if (delta.spookiness != 0) StatRow(label = "Spookiness", value = delta.spookiness)
            if (delta.trust != 0) StatRow(label = "Trust", value = delta.trust)
        }
    }
}

@Composable
private fun StatRow(label: String, value: Int) {
    val color = if (value > 0) SlimeGreen else Color(0xFFFF6F61)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.8f),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (value >= 0) "+$value" else "$value",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = color,
        )
    }
}

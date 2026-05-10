package com.nightcatchers.feature.pet.play

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nightcatchers.core.domain.model.MiniGameId
import com.nightcatchers.core.domain.model.StatDelta
import com.nightcatchers.core.ui.theme.CoralAccent
import com.nightcatchers.core.ui.theme.DeepNight
import com.nightcatchers.core.ui.theme.MintFresh
import com.nightcatchers.core.ui.theme.PetRoomBgTop
import com.nightcatchers.core.ui.theme.RarityGold
import com.nightcatchers.core.ui.theme.SoftLavender
import com.nightcatchers.core.ui.theme.StatEnergy
import com.nightcatchers.core.ui.theme.StatHappiness
import com.nightcatchers.core.ui.theme.StatHunger
import com.nightcatchers.core.ui.theme.StatSpookiness
import com.nightcatchers.core.ui.theme.StatTrust

/**
 * Shared result screen for all 6 mini-games. Receives the outcome via navigation args
 * (decoded by the caller) so this composable is purely presentational.
 */
@Composable
fun MiniGameResultScreen(
    gameId: MiniGameId,
    rawScore: Int,
    scoreFraction: Float,
    statDelta: StatDelta,
    onPlayAgain: () -> Unit,
    onBackToMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PetRoomBgTop, DeepNight))),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = gameId.emoji,
                fontSize = 72.sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = gameId.displayName,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
            )
            Spacer(Modifier.height(4.dp))
            StarRating(scoreFraction = scoreFraction)
            Spacer(Modifier.height(12.dp))
            ScoreCallout(rawScore = rawScore)
            Spacer(Modifier.height(20.dp))
            StatDeltaCard(delta = statDelta)
            Spacer(Modifier.height(28.dp))
            ButtonRow(onPlayAgain = onPlayAgain, onBackToMenu = onBackToMenu)
        }
    }
}

@Composable
private fun StarRating(scoreFraction: Float) {
    val stars = when {
        scoreFraction >= 0.85f -> 3
        scoreFraction >= 0.55f -> 2
        else -> 1
    }
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(3) { index ->
            val filled = index < stars
            Text(
                text = if (filled) "★" else "☆",
                color = if (filled) RarityGold else Color.White.copy(alpha = 0.3f),
                fontSize = 28.sp,
            )
        }
    }
}

@Composable
private fun ScoreCallout(rawScore: Int) {
    Text(
        text = "Score · $rawScore",
        style = MaterialTheme.typography.titleMedium,
        color = SoftLavender,
    )
}

@Composable
private fun StatDeltaCard(delta: StatDelta) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Black.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "STAT REWARDS",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.6.sp),
                color = MintFresh,
            )
            Spacer(Modifier.height(12.dp))
            DeltaRow("Hunger", delta.hunger, StatHunger)
            DeltaRow("Happiness", delta.happiness, StatHappiness)
            DeltaRow("Trust", delta.trust, StatTrust)
            DeltaRow("Spookiness", delta.spookiness, StatSpookiness)
            DeltaRow("Energy", delta.energy, StatEnergy)
        }
    }
}

@Composable
private fun DeltaRow(label: String, value: Int, color: Color) {
    if (value == 0) return
    val isNegative = value < 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
        )
        Text(
            text = (if (value > 0) "+" else "") + value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = if (isNegative) CoralAccent else color,
        )
    }
}

@Composable
private fun ButtonRow(onPlayAgain: () -> Unit, onBackToMenu: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(
            onClick = onBackToMenu,
            modifier = Modifier.weight(1f),
        ) {
            Text(text = "Back")
        }
        Button(
            onClick = onPlayAgain,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = SoftLavender,
                contentColor = DeepNight,
            ),
        ) {
            Text(text = "Play Again", fontWeight = FontWeight.Bold)
        }
    }
}

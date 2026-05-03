package com.nightcatchers.feature.pet

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcatchers.core.domain.model.EvolutionStage
import com.nightcatchers.core.ui.theme.DeepNight
import com.nightcatchers.core.ui.theme.MonsterPurple
import com.nightcatchers.core.ui.theme.RarityGold
import com.nightcatchers.core.ui.theme.SoftLavender

@Composable
fun PetEvolveScreen(
    monsterId: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PetViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val (emoji, displayName, stage) = when (val s = state) {
        is PetUiState.Ready -> Triple(s.emoji, s.displayName, s.evolutionStage)
        else -> Triple("✨", "Monster", EvolutionStage.TEEN)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "evolve_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "evolve_scale",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(MonsterPurple, DeepNight))),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp),
        ) {
            Text(
                text = "✨ Evolved! ✨",
                style = MaterialTheme.typography.labelLarge,
                color = RarityGold,
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = emoji,
                fontSize = 96.sp,
                modifier = Modifier.scale(scale),
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = displayName,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MonsterPurple.copy(alpha = 0.4f),
            ) {
                Text(
                    text = "Evolved to ${stage.label()}!",
                    style = MaterialTheme.typography.titleMedium,
                    color = SoftLavender,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                )
            }
            Spacer(Modifier.height(44.dp))
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SoftLavender),
                modifier = Modifier.fillMaxWidth(0.6f),
            ) {
                Text(
                    text = "Woohoo!",
                    style = MaterialTheme.typography.titleMedium,
                    color = DeepNight,
                )
            }
        }
    }
}

private fun EvolutionStage.label(): String = when (this) {
    EvolutionStage.BABY -> "Baby"
    EvolutionStage.TEEN -> "Teen"
    EvolutionStage.ADULT -> "Adult"
}

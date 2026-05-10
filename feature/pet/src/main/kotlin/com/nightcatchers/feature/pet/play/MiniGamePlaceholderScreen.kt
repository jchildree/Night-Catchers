package com.nightcatchers.feature.pet.play

import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcatchers.core.domain.model.MiniGameId
import com.nightcatchers.core.domain.model.MiniGameOutcome
import com.nightcatchers.core.domain.usecase.ApplyMiniGameOutcomeUseCase
import com.nightcatchers.core.ui.theme.DeepNight
import com.nightcatchers.core.ui.theme.PetRoomBgTop
import com.nightcatchers.core.ui.theme.SoftLavender
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Temporary placeholder for the 5 mini-games whose interactive UIs are still being built
 * (Food Toss, Spook Tag, Slime Sort, Ghost Dash, Proton Wrangle). Tapping the "Finish"
 * button awards the standard 60% scoreFraction reward — enough to feel rewarding without
 * making the placeholder farmable.
 */
@Composable
fun MiniGamePlaceholderScreen(
    gameId: MiniGameId,
    onSessionComplete: (MiniGameOutcome) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaceholderGameViewModel = hiltViewModel(),
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PetRoomBgTop, DeepNight))),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(32.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = gameId.emoji, fontSize = 84.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                text = gameId.displayName,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Coming soon — playable preview only",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    viewModel.completePlaceholderSession(gameId, onSessionComplete)
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SoftLavender,
                    contentColor = DeepNight,
                ),
            ) {
                Text(text = "Finish session", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onCancel,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.08f),
                    contentColor = Color.White,
                ),
            ) {
                Text(text = "Cancel")
            }
        }
    }
}

@HiltViewModel
class PlaceholderGameViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val applyMiniGameOutcome: ApplyMiniGameOutcomeUseCase,
) : ViewModel() {

    private val monsterId: String = checkNotNull(savedStateHandle["monsterId"])

    fun completePlaceholderSession(gameId: MiniGameId, onComplete: (MiniGameOutcome) -> Unit) {
        val outcome = MiniGameOutcome(
            gameId = gameId,
            scoreFraction = 0.6f,
            rawScore = 60,
        )
        viewModelScope.launch {
            runCatching { applyMiniGameOutcome(monsterId, outcome) }
            onComplete(outcome)
        }
    }
}

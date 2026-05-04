package com.nightcatchers.feature.parental

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.nightcatchers.core.ui.theme.DeepNight
import com.nightcatchers.core.ui.theme.PetRoomBgTop
import com.nightcatchers.core.ui.theme.SoftLavender

@Composable
fun PinChangeScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PinChangeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                PinChangeEvent.PinChanged -> onDone()
            }
        }
    }

    val (emoji, title, subtitle) = when (state.step) {
        PinChangeStep.VerifyOld -> Triple(
            "🔒",
            "Current PIN",
            "Enter your existing 4-digit PIN",
        )
        PinChangeStep.EnterNew -> Triple(
            "🔑",
            "New PIN",
            "Choose a new 4-digit PIN",
        )
        PinChangeStep.ConfirmNew -> Triple(
            "✅",
            "Confirm New PIN",
            "Enter your new PIN again to confirm",
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PetRoomBgTop, DeepNight)))
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(80.dp))

        Text(text = emoji, fontSize = 48.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(32.dp))
        PinDots(filled = state.digits.length)

        state.errorMessage?.let { error ->
            Spacer(Modifier.height(8.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(32.dp))

        if (state.isLoading) {
            CircularProgressIndicator(color = SoftLavender)
        } else {
            PinPad(
                onDigit = viewModel::onDigit,
                onDelete = viewModel::onDelete,
            )
        }
    }
}

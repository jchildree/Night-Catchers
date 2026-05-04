package com.nightcatchers.feature.parental

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcatchers.core.domain.model.PendingShare
import com.nightcatchers.core.domain.model.ShareType
import com.nightcatchers.core.ui.theme.DeepNight
import com.nightcatchers.core.ui.theme.PetRoomBgTop
import com.nightcatchers.core.ui.theme.SoftLavender
import com.nightcatchers.core.ui.theme.SurfaceDark
import kotlin.math.roundToInt

@Composable
fun ParentalDashboardScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPinChange: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ParentalDashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PetRoomBgTop, DeepNight))),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
            DashboardHeader(onNavigateBack = onNavigateBack)
            Spacer(Modifier.height(20.dp))
            ScreenTimeLimitCard()
            Spacer(Modifier.height(16.dp))
            PendingSharesCard(
                shares = uiState.pendingShares,
                onApprove = viewModel::approveShare,
                onDecline = viewModel::declineShare,
            )
            Spacer(Modifier.height(16.dp))
            PinManagementCard(onNavigateToPinChange = onNavigateToPinChange)
        }
    }
}

@Composable
private fun DashboardHeader(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = "Parent Dashboard",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
            )
            Text(
                text = "Manage your child's experience",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.5f),
            )
        }
        TextButton(onClick = onNavigateBack) {
            Text(text = "Done", color = SoftLavender)
        }
    }
}

@Composable
private fun ScreenTimeLimitCard() {
    var sliderValue by remember { mutableFloatStateOf(60f) }
    val minutes = sliderValue.roundToInt()

    DashboardCard(title = "Daily Screen Time Limit") {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = formatMinutes(minutes),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SoftLavender,
                )
                Text(
                    text = "per day",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f),
                )
            }
            Spacer(Modifier.height(8.dp))
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                valueRange = 15f..180f,
                steps = 10,
                colors = SliderDefaults.colors(
                    thumbColor = SoftLavender,
                    activeTrackColor = SoftLavender,
                    inactiveTrackColor = SoftLavender.copy(alpha = 0.2f),
                ),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = "15 min", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
                Text(text = "3 hours", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
            }
        }
    }
}

@Composable
private fun PendingSharesCard(
    shares: List<PendingShare>,
    onApprove: (String) -> Unit,
    onDecline: (String) -> Unit,
) {
    DashboardCard(title = "Pending Share Approvals") {
        if (shares.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No pending shares",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.4f),
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                shares.forEach { share ->
                    ShareItem(
                        share = share,
                        onApprove = { onApprove(share.id) },
                        onDecline = { onDecline(share.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ShareItem(
    share: PendingShare,
    onApprove: () -> Unit,
    onDecline: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.06f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = share.monsterName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                )
                Text(
                    text = share.type.label(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f),
                )
            }
            Row {
                TextButton(onClick = onDecline) {
                    Text(text = "Decline", color = Color(0xFFFF6F61))
                }
                TextButton(onClick = onApprove) {
                    Text(text = "Approve", color = SoftLavender)
                }
            }
        }
    }
}

@Composable
private fun PinManagementCard(onNavigateToPinChange: () -> Unit) {
    DashboardCard(title = "Security") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Change PIN",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                )
                Text(
                    text = "Update your 4-digit parent PIN",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f),
                )
            }
            TextButton(onClick = onNavigateToPinChange) {
                Text(text = "Change", color = SoftLavender)
            }
        }
    }
}

@Composable
private fun DashboardCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceDark,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.6f),
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

private fun formatMinutes(minutes: Int): String = when {
    minutes < 60 -> "$minutes min"
    minutes % 60 == 0 -> "${minutes / 60}h"
    else -> "${minutes / 60}h ${minutes % 60}min"
}

private fun ShareType.label(): String = when (this) {
    ShareType.CAPTURE_CLIP -> "Capture Clip"
    ShareType.CAPTURE_CARD -> "Capture Card"
    ShareType.EVOLUTION_CARD -> "Evolution Card"
    ShareType.FRIENDSHIP_CARD -> "Friendship Card"
}

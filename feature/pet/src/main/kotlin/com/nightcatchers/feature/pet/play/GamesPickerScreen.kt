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
import com.nightcatchers.core.ui.theme.DeepNight
import com.nightcatchers.core.ui.theme.PetRoomBgTop
import com.nightcatchers.core.ui.theme.RarityGold
import com.nightcatchers.core.ui.theme.SoftLavender

@Composable
fun GamesPickerScreen(
    onPickMonster: (monsterId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GamesPickerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PetRoomBgTop, DeepNight))),
    ) {
        when (val s = state) {
            GamesPickerUiState.Loading -> CircularProgressIndicator(
                color = SoftLavender,
                modifier = Modifier.align(Alignment.Center),
            )
            is GamesPickerUiState.Ready -> Content(state = s, onPickMonster = onPickMonster)
        }
    }
}

@Composable
private fun Content(
    state: GamesPickerUiState.Ready,
    onPickMonster: (String) -> Unit,
) {
    if (state.entries.isEmpty()) {
        EmptyState()
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Header(featuredGameLabel = state.featuredGameLabel) }
        items(state.entries, key = { it.monsterId }) { entry ->
            MonsterRow(entry = entry, onClick = { onPickMonster(entry.monsterId) })
        }
    }
}

@Composable
private fun Header(featuredGameLabel: String) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = "🎮  Games",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Pick a friend to play with",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.6f),
        )
        Spacer(Modifier.height(10.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = RarityGold.copy(alpha = 0.15f),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "★", color = RarityGold, fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Today's featured: $featuredGameLabel",
                    style = MaterialTheme.typography.labelMedium,
                    color = RarityGold,
                )
            }
        }
    }
}

@Composable
private fun MonsterRow(entry: GamesPickerEntry, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Black.copy(alpha = 0.35f),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SoftLavender.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SoftLavender.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = entry.emoji, fontSize = 30.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = entry.displayName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
                Text(
                    text = entry.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Tap to play →",
                    style = MaterialTheme.typography.labelSmall,
                    color = SoftLavender,
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "🎮", fontSize = 64.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                text = "No friends to play with yet",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Capture a monster from the Scan tab, then come back to play together.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

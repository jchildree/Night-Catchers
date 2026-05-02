package com.nightcatchers.feature.vault

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcatchers.core.domain.model.Mood
import com.nightcatchers.core.domain.model.Rarity
import com.nightcatchers.core.ui.component.MonsterAvatar
import com.nightcatchers.core.ui.theme.DeepNight
import com.nightcatchers.core.ui.theme.RarityCommon
import com.nightcatchers.core.ui.theme.RarityLegendary
import com.nightcatchers.core.ui.theme.RarityRare
import com.nightcatchers.core.ui.theme.RarityUncommon
import com.nightcatchers.core.ui.theme.SoftLavender
import com.nightcatchers.core.ui.theme.SurfaceDark

@Composable
fun VaultScreen(
    onNavigateToPet: (monsterId: String) -> Unit,
    onNavigateToDetail: (monsterId: String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: VaultViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0D0D1A), DeepNight))),
    ) {
        when (val s = state) {
            is VaultUiState.Loading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = SoftLavender,
            )
            is VaultUiState.Error -> Text(
                text = s.message,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
            )
            is VaultUiState.Ready -> {
                VaultContent(
                    state = s,
                    onNavigateToDetail = onNavigateToDetail,
                    onRequestRelease = viewModel::requestRelease,
                )

                if (s.pendingReleaseId != null) {
                    val entry = s.entries.find { it.monster.id == s.pendingReleaseId }
                    if (entry != null) {
                        ReleaseConfirmDialog(
                            entry = entry,
                            onConfirm = viewModel::confirmRelease,
                            onDismiss = viewModel::cancelRelease,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VaultContent(
    state: VaultUiState.Ready,
    onNavigateToDetail: (String) -> Unit,
    onRequestRelease: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        VaultHeader(count = state.entries.size)

        if (state.isEmpty) {
            EmptyVaultMessage(modifier = Modifier.weight(1f))
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f),
            ) {
                items(
                    items = state.entries,
                    key = { it.monster.id },
                ) { entry ->
                    MonsterCard(
                        entry = entry,
                        onClick = { onNavigateToDetail(entry.monster.id) },
                        onLongClick = { onRequestRelease(entry.monster.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun VaultHeader(count: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        Text(
            text = "The Vault",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
        )
        Text(
            text = "$count monster${if (count == 1) "" else "s"} captured",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.5f),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MonsterCard(
    entry: VaultEntry,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rarityColor = entry.rarity.color()
    Surface(
        modifier = modifier
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.5.dp,
                color = rarityColor.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp),
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceDark,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            RarityBadge(rarity = entry.rarity, color = rarityColor)
            MonsterAvatar(emoji = entry.emoji, size = 64.dp)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = entry.displayName,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                MoodIndicator(mood = entry.mood)
            }
        }
    }
}

@Composable
private fun RarityBadge(rarity: Rarity, color: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.2f),
    ) {
        Text(
            text = rarity.name.lowercase().replaceFirstChar { it.uppercase() },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun MoodIndicator(mood: Mood) {
    Text(
        text = "${mood.emoji()} ${mood.name.lowercase().replaceFirstChar { it.uppercase() }}",
        style = MaterialTheme.typography.bodySmall,
        color = Color.White.copy(alpha = 0.6f),
        fontSize = 11.sp,
    )
}

@Composable
private fun EmptyVaultMessage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "👻", fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "No monsters yet!",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Head to the AR scanner to catch your first monster.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp),
        )
    }
}

@Composable
private fun ReleaseConfirmDialog(
    entry: VaultEntry,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Release ${entry.displayName}?") },
        text = {
            Text(
                "Are you sure you want to release ${entry.displayName} back into the wild? " +
                    "This cannot be undone.",
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4D4D)),
            ) {
                Text("Release", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Keep") }
        },
    )
}

private fun Rarity.color(): Color = when (this) {
    Rarity.COMMON    -> RarityCommon
    Rarity.UNCOMMON  -> RarityUncommon
    Rarity.RARE      -> RarityRare
    Rarity.LEGENDARY -> RarityLegendary
}

private fun Mood.emoji(): String = when (this) {
    Mood.CONTENT  -> "😊"
    Mood.EXCITED  -> "🤩"
    Mood.LONELY   -> "😢"
    Mood.GRUMPY   -> "😤"
    Mood.SLEEPY   -> "😴"
    Mood.PLAYFUL  -> "😜"
    Mood.SPOOKED  -> "😱"
}

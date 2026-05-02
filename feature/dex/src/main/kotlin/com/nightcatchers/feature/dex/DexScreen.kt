package com.nightcatchers.feature.dex

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcatchers.core.domain.model.MonsterArchetypeCatalog
import com.nightcatchers.core.domain.model.Rarity
import com.nightcatchers.core.ui.component.MonsterAvatar
import com.nightcatchers.core.ui.theme.DeepNight
import com.nightcatchers.core.ui.theme.PetRoomBgTop
import com.nightcatchers.core.ui.theme.RarityCommon
import com.nightcatchers.core.ui.theme.RarityGold
import com.nightcatchers.core.ui.theme.RarityLegendary
import com.nightcatchers.core.ui.theme.RarityRare
import com.nightcatchers.core.ui.theme.RarityUncommon
import com.nightcatchers.core.ui.theme.SoftLavender

@Composable
fun DexScreen(
    modifier: Modifier = Modifier,
    viewModel: DexViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PetRoomBgTop, DeepNight)))
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        DexHeader(
            discoveredCount = state.entries.count { it.isDiscovered },
            totalCount = MonsterArchetypeCatalog.all.size,
        )

        Spacer(Modifier.height(16.dp))

        DexFilters(
            selectedRarity = state.selectedRarity,
            sortBy = state.sortBy,
            showOnlyDiscovered = state.showOnlyDiscovered,
            onRaritySelected = viewModel::setRarityFilter,
            onSortChanged = viewModel::setSortBy,
            onToggleDiscovered = viewModel::toggleShowOnlyDiscovered,
        )

        Spacer(Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(state.entries) { entry ->
                DexEntryCard(entry = entry)
            }
        }
    }
}

@Composable
private fun DexHeader(discoveredCount: Int, totalCount: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "📖 Monster Dex",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "$discoveredCount / $totalCount Discovered",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DexFilters(
    selectedRarity: Rarity?,
    sortBy: DexSort,
    showOnlyDiscovered: Boolean,
    onRaritySelected: (Rarity?) -> Unit,
    onSortChanged: (DexSort) -> Unit,
    onToggleDiscovered: () -> Unit,
) {
    Column {
        Text(
            text = "Filters",
            style = MaterialTheme.typography.titleSmall,
            color = SoftLavender,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Rarity filters
            Rarity.values().forEach { rarity ->
                FilterChip(
                    selected = selectedRarity == rarity,
                    onClick = { onRaritySelected(if (selectedRarity == rarity) null else rarity) },
                    label = {
                        Text(
                            text = rarity.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(rarity.color, CircleShape),
                        )
                    },
                )
            }

            // Sort dropdown
            var sortMenuExpanded by remember { mutableStateOf(false) }
            Box {
                TextButton(onClick = { sortMenuExpanded = true }) {
                    Text("Sort: ${sortBy.name.lowercase().replaceFirstChar { it.uppercase() }}")
                }
                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false },
                ) {
                    DexSort.values().forEach { sort ->
                        DropdownMenuItem(
                            text = { Text(sort.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                onSortChanged(sort)
                                sortMenuExpanded = false
                            },
                        )
                    }
                }
            }

            // Show only discovered toggle
            FilterChip(
                selected = showOnlyDiscovered,
                onClick = onToggleDiscovered,
                label = { Text("Discovered Only") },
            )
        }
    }
}

@Composable
private fun DexEntryCard(entry: DexEntry) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (entry.isDiscovered) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.3f),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(12.dp),
        ) {
            // Monster avatar
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.2f))
                    .then(
                        if (!entry.isDiscovered) Modifier.alpha(0.3f) else Modifier
                    ),
            ) {
                if (entry.isDiscovered) {
                    MonsterAvatar(
                        emoji = entry.archetype.emoji,
                        size = 40.dp,
                    )
                } else {
                    Text(
                        text = "❓",
                        fontSize = 24.sp,
                        color = Color.White.copy(alpha = 0.5f),
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Monster number
            Text(
                text = "#${entry.archetype.number.toString().padStart(3, '0')}",
                style = MaterialTheme.typography.labelSmall,
                color = entry.archetype.rarity.color,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(4.dp))

            // Monster name
            Text(
                text = if (entry.isDiscovered) entry.archetype.name else "???",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
            )

            if (entry.isDiscovered && entry.captureCount > 1) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "×${entry.captureCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = SoftLavender,
                )
            }
        }
    }
}

private val Rarity.color: Color
    get() = when (this) {
        Rarity.COMMON -> RarityCommon
        Rarity.UNCOMMON -> RarityUncommon
        Rarity.RARE -> RarityRare
        Rarity.LEGENDARY -> RarityGold
    }
package com.nightcatchers.feature.dex

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.nightcatchers.core.ui.theme.StatEnergy
import com.nightcatchers.core.ui.theme.StatHappiness
import com.nightcatchers.core.ui.theme.StatHunger
import com.nightcatchers.core.ui.theme.StatSpookiness
import com.nightcatchers.core.ui.theme.StatTrust

private val Rarity.color: Color
    get() = when (this) {
        Rarity.COMMON -> RarityCommon
        Rarity.UNCOMMON -> RarityUncommon
        Rarity.RARE -> RarityRare
        Rarity.LEGENDARY -> RarityGold
    }

@Composable
fun DexDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: DexDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PetRoomBgTop, DeepNight)))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when {
            state.isLoading -> {
                Text(
                    text = "Loading...",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            state.archetype == null -> {
                Text(
                    text = "Monster not found",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            else -> {
                val archetype = state.archetype!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Header with avatar and basic info
                    MonsterHeader(
                        archetype = archetype,
                        isDiscovered = state.isDiscovered,
                        captureCount = state.captureCount,
                    )

                    Spacer(Modifier.height(24.dp))

                    // Stats section
                    if (state.isDiscovered) {
                        StatsSection(archetype = archetype)
                        Spacer(Modifier.height(24.dp))
                    }

                    // Personality section
                    PersonalitySection(archetype = archetype, isDiscovered = state.isDiscovered)
                    Spacer(Modifier.height(24.dp))

                    // Habitat section
                    HabitatSection(archetype = archetype, isDiscovered = state.isDiscovered)
                    Spacer(Modifier.height(24.dp))

                    // Preferences section
                    if (state.isDiscovered) {
                        PreferencesSection(archetype = archetype)
                        Spacer(Modifier.height(24.dp))
                    }

                    // Capture info section
                    CaptureInfoSection(archetype = archetype, isDiscovered = state.isDiscovered)
                }
            }
        }
    }
}

@Composable
private fun MonsterHeader(
    archetype: com.nightcatchers.core.domain.model.MonsterArchetype,
    isDiscovered: Boolean,
    captureCount: Int,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 6.dp,
        color = Color.White.copy(alpha = 0.05f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(archetype.rarity.color.copy(alpha = 0.35f), Color.Black.copy(alpha = 0.15f)),
                    ),
                )
                .padding(20.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(112.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.22f))
                        .border(3.dp, archetype.rarity.color, CircleShape),
                ) {
                    if (isDiscovered) {
                        MonsterAvatar(
                            emoji = archetype.emoji,
                            size = 72.dp,
                        )
                    } else {
                        Text(
                            text = "❓",
                            fontSize = 40.sp,
                            color = Color.White.copy(alpha = 0.55f),
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "#${archetype.number.toString().padStart(3, '0')}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.75f),
                        fontWeight = FontWeight.SemiBold,
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = if (isDiscovered) archetype.name else "Unknown Creature",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )

                    if (isDiscovered) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = archetype.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.75f),
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.1f),
                    tonalElevation = 1.dp,
                ) {
                    Text(
                        text = if (isDiscovered) "Discovered" else "Hidden",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                InfoChip(
                    label = "Rarity",
                    value = archetype.rarity.name.lowercase().replaceFirstChar { it.uppercase() },
                    color = archetype.rarity.color,
                )
                InfoChip(
                    label = "Captured",
                    value = if (captureCount > 0) "$captureCount" else "—",
                    color = if (captureCount > 0) SoftLavender else Color.White.copy(alpha = 0.35f),
                )
            }
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.08f),
        tonalElevation = 1.dp,
        modifier = Modifier
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.75f),
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = color,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun StatsSection(archetype: com.nightcatchers.core.domain.model.MonsterArchetype) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Base Stats",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            StatRow("Hunger", archetype.defaultStats.hunger, StatHunger)
            StatRow("Happiness", archetype.defaultStats.happiness, StatHappiness)
            StatRow("Energy", archetype.defaultStats.energy, StatEnergy)
            StatRow("Spookiness", archetype.defaultStats.spookiness, StatSpookiness)
            StatRow("Trust", archetype.defaultStats.trust, StatTrust)
        }
    }
}

@Composable
private fun StatRow(label: String, value: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = color,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .width(60.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(value / 100f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(color),
                )
            }
        }
    }
}

@Composable
private fun PersonalitySection(
    archetype: com.nightcatchers.core.domain.model.MonsterArchetype,
    isDiscovered: Boolean,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Personality",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            if (isDiscovered) {
                Text(
                    text = archetype.personality.core,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                Text(
                    text = "\"${archetype.personality.catchphrase}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoftLavender,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    modifier = Modifier.padding(bottom = 12.dp),
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Loves",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = archetype.personality.loves,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hates",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = archetype.personality.hates,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                        )
                    }
                }
            } else {
                Text(
                    text = "Discover this monster to learn about its personality!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f),
                )
            }
        }
    }
}

@Composable
private fun HabitatSection(
    archetype: com.nightcatchers.core.domain.model.MonsterArchetype,
    isDiscovered: Boolean,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Habitat",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            if (isDiscovered) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Location",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = archetype.spawnBias.location.replace("_", " ").replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Time of Day",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = archetype.spawnBias.timeOfDay.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                        )
                    }
                }
            } else {
                Text(
                    text = "Discover this monster to learn where it lives!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f),
                )
            }
        }
    }
}

@Composable
private fun PreferencesSection(archetype: com.nightcatchers.core.domain.model.MonsterArchetype) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Favorite Food",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = archetype.favFood,
                        style = MaterialTheme.typography.bodySmall,
                        color = StatHunger,
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Favorite Game",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = archetype.favGame,
                        style = MaterialTheme.typography.bodySmall,
                        color = StatHappiness,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Soothing Action",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = archetype.soothingAction,
                style = MaterialTheme.typography.bodySmall,
                color = StatTrust,
            )
        }
    }
}

@Composable
private fun CaptureInfoSection(
    archetype: com.nightcatchers.core.domain.model.MonsterArchetype,
    isDiscovered: Boolean,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Capture Info",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            if (isDiscovered) {
                Text(
                    text = "Hold Time: ${archetype.captureHoldSeconds}s",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Decay Rates (per hour)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hunger: -${archetype.decayRates.hungerPerHour}",
                            style = MaterialTheme.typography.bodySmall,
                            color = StatHunger,
                        )
                        Text(
                            text = "Happiness: -${archetype.decayRates.happinessPerHour}",
                            style = MaterialTheme.typography.bodySmall,
                            color = StatHappiness,
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Energy: -${archetype.decayRates.energyPerHour}",
                            style = MaterialTheme.typography.bodySmall,
                            color = StatEnergy,
                        )
                        Text(
                            text = "Spookiness: +${archetype.decayRates.spookinessPerHour}",
                            style = MaterialTheme.typography.bodySmall,
                            color = StatSpookiness,
                        )
                    }
                }
            } else {
                Text(
                    text = "Discover this monster to learn how to capture it!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f),
                )
            }
        }
    }
}
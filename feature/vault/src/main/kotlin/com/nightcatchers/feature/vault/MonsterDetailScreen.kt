package com.nightcatchers.feature.vault

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcatchers.core.domain.model.Mood
import com.nightcatchers.core.domain.model.Rarity
import com.nightcatchers.core.ui.component.MonsterAvatar
import com.nightcatchers.core.ui.component.StatBar
import com.nightcatchers.core.ui.theme.DeepNight
import com.nightcatchers.core.ui.theme.PetRoomBgTop
import com.nightcatchers.core.ui.theme.RarityCommon
import com.nightcatchers.core.ui.theme.RarityLegendary
import com.nightcatchers.core.ui.theme.RarityRare
import com.nightcatchers.core.ui.theme.RarityUncommon
import com.nightcatchers.core.ui.theme.SoftLavender
import com.nightcatchers.core.ui.theme.StatEnergy
import com.nightcatchers.core.ui.theme.StatHappiness
import com.nightcatchers.core.ui.theme.StatHunger
import com.nightcatchers.core.ui.theme.StatSpookiness
import com.nightcatchers.core.ui.theme.StatTrust
import com.nightcatchers.core.ui.theme.SurfaceDark

@Composable
fun MonsterDetailScreen(
    monsterId: String,
    onNavigateToPet: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VaultViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val entry = (state as? VaultUiState.Ready)?.entries?.find { it.monster.id == monsterId }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PetRoomBgTop, DeepNight))),
    ) {
        when {
            state is VaultUiState.Loading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = SoftLavender,
            )
            entry == null -> Column(
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Monster not found", color = Color.White)
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onNavigateBack) { Text("Back", color = SoftLavender) }
            }
            else -> MonsterDetailContent(
                entry = entry,
                onNavigateToPet = onNavigateToPet,
                onNavigateBack = onNavigateBack,
            )
        }
    }
}

@Composable
private fun MonsterDetailContent(
    entry: VaultEntry,
    onNavigateToPet: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Back button row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            TextButton(onClick = onNavigateBack) {
                Text("← Back", color = SoftLavender)
            }
        }

        Spacer(Modifier.height(8.dp))

        // Avatar + name
        MonsterAvatar(
            emoji = entry.emoji,
            size = 110.dp,
            mood = entry.mood,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = entry.displayName,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
        )
        Text(
            text = entry.archetype.subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.5f),
        )
        Spacer(Modifier.height(4.dp))
        RarityChip(rarity = entry.rarity)
        Spacer(Modifier.height(4.dp))
        MoodLabel(mood = entry.mood)

        Spacer(Modifier.height(20.dp))

        // Personality card
        DetailCard(title = "Personality") {
            PersonalityRow(label = "Core", value = entry.archetype.personality.core)
            PersonalityRow(label = "Loves", value = entry.archetype.personality.loves)
            PersonalityRow(label = "Hates", value = entry.archetype.personality.hates)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "\"${entry.archetype.personality.catchphrase}\"",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = SoftLavender,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(16.dp))

        // Stats card
        DetailCard(title = "Current Stats") {
            StatBar(label = "Hunger",     value = entry.stats.hunger,     color = StatHunger)
            StatBar(label = "Happiness",  value = entry.stats.happiness,  color = StatHappiness)
            StatBar(label = "Energy",     value = entry.stats.energy,     color = StatEnergy)
            StatBar(label = "Spookiness", value = entry.stats.spookiness, color = StatSpookiness)
            StatBar(label = "Trust",      value = entry.stats.trust,      color = StatTrust)
        }

        Spacer(Modifier.height(16.dp))

        // Favourites card
        DetailCard(title = "Favourites") {
            PersonalityRow(label = "Favourite food", value = entry.archetype.favFood)
            PersonalityRow(label = "Favourite game", value = entry.archetype.favGame)
            PersonalityRow(label = "Soothing action", value = entry.archetype.soothingAction)
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onNavigateToPet,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SoftLavender),
        ) {
            Text(
                text = "Visit Pet Room",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}

@Composable
private fun RarityChip(rarity: Rarity) {
    val color = when (rarity) {
        Rarity.COMMON    -> RarityCommon
        Rarity.UNCOMMON  -> RarityUncommon
        Rarity.RARE      -> RarityRare
        Rarity.LEGENDARY -> RarityLegendary
    }
    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.2f)) {
        Text(
            text = rarity.name.lowercase().replaceFirstChar { it.uppercase() },
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = color,
        )
    }
}

@Composable
private fun MoodLabel(mood: Mood) {
    val emoji = when (mood) {
        Mood.CONTENT -> "😊"; Mood.EXCITED -> "🤩"; Mood.LONELY -> "😢"
        Mood.GRUMPY  -> "😤"; Mood.SLEEPY -> "😴"; Mood.PLAYFUL -> "😜"
        Mood.SPOOKED -> "😱"
    }
    Text(
        text = "$emoji ${mood.name.lowercase().replaceFirstChar { it.uppercase() }}",
        style = MaterialTheme.typography.bodySmall,
        color = Color.White.copy(alpha = 0.6f),
        fontSize = 13.sp,
    )
}

@Composable
private fun DetailCard(title: String, content: @Composable () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = SurfaceDark) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.6f),
            )
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun PersonalityRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f))
        Text(text = value, style = MaterialTheme.typography.bodySmall, color = Color.White, modifier = Modifier.padding(start = 12.dp))
    }
}

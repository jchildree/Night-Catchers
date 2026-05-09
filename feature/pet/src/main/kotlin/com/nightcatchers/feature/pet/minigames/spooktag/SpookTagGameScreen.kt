package com.nightcatchers.feature.pet.minigames.spooktag

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nightcatchers.core.ui.theme.DeepNight
import com.nightcatchers.core.ui.theme.MonsterPurple
import com.nightcatchers.core.ui.theme.PetRoomBgBottom

@Composable
fun SpookTagGameScreen(
    monsterId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DeepNight, PetRoomBgBottom))),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp),
        ) {
            Text(text = "🚪", fontSize = 56.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Spook Tag",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Tap the door before it opens!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onNavigateBack,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MonsterPurple),
            ) {
                Text(text = "Finish Game", color = Color.White)
            }
        }
    }
}

package com.nightcatchers.feature.capture

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcatchers.core.ui.component.MonsterAvatar
import com.nightcatchers.core.ui.theme.SlimeGreen

@Composable
fun CaptureScreen(
    onCaptureSuccess: (monsterId: String) -> Unit,
    viewModel: CaptureViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Respond to success state
    if (state is CaptureState.Success) {
        onCaptureSuccess((state as CaptureState.Success).monsterId)
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview is rendered behind this via AndroidView in the AR feature.
        // This composable handles the Compose-layer overlay.

        when (val s = state) {
            is CaptureState.Scanning -> {
                ScanningHud(modifier = Modifier.align(Alignment.BottomCenter))
            }
            is CaptureState.MonsterSpawned -> {
                MonsterAnchorOverlay(
                    archetype = s.archetype,
                    anchorX = s.anchorX,
                    anchorY = s.anchorY,
                    onHoldStart = { viewModel.onBeamHoldStart(s.archetype, s.anchorX, s.anchorY) },
                    onHoldRelease = { viewModel.onBeamHoldRelease() },
                )
            }
            is CaptureState.Capturing -> {
                MonsterAnchorOverlay(
                    archetype = s.archetype,
                    anchorX = s.anchorX,
                    anchorY = s.anchorY,
                    holdProgress = s.holdProgress,
                    onHoldStart = {},
                    onHoldRelease = { viewModel.onBeamHoldRelease() },
                )
                ProtonBeamOverlay(beamX = s.anchorX, beamY = s.anchorY, progress = s.holdProgress)
            }
            else -> Unit
        }
    }
}

@Composable
private fun ScanningHud(modifier: Modifier = Modifier) {
    Text(
        text = "Scanning for monsters…",
        style = MaterialTheme.typography.labelLarge,
        color = SlimeGreen,
        modifier = modifier.padding(bottom = 48.dp),
    )
}

@Composable
private fun MonsterAnchorOverlay(
    archetype: com.nightcatchers.core.domain.model.MonsterArchetype,
    anchorX: Float,
    anchorY: Float,
    holdProgress: Float = 0f,
    onHoldStart: () -> Unit,
    onHoldRelease: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onHoldStart()
                        tryAwaitRelease()
                        onHoldRelease()
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        MonsterAvatar(
            emoji = archetype.emoji,
            isAnimated = holdProgress == 0f,
        )
    }
}

@Composable
private fun ProtonBeamOverlay(beamX: Float, beamY: Float, progress: Float) {
    val alpha by animateFloatAsState(
        targetValue = 0.6f + progress * 0.4f,
        animationSpec = tween(100),
        label = "beam_alpha",
    )
    // Beam rendered by FilterLayerManager GL pass; this layer adds Compose particle count indicator.
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.labelLarge,
            color = SlimeGreen.copy(alpha = alpha),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp),
        )
    }
}

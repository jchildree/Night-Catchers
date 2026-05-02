package com.nightcatchers.feature.ar

import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcatchers.core.common.DeviceTier
import com.nightcatchers.core.ui.component.MonsterAvatar
import com.nightcatchers.core.ui.theme.EctoplasmCyan
import com.nightcatchers.core.ui.theme.SlimeGreen
import java.util.concurrent.Executors

@Composable
fun ArScanScreen(
    onNavigateToCapture: (archetypeId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val cameraManager = remember { CameraManager() }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val analyzer = remember {
        MlKitObjectAnalyzer { objectCount ->
            viewModel.onFrameAvailable(System.currentTimeMillis(), objectCount > 0)
        }
    }

    // Check device tier — Tier C has no AR
    if (viewModel.deviceTier == DeviceTier.C) {
        TierCFallback()
        return
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraManager.release()
            analysisExecutor.shutdown()
        }
    }

    // Navigate to capture when a monster spawns — pass archetype ID so CaptureViewModel
    // can seed its initial state without relying on SharedFlow replay.
    LaunchedEffect(uiState) {
        val s = uiState
        if (s is ArUiState.MonsterSpawned) {
            onNavigateToCapture(s.archetype.id)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Camera preview — start/stop tied to compose lifecycle via DisposableEffect inside factory
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }.also { pv ->
                    cameraManager.startCamera(
                        context = ctx,
                        lifecycleOwner = lifecycleOwner,
                        previewView = pv,
                        analyzer = analyzer,
                        analysisExecutor = analysisExecutor,
                    )
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        // Compose overlay layer
        when (val s = uiState) {
            is ArUiState.Scanning -> ScanningHud(modifier = Modifier.align(Alignment.BottomCenter))
            is ArUiState.MonsterSpawned -> MonsterDetectedOverlay(
                archetype = s.archetype,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

@Composable
private fun ScanningHud(modifier: Modifier = Modifier) {
    val pulse = rememberInfiniteTransition(label = "scan_pulse")
    val alpha by pulse.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scan_alpha",
    )

    Column(
        modifier = modifier.padding(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        RadarRing()
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Scanning for monsters…",
            style = MaterialTheme.typography.labelLarge,
            color = SlimeGreen.copy(alpha = alpha),
        )
    }
}

@Composable
private fun RadarRing() {
    val pulse = rememberInfiniteTransition(label = "radar")
    val scale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "radar_scale",
    )
    val alpha by pulse.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "radar_alpha",
    )
    Box(
        modifier = Modifier
            .scale(scale)
            .border(2.dp, EctoplasmCyan.copy(alpha = alpha), RoundedCornerShape(percent = 50))
            .padding(28.dp),
    )
}

@Composable
private fun MonsterDetectedOverlay(
    archetype: com.nightcatchers.core.domain.model.MonsterArchetype,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = true,
        enter = scaleIn() + fadeIn(),
        modifier = modifier,
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.Black.copy(alpha = 0.75f),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                MonsterAvatar(emoji = archetype.emoji, size = 80.dp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = archetype.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
                Text(
                    text = archetype.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = EctoplasmCyan,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "👻 Monster detected!",
                    style = MaterialTheme.typography.labelLarge,
                    color = SlimeGreen,
                    fontSize = 18.sp,
                )
                Text(
                    text = "Preparing capture ritual…",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                )
            }
        }
    }
}

@Composable
private fun TierCFallback() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "👾", fontSize = 64.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "AR requires a newer device",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "OpenGL ES 3.0 and ARCore are needed\nto hunt monsters in your room.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.5f),
            )
        }
    }
}

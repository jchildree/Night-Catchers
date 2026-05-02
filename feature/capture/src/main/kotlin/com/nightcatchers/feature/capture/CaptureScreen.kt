package com.nightcatchers.feature.capture

import android.graphics.PixelFormat
import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcatchers.core.common.DeviceTier
import com.nightcatchers.core.ui.component.MonsterAvatar
import com.nightcatchers.core.ui.theme.SlimeGreen
import com.nightcatchers.feature.ar.CameraManager

@Composable
fun CaptureScreen(
    onCaptureSuccess: (monsterId: String) -> Unit,
    viewModel: CaptureViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraManager = remember { CameraManager() }
    val renderer = remember { CaptureGlRenderer(viewModel.filterLayerManager) }

    // Sync beam position from Capturing state to the GL renderer (cross-thread via @Volatile).
    LaunchedEffect(state) {
        val s = state
        if (s is CaptureState.Capturing) {
            renderer.beamX = s.anchorX
            renderer.beamY = s.anchorY
        }
    }

    DisposableEffect(Unit) {
        onDispose { cameraManager.release() }
    }

    if (state is CaptureState.Success) {
        onCaptureSuccess((state as CaptureState.Success).monsterId)
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Layer 1: Camera preview ──────────────────────────────────────────
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }.also { pv ->
                    cameraManager.startPreviewOnly(
                        context = ctx,
                        lifecycleOwner = lifecycleOwner,
                        previewView = pv,
                    )
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        // ── Layer 2: GL shader overlay (Tier A / B only) ─────────────────────
        if (viewModel.deviceTier != DeviceTier.C) {
            AndroidView(
                factory = { ctx ->
                    android.opengl.GLSurfaceView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                        setEGLContextClientVersion(3)
                        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
                        holder.setFormat(PixelFormat.RGBA_8888)
                        setZOrderOnTop(true)
                        setRenderer(renderer)
                        renderMode = android.opengl.GLSurfaceView.RENDERMODE_CONTINUOUSLY
                    }
                },
                onRelease = { it.onPause() },
                modifier = Modifier.fillMaxSize(),
            )
        }

        // ── Layer 3: Compose capture overlay ────────────────────────────────
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
                ProtonBeamProgress(progress = s.holdProgress)
            }
            else -> Unit
        }
    }
}

@Composable
private fun ScanningHud(modifier: Modifier = Modifier) {
    Text(
        text = "Point at a monster…",
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
private fun ProtonBeamProgress(progress: Float) {
    val alpha by animateFloatAsState(
        targetValue = 0.6f + progress * 0.4f,
        animationSpec = tween(100),
        label = "beam_alpha",
    )
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

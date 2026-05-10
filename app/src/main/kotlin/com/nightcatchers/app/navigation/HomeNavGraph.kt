package com.nightcatchers.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.nightcatchers.core.ui.theme.DeepNight
import com.nightcatchers.core.ui.theme.PetRoomBgTop
import com.nightcatchers.feature.ar.ArScanScreen
import com.nightcatchers.feature.capture.CaptureScreen
import com.nightcatchers.feature.parental.ParentalDashboardScreen
import com.nightcatchers.feature.parental.PinChangeScreen
import com.nightcatchers.feature.parental.PinGateScreen
import com.nightcatchers.core.domain.model.MiniGameId
import com.nightcatchers.feature.pet.PetEvolveScreen
import com.nightcatchers.feature.pet.PetRoomScreen
import com.nightcatchers.feature.pet.play.CuddleStormScreen
import com.nightcatchers.feature.pet.play.MiniGamePlaceholderScreen
import com.nightcatchers.feature.pet.play.MiniGameResultScreen
import com.nightcatchers.feature.pet.play.PlayMenuScreen
import com.nightcatchers.feature.vault.MonsterDetailScreen
import com.nightcatchers.feature.vault.VaultScreen
import com.nightcatchers.feature.dex.DexDetailScreen
import com.nightcatchers.feature.dex.DexScreen

/**
 * Inner NavHost for the 5-tab home area.
 * Lives inside HomeScreen's Scaffold; has its own back-stack scoped to the tabs.
 */
@Composable
fun HomeNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Dest.Vault,
        modifier = modifier,
    ) {

        // ── Vault tab ──────────────────────────────────────────────────────
        navigation<Dest.Vault>(startDestination = Dest.Vault) {
            composable<Dest.Vault> {
                VaultScreen(
                    onNavigateToPet = { monsterId -> navController.navigateToPet(monsterId) },
                    onNavigateToDetail = { monsterId ->
                        navController.navigate(Dest.VaultDetail(monsterId))
                    },
                )
            }
            composable<Dest.VaultDetail> { back ->
                val dest = back.toRoute<Dest.VaultDetail>()
                MonsterDetailScreen(
                    monsterId = dest.monsterId,
                    onNavigateToPet = { navController.navigateToPet(dest.monsterId) },
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable<Dest.VaultRelease> {
                // Release is handled via inline dialog in VaultScreen
            }
        }

        // ── Scan tab ───────────────────────────────────────────────────────
        navigation<Dest.ScanCamera>(startDestination = Dest.ScanCamera) {
            composable<Dest.ScanCamera> {
                ArScanScreen(
                    onNavigateToCapture = { archetypeId ->
                        navController.navigate(Dest.ScanCapture(archetypeId)) {
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable<Dest.ScanCapture> { back ->
                val dest = back.toRoute<Dest.ScanCapture>()
                CaptureScreen(
                    onCaptureSuccess = { monsterId ->
                        navController.navigate(Dest.PetRoom(monsterId)) {
                            popUpTo(Dest.ScanCamera) { inclusive = false }
                        }
                    },
                )
            }
            composable<Dest.ScanResult> {
                // CaptureResultScreen — V2
            }
        }

        // ── Pet tab ────────────────────────────────────────────────────────
        navigation<Dest.PetRoom>(startDestination = Dest.PetRoom("")) {
            composable<Dest.PetRoom> { back ->
                val dest = back.toRoute<Dest.PetRoom>()
                if (dest.monsterId.isBlank()) {
                    // No monster selected — show vault so user can pick one
                    VaultScreen(
                        onNavigateToPet = { monsterId -> navController.navigateToPet(monsterId) },
                        onNavigateToDetail = { monsterId ->
                            navController.navigate(Dest.VaultDetail(monsterId))
                        },
                    )
                } else {
                    PetRoomScreen(
                        monsterId = dest.monsterId,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToEvolve = { id -> navController.navigateToEvolve(id) },
                        onNavigateToPlay = { id -> navController.navigateToPlayMenu(id) },
                    )
                }
            }
            composable<Dest.PetPlayMenu> { back ->
                val dest = back.toRoute<Dest.PetPlayMenu>()
                PlayMenuScreen(
                    onPlayGame = { gameId ->
                        navController.navigate(
                            Dest.PetPlay(monsterId = dest.monsterId, game = gameId.slug),
                        )
                    },
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable<Dest.PetPlay> { back ->
                val dest = back.toRoute<Dest.PetPlay>()
                val gameId = MiniGameId.fromSlug(dest.game)
                if (gameId == null) {
                    PlaceholderScreen(emoji = "❓", label = "Unknown game", subtitle = dest.game)
                    return@composable
                }
                val onComplete: (com.nightcatchers.core.domain.model.MiniGameOutcome) -> Unit =
                    { outcome ->
                        navController.navigate(
                            Dest.PetPlayResult(
                                monsterId = dest.monsterId,
                                game = gameId.slug,
                                rawScore = outcome.rawScore,
                                scoreBps = (outcome.scoreFraction * 10_000).toInt(),
                            ),
                        ) {
                            popUpTo(Dest.PetPlayMenu(dest.monsterId)) { inclusive = false }
                        }
                    }
                when (gameId) {
                    MiniGameId.CUDDLE_STORM -> CuddleStormScreen(onSessionComplete = onComplete)
                    else -> MiniGamePlaceholderScreen(
                        gameId = gameId,
                        onSessionComplete = onComplete,
                        onCancel = { navController.popBackStack() },
                    )
                }
            }
            composable<Dest.PetPlayResult> { back ->
                val dest = back.toRoute<Dest.PetPlayResult>()
                val gameId = MiniGameId.fromSlug(dest.game)
                    ?: MiniGameId.CUDDLE_STORM
                val fraction = (dest.scoreBps / 10_000f).coerceIn(0f, 1f)
                val outcome = com.nightcatchers.core.domain.model.MiniGameOutcome(
                    gameId = gameId,
                    scoreFraction = fraction,
                    rawScore = dest.rawScore,
                )
                MiniGameResultScreen(
                    gameId = gameId,
                    rawScore = dest.rawScore,
                    scoreFraction = fraction,
                    statDelta = outcome.statDelta(),
                    onPlayAgain = {
                        navController.navigate(Dest.PetPlay(dest.monsterId, gameId.slug)) {
                            popUpTo(Dest.PetPlayMenu(dest.monsterId)) { inclusive = false }
                        }
                    },
                    onBackToMenu = {
                        navController.popBackStack(Dest.PetPlayMenu(dest.monsterId), inclusive = false)
                    },
                )
            }
            composable<Dest.PetEvolve> { back ->
                val dest = back.toRoute<Dest.PetEvolve>()
                PetEvolveScreen(
                    monsterId = dest.monsterId,
                    onDismiss = { navController.popBackStack() },
                )
            }
        }

        // ── Dex tab ────────────────────────────────────────────────────────
        navigation<Dest.Dex>(startDestination = Dest.Dex) {
            composable<Dest.Dex> {
                DexScreen(
                    onNavigateToDetail = { archetypeId ->
                        navController.navigate(Dest.DexDetail(archetypeId))
                    },
                )
            }
            composable<Dest.DexDetail> {
                DexDetailScreen()
            }
            composable<Dest.DexAchievement> { }
            composable<Dest.DexShare> { }
        }

        // ── Settings tab ───────────────────────────────────────────────────
        navigation<Dest.Settings>(startDestination = Dest.Settings) {
            composable<Dest.Settings> {
                SettingsScreen(
                    onNavigateToParental = { navController.navigate(Dest.SettingsParent) },
                )
            }
            composable<Dest.SettingsParent> {
                PinGateScreen(
                    onPinVerified = {
                        navController.navigate(Dest.SettingsParentTime) {
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable<Dest.SettingsParentTime> {
                ParentalDashboardScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPinChange = {
                        navController.navigate(Dest.SettingsParentPinChange) {
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable<Dest.SettingsParentPinChange> {
                PinChangeScreen(onDone = { navController.popBackStack() })
            }
        }
    }
}

// ── Internal placeholder ──────────────────────────────────────────────────

@Composable
internal fun PlaceholderScreen(emoji: String, label: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PetRoomBgTop, DeepNight))),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = emoji, fontSize = 56.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.5f),
            )
        }
    }
}

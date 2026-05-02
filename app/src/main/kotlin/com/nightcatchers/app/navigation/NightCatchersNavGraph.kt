package com.nightcatchers.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.nightcatchers.feature.onboarding.OnboardingScreen
import com.nightcatchers.feature.parental.ParentalDashboardScreen
import com.nightcatchers.feature.parental.PinGateScreen
import com.nightcatchers.feature.pet.PetRoomScreen
import com.nightcatchers.feature.vault.VaultScreen

@Composable
fun NightCatchersNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Dest.Splash,
        modifier = modifier,
    ) {
        composable<Dest.Splash> {
            // SplashScreen — navigates to Onboarding or Home
        }

        composable<Dest.Onboarding> {
            OnboardingScreen(
                onNavigateToHome = {
                    navController.navigate(Dest.Home) {
                        popUpTo(Dest.Onboarding) { inclusive = true }
                    }
                },
            )
        }

        composable<Dest.Home> {
            HomeScreen(navController)
        }

        // ── Scan nested graph ──
        navigation<Dest.ScanCamera>(startDestination = Dest.ScanCamera) {
            composable<Dest.ScanCamera> {
                // CameraScreen
            }
            composable<Dest.ScanFilters> {
                // FiltersScreen
            }
            composable<Dest.ScanCapture> { back ->
                val dest = back.toRoute<Dest.ScanCapture>()
                // CaptureScreen(monsterId = dest.monsterId)
            }
            composable<Dest.ScanResult> { back ->
                val dest = back.toRoute<Dest.ScanResult>()
                // CaptureResultScreen(monsterId = dest.monsterId)
            }
        }

        // ── Vault nested graph ──
        navigation<Dest.Vault>(startDestination = Dest.Vault) {
            composable<Dest.Vault> {
                VaultScreen(
                    onNavigateToPet = { monsterId -> navController.navigateToPet(monsterId) },
                )
            }
            composable<Dest.VaultDetail> { back ->
                val dest = back.toRoute<Dest.VaultDetail>()
                // MonsterDetailScreen(monsterId = dest.monsterId)
            }
            composable<Dest.VaultRelease> { back ->
                val dest = back.toRoute<Dest.VaultRelease>()
                // ReleaseConfirmScreen(monsterId = dest.monsterId)
            }
        }

        // ── Pet nested graph ──
        navigation<Dest.PetRoom>(startDestination = Dest.PetRoom("")) {
            composable<Dest.PetRoom> { back ->
                val dest = back.toRoute<Dest.PetRoom>()
                PetRoomScreen(
                    monsterId = dest.monsterId,
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable<Dest.PetPlay> { back ->
                val dest = back.toRoute<Dest.PetPlay>()
                // MiniGameScreen(monsterId = dest.monsterId, game = dest.game)
            }
            composable<Dest.PetEvolve> { back ->
                val dest = back.toRoute<Dest.PetEvolve>()
                // EvolveScreen(monsterId = dest.monsterId)
            }
        }

        // ── Dex nested graph ──
        navigation<Dest.Dex>(startDestination = Dest.Dex) {
            composable<Dest.Dex> {
                // DexScreen
            }
            composable<Dest.DexAchievement> { back ->
                val dest = back.toRoute<Dest.DexAchievement>()
                // AchievementDetailScreen(id = dest.achievementId)
            }
            composable<Dest.DexShare> { back ->
                val dest = back.toRoute<Dest.DexShare>()
                // ShareScreen(monsterId = dest.monsterId)
            }
        }

        // ── Settings nested graph ──
        navigation<Dest.Settings>(startDestination = Dest.Settings) {
            composable<Dest.Settings> {
                // SettingsScreen
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
                )
            }
        }
    }
}

// Navigation extensions

fun NavHostController.navigateToCapture(monsterId: String) {
    navigate(Dest.ScanCapture(monsterId))
}

fun NavHostController.navigateToPet(monsterId: String) {
    navigate(Dest.PetRoom(monsterId)) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToPetAfterCapture(monsterId: String) {
    navigate(Dest.PetRoom(monsterId)) {
        popUpTo(Dest.Home) { inclusive = false }
    }
}

fun NavHostController.navigateToEvolve(monsterId: String) {
    navigate(Dest.PetEvolve(monsterId)) {
        launchSingleTop = true
    }
}

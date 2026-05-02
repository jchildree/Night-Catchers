package com.nightcatchers.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nightcatchers.app.splash.SplashScreen
import com.nightcatchers.feature.onboarding.OnboardingScreen

/**
 * Root NavHost: Splash → Onboarding or Home.
 * Tab content lives inside HomeScreen → HomeNavGraph.
 */
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
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Dest.Onboarding) {
                        popUpTo(Dest.Splash) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Dest.Home) {
                        popUpTo(Dest.Splash) { inclusive = true }
                    }
                },
            )
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
            HomeScreen()
        }
    }
}

// Navigation helpers used across the app

fun NavHostController.navigateToPet(monsterId: String) {
    navigate(Dest.PetRoom(monsterId)) { launchSingleTop = true }
}

fun NavHostController.navigateToPetAfterCapture(monsterId: String) {
    navigate(Dest.PetRoom(monsterId)) {
        popUpTo(Dest.ScanCamera) { inclusive = false }
    }
}

fun NavHostController.navigateToEvolve(monsterId: String) {
    navigate(Dest.PetEvolve(monsterId)) { launchSingleTop = true }
}

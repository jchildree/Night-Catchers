package com.nightcatchers.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

private val bottomNavItems = listOf(
    BottomNavItem("Scan",     "👁",  Dest.ScanCamera),
    BottomNavItem("Vault",    "🏛",  Dest.Vault),
    BottomNavItem("Pet",      "👾",  Dest.PetRoom("")),
    BottomNavItem("Dex",      "📖",  Dest.Dex),
    BottomNavItem("Settings", "⚙",  Dest.Settings),
)

private data class BottomNavItem(
    val label: String,
    val icon: String,
    val dest: Dest,
)

@Composable
fun HomeScreen(rootNavController: NavHostController) {
    val innerNav = rememberNavController()
    val backstackEntry by innerNav.currentBackStackEntryAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    val selected = backstackEntry?.destination?.hasRoute(item.dest::class) == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            innerNav.navigate(item.dest) {
                                popUpTo(innerNav.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Text(item.icon) },
                        label = { Text(item.label) },
                    )
                }
            }
        }
    ) { padding ->
        NightCatchersNavGraph(
            navController = innerNav,
            modifier = Modifier.padding(padding),
        )
    }
}

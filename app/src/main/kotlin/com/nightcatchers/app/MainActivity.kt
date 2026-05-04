package com.nightcatchers.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.nightcatchers.app.navigation.NightCatchersNavGraph
import com.nightcatchers.core.ui.theme.NightCatchersTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NightCatchersTheme {
                val navController = rememberNavController()
                NightCatchersNavGraph(navController = navController)
            }
        }
    }
}

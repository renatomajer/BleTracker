package com.renatomajer.bletracker.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.renatomajer.bletracker.presentation.main.MainScreen
import com.renatomajer.bletracker.presentation.start.StartScreen

@Composable
fun Navigation(
    onBluetoothStateChanged: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.StartScreen.route) {

        composable(route = Screen.StartScreen.route) {
            StartScreen(navController = navController)
        }

        composable(route = Screen.MainScreen.route) {
            MainScreen(
                onBluetoothStateChanged = onBluetoothStateChanged
            )
        }
    }
}

sealed class Screen(val route: String) {
    data object StartScreen : Screen("start_screen")
    data object MainScreen : Screen("main_screen")
}
package com.renatomajer.bletracker.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph
import androidx.navigation.NavHost
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun Navigation(
    viewModel: MainActivityViewModel,
    onBluetoothStateChanged: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.StartScreen.route) {

        composable(route = Screen.StartScreen.route) {
            StartScreen(navController = navController)
        }

        composable(route = Screen.MainScreen.route) {
            MainScreen(
                onBluetoothStateChanged = onBluetoothStateChanged,
                viewModel = viewModel
            )
        }
    }

}

sealed class Screen(val route: String) {
    data object StartScreen : Screen("start_screen")
    data object MainScreen : Screen("main_screen")
}
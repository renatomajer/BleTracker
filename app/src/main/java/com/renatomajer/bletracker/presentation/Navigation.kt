package com.renatomajer.bletracker.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.renatomajer.bletracker.presentation.main.MainScreen
import com.renatomajer.bletracker.presentation.start.StartScreen

@Composable
fun Navigation(
    modifier: Modifier,
    onBluetoothStateChanged: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Screen.StartScreen.route
    ) {

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
package com.example.myapplication.ui

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.auth.AuthScreen
import com.example.myapplication.ui.sensors.AgroSensScreen
import com.example.myapplication.viewmodel.AuthViewModel

@Composable
fun AppNavigator() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val user by authViewModel.user

    // Listen for user state changes to navigate
    LaunchedEffect(user) {
        if (user != null) {
            navController.navigate("home") {
                // Clear backstack so user can't go back to login
                popUpTo("auth") { inclusive = true }
            }
        } else {
            navController.navigate("auth") {
                popUpTo(0)
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "auth"
    ) {
        composable("auth") {
            AuthScreen(authViewModel)
        }

        composable("home") {
            AgroSensScreen()
        }
    }
}

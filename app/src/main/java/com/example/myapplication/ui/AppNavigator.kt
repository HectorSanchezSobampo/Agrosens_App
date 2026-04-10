package com.example.myapplication.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

    LaunchedEffect(user) {
        if (user != null) {
            navController.navigate("home") {
                popUpTo("auth") { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        modifier = Modifier.fillMaxSize(),
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

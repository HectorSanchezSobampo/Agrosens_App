package com.example.myapplication.ui.navigator

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.auth.LoginScreen
import com.example.myapplication.ui.auth.RegisterScreen
import com.example.myapplication.ui.sensors.AgroSensScreen
import com.example.myapplication.viewmodel.AuthViewModel

private const val ANIM_DURATION = 420

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

    val easing = FastOutSlowInEasing

    NavHost(
        modifier = Modifier.fillMaxSize(),
        navController = navController,
        startDestination = "auth",

        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(ANIM_DURATION, easing = easing)
            ) +
                    fadeIn(animationSpec = tween(ANIM_DURATION)) +
                    scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(ANIM_DURATION, easing = easing)
                    )
        },

        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it / 3 },
                animationSpec = tween(ANIM_DURATION, easing = easing)
            ) +
                    fadeOut(animationSpec = tween(ANIM_DURATION))
        },

        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / 3 },
                animationSpec = tween(ANIM_DURATION, easing = easing)
            ) +
                    fadeIn(animationSpec = tween(ANIM_DURATION)) +
                    scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(ANIM_DURATION, easing = easing)
                    )
        },

        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(ANIM_DURATION, easing = easing)
            ) +
                    fadeOut(animationSpec = tween(ANIM_DURATION))
        }
    ) {
        composable("auth") {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToRegister = { navController.navigate("register") }
            )
        }

        composable("register") {
            RegisterScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable("home") {
            AgroSensScreen()
        }
    }
}
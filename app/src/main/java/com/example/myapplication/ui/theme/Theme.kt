package com.example.myapplication.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Colores personalizados para tu App (AgroSens)
val DarkGrey = Color(0xFF1E232C)
val MediumGrey = Color(0xFF6A707C)
val LightGrey = Color(0xFF8391A1)

private val DarkColorScheme = darkColorScheme(
    primary = Color.White,
    secondary = LightGrey,
    tertiary = MediumGrey,
    background = DarkGrey,
    surface = DarkGrey,
    onPrimary = DarkGrey,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = DarkGrey,
    secondary = MediumGrey,
    tertiary = LightGrey,
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = DarkGrey,
    onSurface = DarkGrey
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Desactivamos dynamicColor por defecto para mantener consistencia con Inter y tus colores
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
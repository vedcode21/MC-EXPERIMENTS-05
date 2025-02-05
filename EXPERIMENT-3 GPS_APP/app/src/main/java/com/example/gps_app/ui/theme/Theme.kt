package com.example.gps_app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Dark theme color scheme
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF800000), // Maroon
    secondary = Color(0xFFB22222), // Firebrick
    tertiary = Color(0xFFCD5C5C)  // Indian Red
)

// Light theme color scheme
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF800000), // Maroon
    secondary = Color(0xFFB22222), // Firebrick
    tertiary = Color(0xFFCD5C5C)  // Indian Red
)

// Define AppTypography instead of the default Typography
val AppTypography = Typography(
    // Customize your typography here if needed, else you can leave it as default
)

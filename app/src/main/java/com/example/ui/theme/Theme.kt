package com.example.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = XynoCyanAccent,
    onPrimary = Color(0xFF003544),
    primaryContainer = Color(0xFF004D63),
    onPrimaryContainer = XynoTealLight,
    secondary = Color(0xFF64B5F6),
    onSecondary = Color(0xFF0D47A1),
    background = XynoDarkBg,
    onBackground = XynoDarkTextPrimary,
    surface = XynoDarkSurface,
    onSurface = XynoDarkTextPrimary,
    surfaceVariant = XynoDarkCard,
    onSurfaceVariant = XynoDarkTextSecondary,
    outline = XynoDarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = XynoBlueAccent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = XynoCyanAccent,
    onSecondary = Color.White,
    background = XynoSurfaceLight,
    onBackground = XynoTextPrimary,
    surface = XynoCardLight,
    onSurface = XynoTextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = XynoTextSecondary,
    outline = XynoCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep corporate branded colors
    content: @Composable () -> Unit,
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

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
    primary = Color(0xFF38BDF8),
    onPrimary = Color(0xFF0C4A6E),
    primaryContainer = Color(0xFF0369A1),
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = Color(0xFF818CF8),
    onSecondary = Color(0xFF1E1B4B),
    secondaryContainer = Color(0xFF3730A3),
    onSecondaryContainer = Color(0xFFEEF2FF),
    tertiary = Color(0xFFF472B6),
    onTertiary = Color(0xFF500724),
    background = PrintDarkBackground,
    surface = PrintDarkSurface,
    surfaceVariant = PrintDarkSurfaceVariant,
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFFCBD5E1),
)

private val LightColorScheme = lightColorScheme(
    primary = PrintPrimary,
    onPrimary = PrintOnPrimary,
    primaryContainer = PrintPrimaryContainer,
    onPrimaryContainer = PrintOnPrimaryContainer,
    secondary = PrintSecondary,
    onSecondary = PrintOnSecondary,
    secondaryContainer = PrintSecondaryContainer,
    onSecondaryContainer = PrintOnSecondaryContainer,
    tertiary = PrintTertiary,
    onTertiary = PrintOnTertiary,
    tertiaryContainer = PrintTertiaryContainer,
    onTertiaryContainer = PrintOnTertiaryContainer,
    background = PrintLightBackground,
    surface = PrintLightSurface,
    surfaceVariant = PrintLightSurfaceVariant,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF475569),
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent graphic studio branding
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

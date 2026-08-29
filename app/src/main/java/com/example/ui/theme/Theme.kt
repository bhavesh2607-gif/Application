package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = HighDensityPrimaryDark,
    onPrimary = HighDensityOnPrimaryDark,
    primaryContainer = HighDensityPrimaryContainerDark,
    onPrimaryContainer = HighDensityOnPrimaryContainerDark,
    secondary = HighDensitySecondaryDark,
    onSecondary = HighDensityOnSecondaryDark,
    secondaryContainer = HighDensitySecondaryContainerDark,
    onSecondaryContainer = HighDensityOnSecondaryContainerDark,
    tertiary = HighDensityTertiaryDark,
    onTertiary = HighDensityOnTertiaryDark,
    background = HighDensityBackgroundDark,
    onBackground = HighDensityOnBackgroundDark,
    surface = HighDensitySurfaceDark,
    onSurface = HighDensityOnSurfaceDark,
    surfaceVariant = HighDensitySurfaceVariantDark,
    onSurfaceVariant = HighDensityOnSurfaceVariantDark,
    outline = HighDensityOutlineDark,
    outlineVariant = HighDensityOutlineVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = HighDensityPrimaryLight,
    onPrimary = HighDensityOnPrimaryLight,
    primaryContainer = HighDensityPrimaryContainerLight,
    onPrimaryContainer = HighDensityOnPrimaryContainerLight,
    secondary = HighDensitySecondaryLight,
    onSecondary = HighDensityOnSecondaryLight,
    secondaryContainer = HighDensitySecondaryContainerLight,
    onSecondaryContainer = HighDensityOnSecondaryContainerLight,
    tertiary = HighDensityTertiaryLight,
    onTertiary = HighDensityOnTertiaryLight,
    background = HighDensityBackgroundLight,
    onBackground = HighDensityOnBackgroundLight,
    surface = HighDensitySurfaceLight,
    onSurface = HighDensityOnSurfaceLight,
    surfaceVariant = HighDensitySurfaceVariantLight,
    onSurfaceVariant = HighDensityOnSurfaceVariantLight,
    outline = HighDensityOutlineLight,
    outlineVariant = HighDensityOutlineVariantLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to ensure High Density theme styling is primary
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


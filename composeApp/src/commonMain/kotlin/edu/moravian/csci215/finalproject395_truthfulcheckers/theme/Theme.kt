package edu.moravian.csci215.finalproject395_truthfulcheckers.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val warmTanScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
)

private val darkEspressoScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
)

private val classicBlueScheme = lightColorScheme(
    primary = primaryBlue,
    onPrimary = onPrimaryBlue,
    background = backgroundBlue,
    onBackground = onBackgroundBlue,
    surface = backgroundBlue,
    onSurface = onBackgroundBlue,
)

@Composable
fun TruthfulCheckersTheme(
    themeName: String = "Warm Tan",
    content: @Composable () -> Unit,
) {
    val colorScheme = when (themeName) {
        "Dark Espresso" -> darkEspressoScheme
        "Classic Blue" -> classicBlueScheme
        else -> warmTanScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content,
    )
}

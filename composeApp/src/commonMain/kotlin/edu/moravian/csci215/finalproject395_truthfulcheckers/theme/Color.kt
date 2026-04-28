package edu.moravian.csci215.finalproject395_truthfulcheckers.theme

import androidx.compose.ui.graphics.Color

// Your Custom Colors
val remeCream = Color(0xFFF1E3D3) // "Reme" text color
val actualTan = Color(0xFFD2B48C) // Actual Tan for backgrounds
val warmBrown = Color(0xFF8D6E63) // Lighter Brown primary
val deepBrown = Color(0xFF5D4037) // Deep Brown for containers

// Light Theme Mapping
val primaryLight = warmBrown
val onPrimaryLight = remeCream // Cream text on brown (Buttons, etc.)

val primaryContainerLight = deepBrown
val onPrimaryContainerLight = remeCream // Cream text on deep brown

val secondaryLight = actualTan
val onSecondaryLight = deepBrown

val secondaryContainerLight = Color(0xFFE6D5B8)
val onSecondaryContainerLight = deepBrown

val tertiaryLight = Color(0xFF7D5800)
val onTertiaryLight = remeCream
val tertiaryContainerLight = Color(0xFFFFDDB1)
val onTertiaryContainerLight = Color(0xFF281900)

val errorLight = Color(0xFFBA1A1A)
val onErrorLight = remeCream // Cream text on Error boxes

val backgroundLight = actualTan // Full Tan Background
val onBackgroundLight = deepBrown // Brown text on Tan background

val surfaceLight = actualTan
val onSurfaceLight = deepBrown

val surfaceVariantLight = Color(0xFFE6D5B8)
val onSurfaceVariantLight = deepBrown

val outlineLight = Color(0xFF85736B)

// Dark Theme Variants (Synced to match the earthy aesthetic)
val primaryDark = Color(0xFFE6C1A4)
val onPrimaryDark = deepBrown
val primaryContainerDark = warmBrown
val onPrimaryContainerDark = remeCream

val secondaryDark = actualTan
val onSecondaryDark = deepBrown
val secondaryContainerDark = Color(0xFF4E4735)
val onSecondaryContainerDark = Color(0xFFEEE3D5)

val backgroundDark = Color(0xFF1A110F)
val onBackgroundDark = remeCream
val surfaceDark = Color(0xFF1A110F)
val onSurfaceDark = remeCream

val tertiaryDark = warmBrown
val onTertiaryDark = remeCream
val tertiaryContainerDark = deepBrown
val onTertiaryContainerDark = remeCream

val outlineDark = Color(0xFF9F8D89)

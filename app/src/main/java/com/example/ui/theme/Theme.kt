package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val VoxForgeDarkColorScheme =
  darkColorScheme(
    primary = CrimsonPrimary,
    onPrimary = TextPrimary,
    primaryContainer = CrimsonContainer,
    onPrimaryContainer = CrimsonSoft,
    secondary = CyberCyan,
    onSecondary = ObsidianDeep,
    secondaryContainer = CyberCyanContainer,
    onSecondaryContainer = CyberCyan,
    tertiary = EmeraldActive,
    onTertiary = ObsidianDeep,
    tertiaryContainer = EmeraldContainer,
    onTertiaryContainer = EmeraldActive,
    background = ObsidianDeep,
    onBackground = TextPrimary,
    surface = ObsidianSurface,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = TextSecondary,
    surfaceContainerHigh = ObsidianCardElevated,
    outline = OutlineSubtle,
    outlineVariant = CrimsonPrimary,
    error = CrimsonBright,
    onError = TextPrimary,
  )

val VoxForgeShapes =
  Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
  )

@Composable
fun VoxForgeTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = VoxForgeDarkColorScheme,
    typography = VoxForgeTypography,
    shapes = VoxForgeShapes,
    content = content,
  )
}

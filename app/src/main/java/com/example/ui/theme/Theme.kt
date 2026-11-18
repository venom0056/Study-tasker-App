package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val StudyDarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = TextPrimary,
    primaryContainer = ElectricBlueBright,
    onPrimaryContainer = TextPrimary,
    secondary = PhysicsCyan,
    onSecondary = MidnightBg,
    tertiary = EmeraldDone,
    onTertiary = MidnightBg,
    background = MidnightBg,
    onBackground = TextPrimary,
    surface = CardSurface,
    onSurface = TextPrimary,
    surfaceVariant = CardElevated,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderSubtle,
    error = CrimsonRed,
    onError = TextPrimary,
    errorContainer = CrimsonRedBg,
    onErrorContainer = CrimsonBorder
)

@Composable
fun StudyTaskManagerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StudyDarkColorScheme,
        typography = Typography,
        content = content
    )
}

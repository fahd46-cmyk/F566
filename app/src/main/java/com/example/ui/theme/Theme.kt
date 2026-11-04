package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JissrDarkColorScheme = darkColorScheme(
    primary = JissrEmerald,
    onPrimary = Color(0xFF032219),
    primaryContainer = JissrEmeraldContainer,
    onPrimaryContainer = JissrEmerald,
    secondary = JissrCyan,
    onSecondary = Color(0xFF042233),
    secondaryContainer = JissrCyanContainer,
    onSecondaryContainer = JissrCyan,
    tertiary = JissrAmber,
    onTertiary = Color(0xFF2B1B02),
    tertiaryContainer = JissrAmberContainer,
    onTertiaryContainer = JissrAmber,
    error = JissrCrimson,
    onError = Color(0xFF2D0610),
    errorContainer = JissrCrimsonContainer,
    onErrorContainer = JissrCrimson,
    background = ObsidianBg,
    onBackground = TextPrimaryDark,
    surface = ObsidianSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = ObsidianSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderSubtleDark
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = JissrDarkColorScheme,
        typography = Typography,
        content = content
    )
}

package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NovaDarkColorScheme = darkColorScheme(
    primary = CyanNeon,
    onPrimary = VoidBlack,
    primaryContainer = Color(0xFF003848),
    onPrimaryContainer = CyanNeon,
    secondary = VioletNeon,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF381254),
    onSecondaryContainer = Color(0xFFE9D5FF),
    tertiary = EmeraldPulse,
    onTertiary = VoidBlack,
    background = VoidBlack,
    onBackground = TextPrimaryDark,
    surface = ObsidianDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDark,
    error = CoralError,
    onError = Color.White
)

private val NovaLightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFF7E22CE),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E8FF),
    onSecondaryContainer = Color(0xFF6B21A8),
    tertiary = Color(0xFF059669),
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    error = Color(0xFFDC2626),
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve bespoke Incredible AI aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) NovaDarkColorScheme else NovaDarkColorScheme // Keep immersive dark HUD by default

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.example.domain.model.AppColorPalette
import com.example.domain.model.AppThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = BrandTealPrimaryDark,
    onPrimary = BrandTealOnPrimaryDark,
    primaryContainer = BrandTealContainerDark,
    onPrimaryContainer = BrandTealOnContainerDark,
    secondary = BrandSecondaryDark,
    onSecondary = BrandOnSecondaryDark,
    secondaryContainer = BrandSecondaryContainerDark,
    onSecondaryContainer = BrandOnSecondaryContainerDark,
    background = BrandBackgroundDark,
    surface = BrandSurfaceDark,
    surfaceVariant = BrandSurfaceVariantDark,
    onBackground = BrandOnSurfaceDark,
    onSurface = BrandOnSurfaceDark,
    onSurfaceVariant = BrandOnSurfaceVariantDark,
    outline = BrandOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = BrandTealPrimary,
    onPrimary = BrandTealOnPrimary,
    primaryContainer = BrandTealContainer,
    onPrimaryContainer = BrandTealOnContainer,
    secondary = BrandSecondary,
    onSecondary = BrandOnSecondary,
    secondaryContainer = BrandSecondaryContainer,
    onSecondaryContainer = BrandOnSecondaryContainer,
    background = BrandBackground,
    surface = BrandSurface,
    surfaceVariant = BrandSurfaceVariant,
    onBackground = BrandOnSurface,
    onSurface = BrandOnSurface,
    onSurfaceVariant = BrandOnSurfaceVariant,
    outline = BrandOutline
)

@Composable
fun WhatsAppBusinessInboxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    colorPalette: AppColorPalette = AppColorPalette.TEAL,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> darkTheme
    }

    val baseScheme = if (isDark) DarkColorScheme else LightColorScheme
    val dynamicScheme = baseScheme.copy(
        primary = colorPalette.primary,
        primaryContainer = if (isDark) colorPalette.primary.copy(alpha = 0.25f) else colorPalette.container
    )

    MaterialTheme(
        colorScheme = dynamicScheme,
        typography = Typography,
        content = content
    )
}

package com.example.domain.model

import androidx.compose.ui.graphics.Color

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class AppColorPalette(val label: String, val primary: Color, val container: Color) {
    TEAL("WhatsApp Classic", Color(0xFF0F766E), Color(0xFFCCFBF1)),
    OCEAN("Royal Ocean", Color(0xFF1D4ED8), Color(0xFFDBEAFE)),
    AMETHYST("Velvet Amethyst", Color(0xFF7E22CE), Color(0xFFF3E8FF)),
    FOREST("Emerald Forest", Color(0xFF15803D), Color(0xFFDCFCE7)),
    SUNSET("Sunset Terracotta", Color(0xFFC2410C), Color(0xFFFFEDD5)),
    OBSIDIAN("Midnight Slate", Color(0xFF334155), Color(0xFFF1F5F9))
}

enum class FontSizeOption(val label: String, val scaleFactor: Float, val sampleSp: Int) {
    SMALL("Compact", 0.88f, 13),
    MEDIUM("Standard", 1.0f, 15),
    LARGE("Large Text", 1.15f, 17)
}

enum class WallpaperType {
    DEFAULT,
    SOLID,
    GRADIENT,
    PATTERN,
    NATURE,
    CUSTOM
}

data class WallpaperItem(
    val id: String,
    val name: String,
    val type: WallpaperType,
    val colorHex: String? = null,
    val gradientColors: List<Long>? = null,
    val patternType: String? = null,
    val imageUrl: String? = null
)

data class BubbleColorPreset(
    val id: String,
    val name: String,
    val outgoingColor: Long,
    val incomingColor: Long,
    val outgoingTextColor: Long,
    val incomingTextColor: Long
)

data class ChatThemeConfig(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val colorPalette: AppColorPalette = AppColorPalette.TEAL,
    val fontSize: FontSizeOption = FontSizeOption.MEDIUM,
    val outgoingBubbleColor: Long = 0xFF0F766EL,
    val incomingBubbleColor: Long = 0xFFF1F5F9L,
    val wallpaperId: String = "default",
    val customWallpaperUri: String? = null
)

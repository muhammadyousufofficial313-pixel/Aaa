package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.domain.model.AppColorPalette
import com.example.domain.model.AppThemeMode
import com.example.domain.model.BubbleColorPreset
import com.example.domain.model.ChatThemeConfig
import com.example.domain.model.FontSizeOption
import com.example.domain.model.WallpaperItem
import com.example.domain.model.WallpaperType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CustomizationPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("wa_customization_prefs", Context.MODE_PRIVATE)

    private val _themeConfig = MutableStateFlow(loadConfig())
    val themeConfig: StateFlow<ChatThemeConfig> = _themeConfig.asStateFlow()

    companion object {
        val PRESET_WALLPAPERS = listOf(
            WallpaperItem(
                id = "default",
                name = "Default Inbox",
                type = WallpaperType.DEFAULT
            ),
            // Solid Colors
            WallpaperItem(
                id = "solid_teal_dark",
                name = "Slate Teal",
                type = WallpaperType.SOLID,
                colorHex = "#0D2825"
            ),
            WallpaperItem(
                id = "solid_dark_navy",
                name = "Midnight Navy",
                type = WallpaperType.SOLID,
                colorHex = "#0F172A"
            ),
            WallpaperItem(
                id = "solid_classic_wa",
                name = "Classic WA Dark",
                type = WallpaperType.SOLID,
                colorHex = "#0B141B"
            ),
            WallpaperItem(
                id = "solid_warm_beige",
                name = "Warm Sand",
                type = WallpaperType.SOLID,
                colorHex = "#EFEAE2"
            ),
            WallpaperItem(
                id = "solid_soft_pearl",
                name = "Soft Pearl",
                type = WallpaperType.SOLID,
                colorHex = "#F3F4F6"
            ),
            WallpaperItem(
                id = "solid_charcoal",
                name = "Pure Charcoal",
                type = WallpaperType.SOLID,
                colorHex = "#18181B"
            ),

            // Gradients
            WallpaperItem(
                id = "grad_emerald",
                name = "Emerald Mist",
                type = WallpaperType.GRADIENT,
                gradientColors = listOf(0xFF042F2E, 0xFF064E3B, 0xFF022C22)
            ),
            WallpaperItem(
                id = "grad_sunset",
                name = "Sunset Glow",
                type = WallpaperType.GRADIENT,
                gradientColors = listOf(0xFF7C2D12, 0xFF9A3412, 0xFF431407)
            ),
            WallpaperItem(
                id = "grad_ocean",
                name = "Deep Ocean",
                type = WallpaperType.GRADIENT,
                gradientColors = listOf(0xFF1E3A8A, 0xFF0F172A, 0xFF172554)
            ),
            WallpaperItem(
                id = "grad_twilight",
                name = "Twilight Lavender",
                type = WallpaperType.GRADIENT,
                gradientColors = listOf(0xFF581C87, 0xFF3B0764, 0xFF1E1B4B)
            ),

            // Patterns
            WallpaperItem(
                id = "pattern_doodles",
                name = "WhatsApp Doodles",
                type = WallpaperType.PATTERN,
                patternType = "DOODLES"
            ),
            WallpaperItem(
                id = "pattern_dots",
                name = "Dot Matrix",
                type = WallpaperType.PATTERN,
                patternType = "DOTS"
            ),
            WallpaperItem(
                id = "pattern_grid",
                name = "Minimal Grid",
                type = WallpaperType.PATTERN,
                patternType = "GRID"
            ),

            // Nature & Minimal Collections
            WallpaperItem(
                id = "nature_mountain",
                name = "Alpine Forest",
                type = WallpaperType.NATURE,
                imageUrl = "https://images.unsplash.com/photo-1511497584788-87676104235f?w=800"
            ),
            WallpaperItem(
                id = "nature_desert",
                name = "Sahara Dunes",
                type = WallpaperType.NATURE,
                imageUrl = "https://images.unsplash.com/photo-1509316975850-ff9c5deb0cd9?w=800"
            ),
            WallpaperItem(
                id = "minimal_architecture",
                name = "Minimalist Architecture",
                type = WallpaperType.NATURE,
                imageUrl = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=800"
            )
        )

        val PRESET_BUBBLES = listOf(
            BubbleColorPreset(
                id = "bubble_teal",
                name = "Teal Emerald",
                outgoingColor = 0xFF0F766EL,
                incomingColor = 0xFFF1F5F9L,
                outgoingTextColor = 0xFFFFFFFFL,
                incomingTextColor = 0xFF0F172AL
            ),
            BubbleColorPreset(
                id = "bubble_ocean",
                name = "Royal Ocean",
                outgoingColor = 0xFF1D4ED8L,
                incomingColor = 0xFFEFF6FFL,
                outgoingTextColor = 0xFFFFFFFFL,
                incomingTextColor = 0xFF1E3A8AL
            ),
            BubbleColorPreset(
                id = "bubble_amethyst",
                name = "Amethyst Purple",
                outgoingColor = 0xFF7E22CEL,
                incomingColor = 0xFFFAF5FFL,
                outgoingTextColor = 0xFFFFFFFFL,
                incomingTextColor = 0xFF3B0764L
            ),
            BubbleColorPreset(
                id = "bubble_forest",
                name = "Forest Green",
                outgoingColor = 0xFF15803DL,
                incomingColor = 0xFFF0FDF4L,
                outgoingTextColor = 0xFFFFFFFFL,
                incomingTextColor = 0xFF14532DL
            ),
            BubbleColorPreset(
                id = "bubble_terracotta",
                name = "Terracotta Amber",
                outgoingColor = 0xFFC2410CL,
                incomingColor = 0xFFFFF7EDL,
                outgoingTextColor = 0xFFFFFFFFL,
                incomingTextColor = 0xFF431407L
            ),
            BubbleColorPreset(
                id = "bubble_dark_mono",
                name = "Stealth Charcoal",
                outgoingColor = 0xFF27272AL,
                incomingColor = 0xFF3F3F46L,
                outgoingTextColor = 0xFFF4F4F5L,
                incomingTextColor = 0xFFF4F4F5L
            )
        )
    }

    private fun loadConfig(): ChatThemeConfig {
        val themeModeStr = prefs.getString("theme_mode", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
        val colorPaletteStr = prefs.getString("color_palette", AppColorPalette.TEAL.name) ?: AppColorPalette.TEAL.name
        val fontSizeStr = prefs.getString("font_size", FontSizeOption.MEDIUM.name) ?: FontSizeOption.MEDIUM.name
        val outgoingBubble = prefs.getLong("outgoing_bubble", 0xFF0F766EL)
        val incomingBubble = prefs.getLong("incoming_bubble", 0xFFF1F5F9L)
        val wallpaperId = prefs.getString("wallpaper_id", "default") ?: "default"
        val customUri = prefs.getString("custom_wallpaper_uri", null)

        val themeMode = runCatching { AppThemeMode.valueOf(themeModeStr) }.getOrDefault(AppThemeMode.SYSTEM)
        val colorPalette = runCatching { AppColorPalette.valueOf(colorPaletteStr) }.getOrDefault(AppColorPalette.TEAL)
        val fontSize = runCatching { FontSizeOption.valueOf(fontSizeStr) }.getOrDefault(FontSizeOption.MEDIUM)

        return ChatThemeConfig(
            themeMode = themeMode,
            colorPalette = colorPalette,
            fontSize = fontSize,
            outgoingBubbleColor = outgoingBubble,
            incomingBubbleColor = incomingBubble,
            wallpaperId = wallpaperId,
            customWallpaperUri = customUri
        )
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _themeConfig.value = _themeConfig.value.copy(themeMode = mode)
    }

    fun setColorPalette(palette: AppColorPalette) {
        prefs.edit().putString("color_palette", palette.name).apply()
        _themeConfig.value = _themeConfig.value.copy(colorPalette = palette)
    }

    fun setFontSize(size: FontSizeOption) {
        prefs.edit().putString("font_size", size.name).apply()
        _themeConfig.value = _themeConfig.value.copy(fontSize = size)
    }

    fun setBubbleColors(outgoing: Long, incoming: Long) {
        prefs.edit()
            .putLong("outgoing_bubble", outgoing)
            .putLong("incoming_bubble", incoming)
            .apply()
        _themeConfig.value = _themeConfig.value.copy(
            outgoingBubbleColor = outgoing,
            incomingBubbleColor = incoming
        )
    }

    fun setWallpaper(wallpaperId: String, customUri: String? = null) {
        prefs.edit()
            .putString("wallpaper_id", wallpaperId)
            .putString("custom_wallpaper_uri", customUri)
            .apply()
        _themeConfig.value = _themeConfig.value.copy(
            wallpaperId = wallpaperId,
            customWallpaperUri = customUri
        )
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
        _themeConfig.value = ChatThemeConfig()
    }
}

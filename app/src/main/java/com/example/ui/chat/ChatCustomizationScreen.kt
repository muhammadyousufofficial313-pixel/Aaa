package com.example.ui.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.local.CustomizationPreferences
import com.example.domain.model.AppColorPalette
import com.example.domain.model.AppThemeMode
import com.example.domain.model.BubbleColorPreset
import com.example.domain.model.FontSizeOption
import com.example.domain.model.WallpaperItem
import com.example.domain.model.WallpaperType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatCustomizationScreen(
    preferences: CustomizationPreferences,
    conversationId: String? = null,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val config by preferences.themeConfig.collectAsStateWithLifecycle()
    var selectedWallpaperTab by remember { mutableIntStateOf(0) } // 0=Solid, 1=Gradient, 2=Patterns, 3=Nature

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            preferences.setWallpaper("custom_user_image", uri.toString())
        }
    }

    val activeWallpaper = remember(config.wallpaperId) {
        CustomizationPreferences.PRESET_WALLPAPERS.find { it.id == config.wallpaperId }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Chat Appearance & Theme", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            text = if (conversationId != null) "Customize this conversation" else "Global Chat Defaults",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_customization_back")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { preferences.resetToDefaults() },
                        modifier = Modifier.testTag("btn_reset_customization")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset to Defaults")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- 1. LIVE CHAT PREVIEW ---
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "LIVE INTERACTIVE PREVIEW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    WallpaperBackground(
                        wallpaper = activeWallpaper,
                        customUri = config.customWallpaperUri,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Customer incoming bubble preview
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 4.dp, bottomEnd = 14.dp))
                                    .background(Color(config.incomingBubbleColor))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Hello! Could you confirm wholesale delivery dates?",
                                        fontSize = (14 * config.fontSize.scaleFactor).sp,
                                        color = if (Color(config.incomingBubbleColor).luminance() > 0.5f) Color(0xFF0F172A) else Color.White
                                    )
                                    Text(
                                        text = "10:42 AM",
                                        fontSize = 9.sp,
                                        color = if (Color(config.incomingBubbleColor).luminance() > 0.5f) Color(0xFF64748B) else Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }
                            }

                            // Business outgoing bubble preview
                            Box(
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 4.dp))
                                    .background(Color(config.outgoingBubbleColor))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Certainly! Shipments depart Monday with tracking.",
                                        fontSize = (14 * config.fontSize.scaleFactor).sp,
                                        color = if (Color(config.outgoingBubbleColor).luminance() > 0.5f) Color(0xFF0F172A) else Color.White
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "10:43 AM",
                                            fontSize = 9.sp,
                                            color = Color.White.copy(alpha = 0.75f)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.DoneAll,
                                            contentDescription = null,
                                            tint = Color(0xFF22D3EE),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- 2. APP THEME MODE (LIGHT / DARK / SYSTEM) ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "App Theme Mode",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppThemeMode.entries.forEach { mode ->
                        val isSelected = config.themeMode == mode
                        FilterChip(
                            selected = isSelected,
                            onClick = { preferences.setThemeMode(mode) },
                            label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // --- 3. APP COLOR PALETTE PRESETS ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Accent Color Palette",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(AppColorPalette.entries.toTypedArray()) { palette ->
                        val isSelected = config.colorPalette == palette
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { preferences.setColorPalette(palette) }
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(palette.primary)
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = palette.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // --- 4. BUBBLE COLOR PRESETS ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Message Bubble Colors",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(CustomizationPreferences.PRESET_BUBBLES) { preset ->
                        val isSelected = config.outgoingBubbleColor == preset.outgoingColor &&
                                config.incomingBubbleColor == preset.incomingColor

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier
                                .width(120.dp)
                                .clickable {
                                    preferences.setBubbleColors(preset.outgoingColor, preset.incomingColor)
                                }
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(preset.outgoingColor))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(preset.incomingColor))
                                            .border(1.dp, Color.Gray.copy(alpha = 0.4f), CircleShape)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = preset.name,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // --- 5. FONT SIZE SCALE ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Chat Font Size",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FontSizeOption.entries.forEach { opt ->
                        val isSelected = config.fontSize == opt
                        FilterChip(
                            selected = isSelected,
                            onClick = { preferences.setFontSize(opt) },
                            label = { Text(opt.label) },
                            leadingIcon = {
                                Icon(Icons.Default.FormatSize, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // --- 6. CHAT WALLPAPERS ---
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Chat Wallpaper",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                TabRow(selectedTabIndex = selectedWallpaperTab) {
                    Tab(
                        selected = selectedWallpaperTab == 0,
                        onClick = { selectedWallpaperTab = 0 },
                        text = { Text("Solid", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedWallpaperTab == 1,
                        onClick = { selectedWallpaperTab = 1 },
                        text = { Text("Gradients", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedWallpaperTab == 2,
                        onClick = { selectedWallpaperTab = 2 },
                        text = { Text("Patterns", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedWallpaperTab == 3,
                        onClick = { selectedWallpaperTab = 3 },
                        text = { Text("Nature", fontSize = 12.sp) }
                    )
                }

                // Filter wallpapers by tab
                val filteredWallpapers = remember(selectedWallpaperTab) {
                    when (selectedWallpaperTab) {
                        0 -> CustomizationPreferences.PRESET_WALLPAPERS.filter { it.type == WallpaperType.SOLID || it.type == WallpaperType.DEFAULT }
                        1 -> CustomizationPreferences.PRESET_WALLPAPERS.filter { it.type == WallpaperType.GRADIENT }
                        2 -> CustomizationPreferences.PRESET_WALLPAPERS.filter { it.type == WallpaperType.PATTERN }
                        else -> CustomizationPreferences.PRESET_WALLPAPERS.filter { it.type == WallpaperType.NATURE }
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredWallpapers) { wall ->
                        val isSelected = config.wallpaperId == wall.id && config.customWallpaperUri == null

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(88.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    preferences.setWallpaper(wall.id, null)
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 88.dp, height = 110.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            ) {
                                when (wall.type) {
                                    WallpaperType.SOLID -> {
                                        val c = remember(wall.colorHex) {
                                            runCatching { Color(android.graphics.Color.parseColor(wall.colorHex)) }.getOrDefault(Color.DarkGray)
                                        }
                                        Box(modifier = Modifier.fillMaxSize().background(c))
                                    }
                                    WallpaperType.GRADIENT -> {
                                        val brush = remember(wall.gradientColors) {
                                            Brush.verticalGradient(wall.gradientColors?.map { Color(it) } ?: listOf(Color.DarkGray, Color.Black))
                                        }
                                        Box(modifier = Modifier.fillMaxSize().background(brush))
                                    }
                                    WallpaperType.NATURE -> {
                                        AsyncImage(
                                            model = wall.imageUrl,
                                            contentDescription = wall.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    WallpaperType.PATTERN -> {
                                        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant)) {
                                            if (wall.patternType == "DOODLES") DoodlesPatternCanvas()
                                            else DotsPatternCanvas()
                                        }
                                    }
                                    WallpaperType.DEFAULT -> {
                                        Box(
                                            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("Default", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    else -> {}
                                }

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = wall.name,
                                fontSize = 11.sp,
                                maxLines = 1,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Pick custom image button
                OutlinedButton(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_pick_custom_wallpaper"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Custom Wallpaper from Gallery")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Reset to Defaults
            Button(
                onClick = { preferences.resetToDefaults() },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_reset_all_customizations"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset All Chat Customizations to Default")
            }
        }
    }
}

// Utility extension for luminance calculation without android.graphics.Color
fun Color.luminance(): Float {
    return (0.299f * red + 0.587f * green + 0.114f * blue)
}

package com.example.ui.chat

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.unit.dp
import com.example.domain.model.WallpaperItem
import com.example.domain.model.WallpaperType

@Composable
fun WallpaperBackground(
    wallpaper: WallpaperItem?,
    customUri: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            // Custom user-selected image
            !customUri.isNullOrBlank() -> {
                AsyncImage(
                    model = customUri,
                    contentDescription = "Custom Chat Wallpaper",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Dark contrast scrim to ensure high text legibility
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f))
                )
            }
            // Curated Nature photo
            wallpaper?.type == WallpaperType.NATURE && !wallpaper.imageUrl.isNullOrBlank() -> {
                AsyncImage(
                    model = wallpaper.imageUrl,
                    contentDescription = wallpaper.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f))
                )
            }
            // Solid color
            wallpaper?.type == WallpaperType.SOLID && !wallpaper.colorHex.isNullOrBlank() -> {
                val color = remember(wallpaper.colorHex) {
                    runCatching {
                        Color(android.graphics.Color.parseColor(wallpaper.colorHex))
                    }.getOrDefault(Color(0xFF0F172A))
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color)
                )
            }
            // Gradient
            wallpaper?.type == WallpaperType.GRADIENT && !wallpaper.gradientColors.isNullOrEmpty() -> {
                val brush = remember(wallpaper.gradientColors) {
                    val colors = wallpaper.gradientColors.map { Color(it) }
                    Brush.verticalGradient(colors)
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(brush)
                )
            }
            // Pattern
            wallpaper?.type == WallpaperType.PATTERN -> {
                val baseBg = MaterialTheme.colorScheme.background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(baseBg)
                ) {
                    when (wallpaper.patternType) {
                        "DOODLES" -> DoodlesPatternCanvas()
                        "DOTS" -> DotsPatternCanvas()
                        else -> GridPatternCanvas()
                    }
                }
            }
            // Default
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                )
            }
        }

        // Child content (messages list, input, etc.)
        content()
    }
}

@Composable
fun DoodlesPatternCanvas(modifier: Modifier = Modifier) {
    val doodleTint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.07f)

    Canvas(modifier = modifier.fillMaxSize()) {
        val step = 80.dp.toPx()
        val cols = (size.width / step).toInt() + 1
        val rows = (size.height / step).toInt() + 1

        for (r in 0..rows) {
            for (c in 0..cols) {
                val x = c * step + (if (r % 2 == 1) step / 2f else 0f)
                val y = r * step

                val iconType = (r * 3 + c) % 4
                when (iconType) {
                    0 -> {
                        // Mini chat bubble
                        drawRoundRect(
                            color = doodleTint,
                            topLeft = Offset(x, y),
                            size = Size(18.dp.toPx(), 12.dp.toPx()),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }
                    1 -> {
                        // Checkmark
                        val path = Path().apply {
                            moveTo(x, y + 6.dp.toPx())
                            lineTo(x + 4.dp.toPx(), y + 10.dp.toPx())
                            lineTo(x + 12.dp.toPx(), y + 2.dp.toPx())
                        }
                        drawPath(path, color = doodleTint, style = Stroke(width = 1.5.dp.toPx()))
                    }
                    2 -> {
                        // Star
                        drawCircle(
                            color = doodleTint,
                            radius = 4.dp.toPx(),
                            center = Offset(x + 6.dp.toPx(), y + 6.dp.toPx()),
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }
                    else -> {
                        // Mini diamond
                        val path = Path().apply {
                            moveTo(x + 6.dp.toPx(), y)
                            lineTo(x + 12.dp.toPx(), y + 6.dp.toPx())
                            lineTo(x + 6.dp.toPx(), y + 12.dp.toPx())
                            lineTo(x, y + 6.dp.toPx())
                            close()
                        }
                        drawPath(path, color = doodleTint, style = Stroke(width = 1.2.dp.toPx()))
                    }
                }
            }
        }
    }
}

@Composable
fun DotsPatternCanvas(modifier: Modifier = Modifier) {
    val dotTint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f)

    Canvas(modifier = modifier.fillMaxSize()) {
        val step = 32.dp.toPx()
        val cols = (size.width / step).toInt() + 1
        val rows = (size.height / step).toInt() + 1

        for (r in 0..rows) {
            for (c in 0..cols) {
                drawCircle(
                    color = dotTint,
                    radius = 2.dp.toPx(),
                    center = Offset(c * step, r * step)
                )
            }
        }
    }
}

@Composable
fun GridPatternCanvas(modifier: Modifier = Modifier) {
    val lineTint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)

    Canvas(modifier = modifier.fillMaxSize()) {
        val step = 40.dp.toPx()
        val cols = (size.width / step).toInt() + 1
        val rows = (size.height / step).toInt() + 1

        for (c in 0..cols) {
            val x = c * step
            drawLine(
                color = lineTint,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1.dp.toPx()
            )
        }

        for (r in 0..rows) {
            val y = r * step
            drawLine(
                color = lineTint,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

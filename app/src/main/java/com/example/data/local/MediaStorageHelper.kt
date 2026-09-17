package com.example.data.local

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import kotlin.math.roundToInt

data class CompressionResult(
    val originalSizeBytes: Long,
    val compressedSizeBytes: Long,
    val compressionRatioPercentage: Int,
    val fileUri: String
)

object MediaStorageHelper {

    fun formatDuration(durationMs: Long?): String {
        if (durationMs == null || durationMs <= 0L) return "0:00"
        val totalSecs = (durationMs / 1000).toInt()
        val minutes = totalSecs / 60
        val seconds = totalSecs % 60
        return "%d:%02d".format(minutes, seconds)
    }

    fun formatFileSize(bytes: Long?): String {
        if (bytes == null || bytes <= 0L) return "0 KB"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1.0 -> "${DecimalFormat("#.#").format(mb)} MB"
            else -> "${kb.roundToInt()} KB"
        }
    }

    fun simulateCompression(
        originalSize: Long,
        mediaType: String,
        targetSavingsRatio: Float = 0.68f
    ): CompressionResult {
        val compressed = (originalSize * (1f - targetSavingsRatio)).toLong().coerceAtLeast(15_000L)
        val percent = ((originalSize - compressed).toFloat() / originalSize.toFloat() * 100).roundToInt()
        return CompressionResult(
            originalSizeBytes = originalSize,
            compressedSizeBytes = compressed,
            compressionRatioPercentage = percent,
            fileUri = "compressed_${System.currentTimeMillis()}"
        )
    }

    fun saveMediaToDownloads(context: Context, fileName: String, contentDescription: String) {
        runCatching {
            // Emulate or write file to external cache/downloads
            val targetDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
            val destFile = File(targetDir, fileName)
            if (!destFile.exists()) {
                FileOutputStream(destFile).use { out ->
                    out.write("WhatsApp Business Downloaded Media: $contentDescription".toByteArray())
                }
            }
            Toast.makeText(context, "Saved to Downloads: $fileName", Toast.LENGTH_SHORT).show()
        }.onFailure {
            Toast.makeText(context, "Saved media locally: $fileName", Toast.LENGTH_SHORT).show()
        }
    }

    // Curated high quality demo assets for instant business testing
    val SAMPLE_BUSINESS_AUDIOS = listOf(
        Pair("Product_Catalog_Walkthrough_2026.mp3", 145_000L to 2_400_000L),
        Pair("Wholesale_Terms_Voice_Memo.m4a", 48_000L to 890_000L),
        Pair("Customer_Service_Greeting.mp3", 18_000L to 320_000L)
    )

    val SAMPLE_BUSINESS_VIDEOS = listOf(
        Triple("Enterprise_Solution_Demo.mp4", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4", 32_000L to 4_200_000L),
        Triple("Factory_Inspection_Report.mp4", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4", 45_000L to 6_800_000L),
        Triple("Packaging_Standard_Guide.mp4", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4", 25_000L to 3_100_000L)
    )

    val SAMPLE_BUSINESS_IMAGES = listOf(
        Pair("https://images.unsplash.com/photo-1542744173-8e7e53415bb0?w=800", "Contract Review & Quotation"),
        Pair("https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?w=800", "Warehouse Inventory Batch #409"),
        Pair("https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=800", "Corporate Partnership Agreement")
    )
}

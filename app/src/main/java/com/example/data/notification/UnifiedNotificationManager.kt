package com.example.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity

enum class PushPlatform {
    GOOGLE_FCM,
    HUAWEI_HMS,
    FALLBACK_POLLING
}

class UnifiedNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_MESSAGES_ID = "channel_whatsapp_messages"
        const val CHANNEL_SYSTEM_ID = "channel_whatsapp_system"
        private const val NOTIFICATION_ID_BASE = 1000
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val messageChannel = NotificationChannel(
                CHANNEL_MESSAGES_ID,
                "WhatsApp Customer Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High priority notifications for incoming customer WhatsApp messages"
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
            }

            val systemChannel = NotificationChannel(
                CHANNEL_SYSTEM_ID,
                "WhatsApp Platform & Connection Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Status updates regarding WABA connection, tokens, and templates"
            }

            notificationManager.createNotificationChannels(listOf(messageChannel, systemChannel))
        }
    }

    /**
     * Determines whether the current device is Huawei HMS, Google GMS, or standard Android
     */
    fun detectDevicePushPlatform(): PushPlatform {
        val isHuawei = Build.MANUFACTURER.contains("HUAWEI", ignoreCase = true) ||
                Build.BRAND.contains("HUAWEI", ignoreCase = true) ||
                Build.BRAND.contains("HONOR", ignoreCase = true)

        val hasHms = runCatching {
            Class.forName("com.huawei.hms.api.HuaweiApiAvailability")
            true
        }.getOrDefault(false)

        val hasGms = runCatching {
            Class.forName("com.google.android.gms.common.GoogleApiAvailability")
            true
        }.getOrDefault(false)

        return when {
            hasHms && isHuawei -> PushPlatform.HUAWEI_HMS
            hasGms -> PushPlatform.GOOGLE_FCM
            else -> PushPlatform.FALLBACK_POLLING
        }
    }

    fun showIncomingMessageNotification(
        conversationId: String,
        customerName: String,
        messageText: String,
        phoneNumber: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("conversation_id", conversationId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            conversationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, CHANNEL_MESSAGES_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle(customerName.ifBlank { phoneNumber })
            .setContentText(messageText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setSound(defaultSound)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        runCatching {
            notificationManager.notify(NOTIFICATION_ID_BASE + conversationId.hashCode().coerceAtLeast(0) % 500, builder.build())
        }
    }

    fun cancelNotification(conversationId: String) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(NOTIFICATION_ID_BASE + conversationId.hashCode().coerceAtLeast(0) % 500)
    }
}

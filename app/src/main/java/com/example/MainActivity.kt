package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.data.local.CustomizationPreferences
import com.example.ui.navigation.AppNavHost
import com.example.ui.theme.WhatsAppBusinessInboxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val notificationConvId = intent?.getStringExtra("conversation_id")

        setContent {
            val customizationPrefs = remember { CustomizationPreferences(this) }
            val themeConfig by customizationPrefs.themeConfig.collectAsStateWithLifecycle()

            WhatsAppBusinessInboxTheme(
                themeMode = themeConfig.themeMode,
                colorPalette = themeConfig.colorPalette
            ) {
                val navController = rememberNavController()
                AppNavHost(
                    navController = navController,
                    initialConversationId = notificationConvId,
                    customizationPreferences = customizationPrefs
                )
            }
        }
    }
}

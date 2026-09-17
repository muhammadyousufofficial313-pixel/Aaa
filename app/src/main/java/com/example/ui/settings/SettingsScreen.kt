package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Palette
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.notification.PushPlatform
import com.example.ui.theme.WindowActiveGreen
import com.example.ui.viewmodel.SettingsUiEvent
import com.example.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToChatCustomization: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val business by viewModel.business.collectAsStateWithLifecycle()
    val serverUrl by viewModel.serverUrl.collectAsStateWithLifecycle()
    val isLiveServerMode by viewModel.isLiveServerMode.collectAsStateWithLifecycle()
    val isUrduLanguage by viewModel.isUrduLanguage.collectAsStateWithLifecycle()

    var metaBizId by remember(business?.metaBusinessAccountId) {
        mutableStateOf(business?.metaBusinessAccountId ?: "109823485721903")
    }
    var wabaId by remember(business?.wabaId) {
        mutableStateOf(business?.wabaId ?: "105948372615482")
    }
    var phoneId by remember(business?.phoneNumberId) {
        mutableStateOf(business?.phoneNumberId ?: "104827163829104")
    }
    var displayPhone by remember(business?.displayPhoneNumber) {
        mutableStateOf(business?.displayPhoneNumber ?: "+1 555-019-2834")
    }
    var verifyToken by remember(business?.webhookVerifyToken) {
        mutableStateOf(business?.webhookVerifyToken ?: "wa_biz_secure_verify_token_2026")
    }

    var serverUrlInput by remember(serverUrl) { mutableStateOf(serverUrl) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is SettingsUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "WhatsApp Platform Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "WhatsApp Cloud API Connection",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Configure your official Meta Business Account & Webhook credentials.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = metaBizId,
                            onValueChange = { metaBizId = it },
                            label = { Text("Meta Business Account ID") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_meta_biz_id")
                        )

                        OutlinedTextField(
                            value = wabaId,
                            onValueChange = { wabaId = it },
                            label = { Text("WhatsApp Business Account (WABA) ID") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_waba_id")
                        )

                        OutlinedTextField(
                            value = phoneId,
                            onValueChange = { phoneId = it },
                            label = { Text("Phone Number ID") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_phone_id")
                        )

                        OutlinedTextField(
                            value = displayPhone,
                            onValueChange = { displayPhone = it },
                            label = { Text("Display Phone Number") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_display_phone")
                        )

                        OutlinedTextField(
                            value = verifyToken,
                            onValueChange = { verifyToken = it },
                            label = { Text("Webhook Verify Token (Secret)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_verify_token")
                        )

                        // Webhook URL display
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "META WEBHOOK CALLBACK URL:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$serverUrl/webhooks/whatsapp",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.connectWhatsApp(
                                    metaBusinessAccountId = metaBizId,
                                    wabaId = wabaId,
                                    phoneNumberId = phoneId,
                                    webhookVerifyToken = verifyToken,
                                    displayPhone = displayPhone
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("btn_save_whatsapp_credentials")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Verify & Save Connection")
                        }
                    }
                }
            }

            // Chat Appearance & Themes
            item {
                Text(
                    text = "Appearance & Personalization",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToChatCustomization() }
                        .testTag("card_settings_chat_appearance"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Chat Themes & Wallpaper",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Customize bubble colors, wallpapers, font size, and themes",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Notification Engine Diagnostics (Huawei Push Kit / FCM)
            item {
                Text(
                    text = "Notification Infrastructure",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Unified Push Engine",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = when (viewModel.pushPlatform) {
                                        PushPlatform.HUAWEI_HMS -> "Huawei Push Kit (HMS) Active"
                                        PushPlatform.GOOGLE_FCM -> "Firebase Cloud Messaging (GMS) Active"
                                        PushPlatform.FALLBACK_POLLING -> "Adaptive Fallback Notification Engine Active"
                                    },
                                    fontSize = 12.sp,
                                    color = WindowActiveGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Ensures zero-delay alert delivery without requiring Google Play Services as a mandatory dependency. Seamlessly functions across Huawei EMUI/HarmonyOS and GMS devices.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Backend Server Configuration
            item {
                Text(
                    text = "Backend Server & Sandbox Relay",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Live Server API Mode",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (isLiveServerMode) "Connected to external backend server" else "Using local high-fidelity sandbox engine",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isLiveServerMode,
                                onCheckedChange = { viewModel.setLiveServerMode(it) },
                                modifier = Modifier.testTag("switch_server_mode")
                            )
                        }

                        if (isLiveServerMode) {
                            OutlinedTextField(
                                value = serverUrlInput,
                                onValueChange = {
                                    serverUrlInput = it
                                    viewModel.setServerUrl(it)
                                },
                                label = { Text("Production Server URL") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("input_server_url")
                            )
                        }
                    }
                }
            }

            // Localization
            item {
                Text(
                    text = "Language & Localization",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isUrduLanguage) "Urdu Interface (اردو)" else "English Interface (LTR)",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (isUrduLanguage) "دائیں سے بائیں (RTL) موڈ فعال ہے" else "Standard business LTR layout",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = isUrduLanguage,
                            onCheckedChange = { viewModel.toggleLanguage(it) },
                            modifier = Modifier.testTag("switch_language")
                        )
                    }
                }
            }

            // Logout
            item {
                OutlinedButton(
                    onClick = onLogout,
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("btn_logout")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out of Unified Inbox")
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

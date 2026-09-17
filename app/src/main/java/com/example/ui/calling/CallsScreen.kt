package com.example.ui.calling

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.AddIcCall
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.PhoneMissed
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.CallLogEntity
import com.example.domain.model.CallDirection
import com.example.domain.model.CallStatus
import com.example.domain.model.CallType
import com.example.ui.viewmodel.CallFilter
import com.example.ui.viewmodel.CallsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallsScreen(
    viewModel: CallsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val callLogs by viewModel.filteredCallLogs.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()

    var showSimulateDialog by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }

    if (showSimulateDialog) {
        AlertDialog(
            onDismissRequest = { showSimulateDialog = false },
            title = { Text("Simulate Incoming Call", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Receive a simulated incoming call from a customer to test ringtone, audio/video alerts, and answering logic without external services."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSimulateDialog = false
                        viewModel.simulateIncomingCall(
                            conversationId = "conv_1",
                            customerId = "cust_1",
                            customerName = "Ahmad Al-Mansoor",
                            customerPhone = "+971 50 123 4567",
                            callType = CallType.AUDIO
                        )
                    },
                    modifier = Modifier.testTag("btn_confirm_simulate_audio")
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Audio Call")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showSimulateDialog = false
                        viewModel.simulateIncomingCall(
                            conversationId = "conv_2",
                            customerId = "cust_2",
                            customerName = "Fatima Textile Trading",
                            customerPhone = "+92 300 9876543",
                            callType = CallType.VIDEO
                        )
                    },
                    modifier = Modifier.testTag("btn_confirm_simulate_video")
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Video Call")
                }
            }
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear Call History?") },
            text = { Text("This will permanently remove all call logs from your local database.") },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirm = false
                        viewModel.clearAllCallLogs()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Calls & History", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("btn_calls_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSimulateDialog = true },
                        modifier = Modifier.testTag("btn_calls_simulate_incoming")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddIcCall,
                            contentDescription = "Simulate Incoming Call",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (callLogs.isNotEmpty()) {
                        IconButton(onClick = { showClearConfirm = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear Call History",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
        ) {
            // Filter chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == CallFilter.ALL,
                        onClick = { viewModel.setFilter(CallFilter.ALL) },
                        label = { Text("All (${callLogs.size})") },
                        colors = FilterChipDefaults.filterChipColors()
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == CallFilter.MISSED,
                        onClick = { viewModel.setFilter(CallFilter.MISSED) },
                        label = { Text("Missed") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == CallFilter.AUDIO,
                        onClick = { viewModel.setFilter(CallFilter.AUDIO) },
                        label = { Text("Audio Calls") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == CallFilter.VIDEO,
                        onClick = { viewModel.setFilter(CallFilter.VIDEO) },
                        label = { Text("Video Calls") }
                    )
                }
            }

            if (callLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneInTalk,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Calls Yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Start audio or video calls directly with customers from any chat thread, or tap the button above to simulate an incoming call.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(callLogs, key = { it.id }) { callLog ->
                        CallLogCard(
                            log = callLog,
                            onCallBackAudio = {
                                viewModel.startCall(
                                    conversationId = callLog.conversationId,
                                    customerId = callLog.customerId,
                                    customerName = callLog.customerName,
                                    customerPhone = callLog.customerPhone,
                                    callType = CallType.AUDIO
                                )
                            },
                            onCallBackVideo = {
                                viewModel.startCall(
                                    conversationId = callLog.conversationId,
                                    customerId = callLog.customerId,
                                    customerName = callLog.customerName,
                                    customerPhone = callLog.customerPhone,
                                    callType = CallType.VIDEO
                                )
                            },
                            onDelete = { viewModel.deleteCallLog(callLog.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CallLogCard(
    log: CallLogEntity,
    onCallBackAudio: () -> Unit,
    onCallBackVideo: () -> Unit,
    onDelete: () -> Unit
) {
    val isMissed = log.status == CallStatus.MISSED || log.status == CallStatus.REJECTED
    val isVideo = log.callType == CallType.VIDEO

    val timeFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }
    val formattedTime = remember(log.timestamp) { timeFormat.format(Date(log.timestamp)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("item_call_log_${log.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Initials
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (isMissed) Color(0xFFFEE2E2) else MaterialTheme.colorScheme.primaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = log.customerName.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = if (isMissed) Color(0xFFEF4444) else MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.customerName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Direction / Missed Icon
                    val icon = when {
                        isMissed -> Icons.AutoMirrored.Filled.CallMissed
                        log.direction == CallDirection.OUTGOING -> Icons.AutoMirrored.Filled.CallMade
                        else -> Icons.AutoMirrored.Filled.CallReceived
                    }
                    val iconTint = if (isMissed) Color(0xFFEF4444) else Color(0xFF10B981)

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(14.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    val durText = if (isMissed) {
                        "Missed"
                    } else {
                        val mins = log.durationSeconds / 60
                        val secs = log.durationSeconds % 60
                        if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
                    }

                    Text(
                        text = "${if (isVideo) "Video" else "Audio"} • $durText",
                        fontSize = 12.sp,
                        color = if (isMissed) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = " • $formattedTime",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // Quick Call Back Icon
            IconButton(
                onClick = if (isVideo) onCallBackVideo else onCallBackAudio,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = if (isVideo) Icons.Default.Videocam else Icons.Default.Call,
                    contentDescription = "Call Back",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

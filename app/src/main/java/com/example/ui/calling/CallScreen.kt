package com.example.ui.calling

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.calling.CallManager
import com.example.domain.model.ActiveCall
import com.example.domain.model.CallDirection
import com.example.domain.model.CallStatus
import com.example.domain.model.CallType

@Composable
fun CallScreen(
    callManager: CallManager,
    modifier: Modifier = Modifier
) {
    val activeCall by callManager.activeCall.collectAsStateWithLifecycle()
    activeCall?.let { call ->
        CallScreen(
            call = call,
            onEndCall = callManager::endCall,
            onToggleMute = callManager::toggleMute,
            onToggleSpeaker = callManager::toggleSpeaker,
            onToggleVideo = callManager::toggleVideo,
            onSwitchCamera = callManager::switchCamera,
            onMinimize = callManager::minimizeCall,
            modifier = modifier
        )
    }
}

@Composable
fun CallScreen(
    call: ActiveCall,
    onEndCall: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleVideo: () -> Unit,
    onSwitchCamera: () -> Unit,
    onMinimize: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Deep Midnight Navy
            .testTag("screen_active_call")
    ) {
        if (call.callType == CallType.VIDEO) {
            VideoCallContent(
                call = call,
                onSwitchCamera = onSwitchCamera
            )
        } else {
            AudioCallContent(call = call)
        }

        // Top Header Overlay: Encryption badge & Minimize
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onMinimize,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .testTag("btn_call_minimize")
            ) {
                Icon(
                    imageVector = Icons.Default.CloseFullscreen,
                    contentDescription = "Minimize Call",
                    tint = Color.White
                )
            }

            // End-to-end encryption indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "End-to-end encrypted",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.size(42.dp))
        }

        // Bottom In-Call Control Bar
        CallControlBar(
            call = call,
            onEndCall = onEndCall,
            onToggleMute = onToggleMute,
            onToggleSpeaker = onToggleSpeaker,
            onToggleVideo = onToggleVideo,
            onSwitchCamera = onSwitchCamera,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        )
    }
}

@Composable
private fun AudioCallContent(call: ActiveCall) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        // Large Customer Profile Avatar with Pulsing Rings
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(200.dp)
        ) {
            if (call.status == CallStatus.RINGING) {
                Canvas(
                    modifier = Modifier
                        .size(190.dp)
                        .scale(pulseScale)
                ) {
                    drawCircle(
                        color = Color(0xFF059669).copy(alpha = 0.25f),
                        radius = size.minDimension / 2f
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF0F766E), Color(0xFF047857))
                        )
                    )
                    .border(3.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = call.customerName.take(1).uppercase(),
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Customer Name
        Text(
            text = call.customerName,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Customer Phone
        Text(
            text = call.customerPhone,
            color = Color.White.copy(alpha = 0.65f),
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Call Status & Live Timer
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.1f))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            if (call.status == CallStatus.CONNECTED) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981))
                )
                Spacer(modifier = Modifier.width(8.dp))
                val mins = call.durationSeconds / 60
                val secs = call.durationSeconds % 60
                Text(
                    text = "%02d:%02d".format(mins, secs),
                    color = Color(0xFF10B981),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Text(
                    text = when (call.status) {
                        CallStatus.RINGING -> if (call.direction == CallDirection.OUTGOING) "Ringing..." else "Incoming Audio Call"
                        CallStatus.CONNECTING -> "Connecting..."
                        CallStatus.CONNECTED -> "Connected"
                        CallStatus.ENDED -> "Call Ended"
                        CallStatus.MISSED -> "Missed Call"
                        CallStatus.REJECTED -> "Declined"
                    },
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(140.dp))
    }
}

@Composable
private fun VideoCallContent(
    call: ActiveCall,
    onSwitchCamera: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Remote Participant Video Feed
        if (call.status == CallStatus.CONNECTED) {
            // Simulated High Definition Remote Customer Video Feed
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F766E).copy(alpha = 0.4f))
                            .border(2.dp, Color(0xFF10B981), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = call.customerName.take(1).uppercase(),
                            color = Color.White,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${call.customerName} (Client Video)",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "HD 1080p • 60fps • Encrypted Stream",
                        color = Color(0xFF10B981),
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            // Outgoing / Incoming Video Ringing State
            AudioCallContent(call = call)
        }

        // Local Caller Self-Video Preview (PIP Box in upper right corner)
        if (call.status == CallStatus.CONNECTED && call.isVideoEnabled) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 56.dp, end = 16.dp)
                    .size(width = 110.dp, height = 150.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF334155))
                    .border(2.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (call.isFrontCamera) "Front Camera" else "Rear Camera",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF047857)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "You",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Flip camera button
                    IconButton(
                        onClick = onSwitchCamera,
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Switch Camera",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Live Duration Badge for Video Call
        if (call.status == CallStatus.CONNECTED) {
            val mins = call.durationSeconds / 60
            val secs = call.durationSeconds % 60
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 18.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "%02d:%02d".format(mins, secs),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun CallControlBar(
    call: ActiveCall,
    onEndCall: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleVideo: () -> Unit,
    onSwitchCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFF1E293B).copy(alpha = 0.92f),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mute Microphone Toggle
            IconButton(
                onClick = onToggleMute,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(if (call.isMuted) Color.White else Color.White.copy(alpha = 0.15f))
                    .testTag("btn_call_mute")
            ) {
                Icon(
                    imageVector = if (call.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = if (call.isMuted) "Unmute Microphone" else "Mute Microphone",
                    tint = if (call.isMuted) Color(0xFF0F172A) else Color.White
                )
            }

            // Loudspeaker Toggle
            IconButton(
                onClick = onToggleSpeaker,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(if (call.isSpeakerOn) Color.White else Color.White.copy(alpha = 0.15f))
                    .testTag("btn_call_speaker")
            ) {
                Icon(
                    imageVector = if (call.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    contentDescription = if (call.isSpeakerOn) "Speaker On" else "Speaker Off",
                    tint = if (call.isSpeakerOn) Color(0xFF0F172A) else Color.White
                )
            }

            // Video Camera Toggle
            IconButton(
                onClick = onToggleVideo,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(if (call.isVideoEnabled) Color.White else Color.White.copy(alpha = 0.15f))
                    .testTag("btn_call_video_toggle")
            ) {
                Icon(
                    imageVector = if (call.isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                    contentDescription = if (call.isVideoEnabled) "Turn Video Off" else "Turn Video On",
                    tint = if (call.isVideoEnabled) Color(0xFF0F172A) else Color.White
                )
            }

            // Switch Camera (only for video calls)
            if (call.callType == CallType.VIDEO) {
                IconButton(
                    onClick = onSwitchCamera,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .testTag("btn_call_switch_camera")
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Switch Camera",
                        tint = Color.White
                    )
                }
            }

            // Big Red End Call Button
            IconButton(
                onClick = onEndCall,
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444))
                    .testTag("btn_call_end")
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

package com.example.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhoneMissed
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.local.MediaStorageHelper
import com.example.data.local.entities.MessageEntity
import com.example.domain.model.ChatThemeConfig
import com.example.domain.model.MessageStatus
import com.example.domain.model.MessageType
import com.example.domain.model.SenderType
import com.example.ui.theme.StatusFailedRed
import com.example.ui.theme.StatusReadCyan
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageBubble(
    message: MessageEntity,
    onReplyClick: (MessageEntity) -> Unit,
    modifier: Modifier = Modifier,
    audioPlayerManager: AudioPlayerManager? = null,
    onImageClick: ((MessageEntity) -> Unit)? = null,
    onVideoClick: ((MessageEntity) -> Unit)? = null,
    onDownloadMedia: ((MessageEntity) -> Unit)? = null,
    onCallBack: ((Boolean) -> Unit)? = null,
    themeConfig: ChatThemeConfig? = null
) {
    val isOutgoing = message.senderType == SenderType.BUSINESS
    val fontScale = themeConfig?.fontSize?.scaleFactor ?: 1.0f

    val bubbleShape = if (isOutgoing) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }

    val bubbleBg = if (isOutgoing) {
        if (themeConfig != null) Color(themeConfig.outgoingBubbleColor)
        else MaterialTheme.colorScheme.primary
    } else {
        if (themeConfig != null) Color(themeConfig.incomingBubbleColor)
        else MaterialTheme.colorScheme.surfaceVariant
    }

    val isLightBg = bubbleBg.luminance() > 0.5f

    val contentColor = if (isLightBg) {
        Color(0xFF0F172A)
    } else {
        Color.White
    }

    val timeColor = if (isLightBg) {
        Color(0xFF475569)
    } else {
        Color.White.copy(alpha = 0.75f)
    }

    val timeFormatted = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp))

    val playbackState by audioPlayerManager?.playbackState?.collectAsStateWithLifecycle()
        ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(AudioPlaybackState()) }

    val isThisAudioPlaying = playbackState.activeMessageId == message.id && playbackState.isPlaying
    val currentProgress = if (playbackState.activeMessageId == message.id) playbackState.progress else 0f
    val currentPosMs = if (playbackState.activeMessageId == message.id) playbackState.currentPositionMs else 0L

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = if (isOutgoing) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(bubbleShape)
                .background(bubbleBg)
                .clickable { onReplyClick(message) }
                .padding(10.dp)
                .testTag("message_bubble_${message.id}")
        ) {
            Column {
                // Reply Context (if present)
                if (message.replyToMessageId != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isLightBg) Color.Black.copy(alpha = 0.08f)
                                else Color.White.copy(alpha = 0.15f)
                            )
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Reply,
                            contentDescription = null,
                            tint = contentColor.copy(alpha = 0.8f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Replying to message",
                            fontSize = (11 * fontScale).sp,
                            fontWeight = FontWeight.Medium,
                            color = contentColor.copy(alpha = 0.9f)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Media Attachment Content
                when (message.messageType) {
                    // --- IMAGE ---
                    MessageType.IMAGE -> {
                        if (!message.mediaUrl.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onImageClick?.invoke(message) }
                                    .testTag("img_message_${message.id}")
                            ) {
                                AsyncImage(
                                    model = message.mediaUrl,
                                    contentDescription = "WhatsApp Image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Download button overlay
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.5f))
                                        .clickable { onDownloadMedia?.invoke(message) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = "Save Image",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    // --- VIDEO ---
                    MessageType.VIDEO -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onVideoClick?.invoke(message) }
                                .testTag("video_message_${message.id}")
                        ) {
                            // Poster Thumbnail
                            AsyncImage(
                                model = message.thumbnailUrl ?: message.mediaUrl
                                ?: "https://images.unsplash.com/photo-1536240478700-b869070f9279?w=600",
                                contentDescription = "WhatsApp Video",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Semi-transparent dark gradient overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.35f))
                            )

                            // Play Button in center
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.65f))
                                    .align(Alignment.Center),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play Video",
                                    tint = Color.White,
                                    modifier = Modifier.size(34.dp)
                                )
                            }

                            // Top Info Pill: Video Duration & Size
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${MediaStorageHelper.formatDuration(message.mediaDurationMs ?: 32_000L)} • ${MediaStorageHelper.formatFileSize(message.mediaSize ?: 4_200_000L)}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Download icon button
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .clickable { onDownloadMedia?.invoke(message) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Save Video",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // --- VOICE MESSAGE ---
                    MessageType.VOICE -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isLightBg) Color.Black.copy(alpha = 0.07f)
                                    else Color.White.copy(alpha = 0.15f)
                                )
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Play / Pause round action button
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(contentColor.copy(alpha = 0.15f))
                                    .clickable {
                                        val dur = message.mediaDurationMs ?: 14_000L
                                        audioPlayerManager?.playOrPause(message.id, message.mediaUrl, dur)
                                    }
                                    .testTag("btn_voice_play_${message.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (message.status == MessageStatus.PENDING) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = contentColor,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (isThisAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isThisAudioPlaying) "Pause voice note" else "Play voice note",
                                        tint = contentColor,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                // Progress Slider / Bar
                                Slider(
                                    value = currentProgress,
                                    onValueChange = { frac ->
                                        audioPlayerManager?.seekTo(frac)
                                    },
                                    colors = SliderDefaults.colors(
                                        thumbColor = contentColor,
                                        activeTrackColor = contentColor,
                                        inactiveTrackColor = contentColor.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier.height(24.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val totalDuration = message.mediaDurationMs ?: 14_000L
                                    val displayTime = if (isThisAudioPlaying) currentPosMs else totalDuration
                                    Text(
                                        text = MediaStorageHelper.formatDuration(displayTime),
                                        fontSize = 11.sp,
                                        color = timeColor
                                    )

                                    // Playback Speed button
                                    if (isThisAudioPlaying) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(contentColor.copy(alpha = 0.2f))
                                                .clickable { audioPlayerManager?.togglePlaybackSpeed() }
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "${playbackState.playbackSpeed}x",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = contentColor
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = null,
                                            tint = timeColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // --- AUDIO / MUSIC FILE ---
                    MessageType.AUDIO -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isLightBg) Color.Black.copy(alpha = 0.07f)
                                    else Color.White.copy(alpha = 0.15f)
                                )
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(contentColor.copy(alpha = 0.15f))
                                    .clickable {
                                        val dur = message.mediaDurationMs ?: 180_000L
                                        audioPlayerManager?.playOrPause(message.id, message.mediaUrl, dur)
                                    }
                                    .testTag("btn_audio_play_${message.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isThisAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isThisAudioPlaying) "Pause" else "Play",
                                    tint = contentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = message.mediaFileName ?: "Audio_File.mp3",
                                    fontSize = (13 * fontScale).sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = contentColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Slider(
                                    value = currentProgress,
                                    onValueChange = { frac ->
                                        audioPlayerManager?.seekTo(frac)
                                    },
                                    colors = SliderDefaults.colors(
                                        thumbColor = contentColor,
                                        activeTrackColor = contentColor,
                                        inactiveTrackColor = contentColor.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier.height(20.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val dur = message.mediaDurationMs ?: 180_000L
                                    val sizeStr = MediaStorageHelper.formatFileSize(message.mediaSize ?: 3_400_000L)
                                    Text(
                                        text = "${MediaStorageHelper.formatDuration(if (isThisAudioPlaying) currentPosMs else dur)} • $sizeStr",
                                        fontSize = 10.sp,
                                        color = timeColor
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isThisAudioPlaying) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(contentColor.copy(alpha = 0.2f))
                                                    .clickable { audioPlayerManager?.togglePlaybackSpeed() }
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "${playbackState.playbackSpeed}x",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = contentColor
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }

                                        Icon(
                                            imageVector = Icons.Default.Download,
                                            contentDescription = "Save Audio",
                                            tint = timeColor,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable { onDownloadMedia?.invoke(message) }
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // --- DOCUMENT ---
                    MessageType.DOCUMENT -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isLightBg) Color.Black.copy(alpha = 0.08f)
                                    else Color.White.copy(alpha = 0.15f)
                                )
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = contentColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = message.mediaFileName ?: "Document.pdf",
                                    fontSize = (13 * fontScale).sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = contentColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "PDF Document • ${MediaStorageHelper.formatFileSize(message.mediaSize ?: 850_000L)}",
                                    fontSize = 10.sp,
                                    color = timeColor
                                )
                            }
                            IconButton(onClick = { onDownloadMedia?.invoke(message) }) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Download Document",
                                    tint = contentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // --- TEMPLATE ---
                    MessageType.TEMPLATE -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isLightBg) Color.Black.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "META TEMPLATE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = contentColor
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // --- AUDIO & VIDEO CALL EVENTS ---
                    MessageType.AUDIO_CALL, MessageType.VIDEO_CALL -> {
                        val isVideo = message.messageType == MessageType.VIDEO_CALL
                        val isMissed = message.content.contains("Missed", ignoreCase = true) || message.content.contains("Declined", ignoreCase = true)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isMissed) Color(0xFFEF4444).copy(alpha = 0.2f) else if (isLightBg) Color.Black.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isVideo) Icons.Default.Videocam else if (isMissed) Icons.Default.PhoneMissed else Icons.Default.Call,
                                    contentDescription = null,
                                    tint = if (isMissed) Color(0xFFEF4444) else contentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isVideo) "WhatsApp Video Call" else "WhatsApp Audio Call",
                                    fontSize = (13 * fontScale).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = contentColor
                                )
                                Text(
                                    text = message.content,
                                    fontSize = (11 * fontScale).sp,
                                    color = if (isMissed) Color(0xFFEF4444) else contentColor.copy(alpha = 0.75f)
                                )
                            }
                            if (onCallBack != null) {
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { onCallBack(isVideo) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isVideo) Icons.Default.Videocam else Icons.Default.Call,
                                        contentDescription = "Call Back",
                                        tint = contentColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    MessageType.TEXT -> { /* Plain text */ }
                }

                // Text Content (suppressed for special media & call banners)
                if (message.content.isNotBlank() &&
                    message.messageType != MessageType.VOICE &&
                    message.messageType != MessageType.AUDIO_CALL &&
                    message.messageType != MessageType.VIDEO_CALL) {
                    Text(
                        text = message.content,
                        fontSize = (14 * fontScale).sp,
                        color = contentColor,
                        lineHeight = (19 * fontScale).sp
                    )
                }

                // Failure Reason (if failed)
                if (message.status == MessageStatus.FAILED && !message.failureReason.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message.failureReason,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = StatusFailedRed
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Footer: Timestamp & Delivery Status Icon
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeFormatted,
                        fontSize = 10.sp,
                        color = timeColor
                    )

                    if (isOutgoing) {
                        Spacer(modifier = Modifier.width(4.dp))
                        when (message.status) {
                            MessageStatus.PENDING -> {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = "Pending delivery",
                                    tint = timeColor,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            MessageStatus.SENT -> {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Sent to WhatsApp Cloud API",
                                    tint = timeColor,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            MessageStatus.DELIVERED -> {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Delivered to Customer WhatsApp",
                                    tint = timeColor,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            MessageStatus.READ -> {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Read by Customer",
                                    tint = StatusReadCyan,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            MessageStatus.FAILED -> {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = "Send Failed",
                                    tint = StatusFailedRed,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

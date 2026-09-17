package com.example.domain.model

enum class CallType {
    AUDIO,
    VIDEO
}

enum class CallDirection {
    INCOMING,
    OUTGOING
}

enum class CallStatus {
    RINGING,
    CONNECTING,
    CONNECTED,
    MISSED,
    REJECTED,
    ENDED
}

data class ActiveCall(
    val callId: String,
    val conversationId: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val callType: CallType,
    val direction: CallDirection,
    val status: CallStatus,
    val durationSeconds: Long = 0L,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isVideoEnabled: Boolean = true,
    val isFrontCamera: Boolean = true,
    val isMinimized: Boolean = false
)

data class CallLogItem(
    val id: String,
    val conversationId: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val callType: CallType,
    val direction: CallDirection,
    val status: CallStatus,
    val durationSeconds: Long,
    val timestamp: Long
)

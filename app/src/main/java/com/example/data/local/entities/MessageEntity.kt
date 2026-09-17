package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.model.MessageStatus
import com.example.domain.model.MessageType
import com.example.domain.model.SenderType

@Entity(
    tableName = "messages",
    indices = [
        Index(value = ["conversationId"]),
        Index(value = ["waMessageId"], unique = false),
        Index(value = ["timestamp"]),
        Index(value = ["status"])
    ]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val waMessageId: String? = null,
    val senderType: SenderType,
    val senderStaffId: String? = null,
    val messageType: MessageType = MessageType.TEXT,
    val content: String,
    val mediaUrl: String? = null,
    val mediaMimeType: String? = null,
    val mediaFileName: String? = null,
    val mediaSize: Long? = null,
    val mediaDurationMs: Long? = null,
    val thumbnailUrl: String? = null,
    val uploadProgress: Float = 1.0f,
    val status: MessageStatus = MessageStatus.SENT,
    val failureReason: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val replyToMessageId: String? = null
)

package com.example.data.remote.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ConversationDto(
    @Json(name = "id") val id: String,
    @Json(name = "customerId") val customerId: String,
    @Json(name = "customerName") val customerName: String,
    @Json(name = "customerPhone") val customerPhone: String,
    @Json(name = "customerAvatar") val customerAvatar: String?,
    @Json(name = "lastMessageText") val lastMessageText: String,
    @Json(name = "lastMessageTimestamp") val lastMessageTimestamp: Long,
    @Json(name = "unreadCount") val unreadCount: Int,
    @Json(name = "windowExpiresAt") val windowExpiresAt: Long,
    @Json(name = "isImportant") val isImportant: Boolean,
    @Json(name = "isArchived") val isArchived: Boolean,
    @Json(name = "tags") val tags: List<String> = emptyList(),
    @Json(name = "assignedStaffId") val assignedStaffId: String?
)

@JsonClass(generateAdapter = true)
data class MessageDto(
    @Json(name = "id") val id: String,
    @Json(name = "conversationId") val conversationId: String,
    @Json(name = "waMessageId") val waMessageId: String?,
    @Json(name = "senderType") val senderType: String,
    @Json(name = "messageType") val messageType: String,
    @Json(name = "content") val content: String,
    @Json(name = "mediaUrl") val mediaUrl: String?,
    @Json(name = "status") val status: String,
    @Json(name = "failureReason") val failureReason: String?,
    @Json(name = "timestamp") val timestamp: Long
)

@JsonClass(generateAdapter = true)
data class TemplateDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "language") val language: String,
    @Json(name = "category") val category: String,
    @Json(name = "status") val status: String,
    @Json(name = "headerText") val headerText: String?,
    @Json(name = "bodyText") val bodyText: String,
    @Json(name = "footerText") val footerText: String?
)

@JsonClass(generateAdapter = true)
data class DashboardAnalyticsDto(
    @Json(name = "totalConversations") val totalConversations: Int,
    @Json(name = "unreadMessages") val unreadMessages: Int,
    @Json(name = "activeWindowConversations") val activeWindowConversations: Int,
    @Json(name = "messagesSent") val messagesSent: Int,
    @Json(name = "messagesReceived") val messagesReceived: Int,
    @Json(name = "averageResponseTimeMinutes") val averageResponseTimeMinutes: Double
)

@JsonClass(generateAdapter = true)
data class MediaUploadResponse(
    @Json(name = "mediaId") val mediaId: String,
    @Json(name = "mediaUrl") val mediaUrl: String,
    @Json(name = "fileSize") val fileSize: Long
)

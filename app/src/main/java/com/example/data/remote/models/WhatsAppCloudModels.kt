package com.example.data.remote.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SendTextMessageRequest(
    @Json(name = "conversationId") val conversationId: String,
    @Json(name = "recipientPhone") val recipientPhone: String,
    @Json(name = "message") val message: String,
    @Json(name = "replyToMessageId") val replyToMessageId: String? = null
)

@JsonClass(generateAdapter = true)
data class SendMediaMessageRequest(
    @Json(name = "conversationId") val conversationId: String,
    @Json(name = "recipientPhone") val recipientPhone: String,
    @Json(name = "mediaType") val mediaType: String, // image, document, audio
    @Json(name = "mediaUrl") val mediaUrl: String,
    @Json(name = "caption") val caption: String? = null,
    @Json(name = "fileName") val fileName: String? = null
)

@JsonClass(generateAdapter = true)
data class SendTemplateMessageRequest(
    @Json(name = "conversationId") val conversationId: String,
    @Json(name = "recipientPhone") val recipientPhone: String,
    @Json(name = "templateName") val templateName: String,
    @Json(name = "languageCode") val languageCode: String = "en_US",
    @Json(name = "parameters") val parameters: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class WhatsAppSendMessageResponse(
    @Json(name = "messageId") val messageId: String,
    @Json(name = "waMessageId") val waMessageId: String,
    @Json(name = "status") val status: String,
    @Json(name = "timestamp") val timestamp: Long
)

@JsonClass(generateAdapter = true)
data class WhatsAppAccountStatusResponse(
    @Json(name = "connected") val connected: Boolean,
    @Json(name = "metaBusinessAccountId") val metaBusinessAccountId: String?,
    @Json(name = "wabaId") val wabaId: String?,
    @Json(name = "phoneNumberId") val phoneNumberId: String?,
    @Json(name = "displayPhoneNumber") val displayPhoneNumber: String?,
    @Json(name = "qualityRating") val qualityRating: String?,
    @Json(name = "webhookCallbackUrl") val webhookCallbackUrl: String?
)

@JsonClass(generateAdapter = true)
data class ConnectAccountRequest(
    @Json(name = "metaBusinessAccountId") val metaBusinessAccountId: String,
    @Json(name = "wabaId") val wabaId: String,
    @Json(name = "phoneNumberId") val phoneNumberId: String,
    @Json(name = "webhookVerifyToken") val webhookVerifyToken: String,
    @Json(name = "displayPhoneNumber") val displayPhoneNumber: String
)

@JsonClass(generateAdapter = true)
data class ConnectAccountResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String,
    @Json(name = "webhookCallbackUrl") val webhookCallbackUrl: String
)

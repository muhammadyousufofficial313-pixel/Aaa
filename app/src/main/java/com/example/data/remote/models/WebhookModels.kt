package com.example.data.remote.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Official Meta Webhook Notification Schema for WhatsApp Business Platform / Cloud API
 */
@JsonClass(generateAdapter = true)
data class WhatsAppWebhookPayload(
    @Json(name = "object") val objectType: String = "whatsapp_business_account",
    @Json(name = "entry") val entry: List<WebhookEntry> = emptyList()
)

@JsonClass(generateAdapter = true)
data class WebhookEntry(
    @Json(name = "id") val id: String, // WABA ID
    @Json(name = "changes") val changes: List<WebhookChange> = emptyList()
)

@JsonClass(generateAdapter = true)
data class WebhookChange(
    @Json(name = "field") val field: String = "messages",
    @Json(name = "value") val value: WebhookValue
)

@JsonClass(generateAdapter = true)
data class WebhookValue(
    @Json(name = "messaging_product") val messagingProduct: String = "whatsapp",
    @Json(name = "metadata") val metadata: WebhookMetadata? = null,
    @Json(name = "contacts") val contacts: List<WebhookContact>? = null,
    @Json(name = "messages") val messages: List<WebhookIncomingMessage>? = null,
    @Json(name = "statuses") val statuses: List<WebhookMessageStatusUpdate>? = null
)

@JsonClass(generateAdapter = true)
data class WebhookMetadata(
    @Json(name = "display_phone_number") val displayPhoneNumber: String?,
    @Json(name = "phone_number_id") val phoneNumberId: String?
)

@JsonClass(generateAdapter = true)
data class WebhookContact(
    @Json(name = "profile") val profile: WebhookProfile?,
    @Json(name = "wa_id") val waId: String // e.g. "15559876543"
)

@JsonClass(generateAdapter = true)
data class WebhookProfile(
    @Json(name = "name") val name: String?
)

@JsonClass(generateAdapter = true)
data class WebhookIncomingMessage(
    @Json(name = "from") val from: String, // e.g. "15559876543"
    @Json(name = "id") val id: String, // e.g. "wamid.HBg..."
    @Json(name = "timestamp") val timestamp: String, // epoch seconds
    @Json(name = "type") val type: String, // text, image, document, audio, reaction
    @Json(name = "text") val text: WebhookTextMessage? = null,
    @Json(name = "image") val image: WebhookMediaMessage? = null,
    @Json(name = "document") val document: WebhookDocumentMessage? = null,
    @Json(name = "audio") val audio: WebhookMediaMessage? = null,
    @Json(name = "context") val context: WebhookContext? = null
)

@JsonClass(generateAdapter = true)
data class WebhookTextMessage(
    @Json(name = "body") val body: String
)

@JsonClass(generateAdapter = true)
data class WebhookMediaMessage(
    @Json(name = "id") val id: String,
    @Json(name = "mime_type") val mimeType: String?,
    @Json(name = "sha256") val sha256: String?,
    @Json(name = "caption") val caption: String?
)

@JsonClass(generateAdapter = true)
data class WebhookDocumentMessage(
    @Json(name = "id") val id: String,
    @Json(name = "filename") val filename: String?,
    @Json(name = "mime_type") val mimeType: String?,
    @Json(name = "sha256") val sha256: String?,
    @Json(name = "caption") val caption: String?
)

@JsonClass(generateAdapter = true)
data class WebhookContext(
    @Json(name = "from") val from: String?,
    @Json(name = "id") val id: String? // Parent wamid replied to
)

@JsonClass(generateAdapter = true)
data class WebhookMessageStatusUpdate(
    @Json(name = "id") val id: String, // wamid of the outgoing message
    @Json(name = "status") val status: String, // sent, delivered, read, failed
    @Json(name = "timestamp") val timestamp: String,
    @Json(name = "recipient_id") val recipientId: String,
    @Json(name = "errors") val errors: List<WebhookErrorDetail>? = null
)

@JsonClass(generateAdapter = true)
data class WebhookErrorDetail(
    @Json(name = "code") val code: Int,
    @Json(name = "title") val title: String,
    @Json(name = "message") val message: String? = null
)

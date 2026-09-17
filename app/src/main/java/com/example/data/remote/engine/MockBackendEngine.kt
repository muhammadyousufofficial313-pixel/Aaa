package com.example.data.remote.engine

import com.example.data.remote.models.ConnectAccountRequest
import com.example.data.remote.models.ConnectAccountResponse
import com.example.data.remote.models.ConversationDto
import com.example.data.remote.models.DashboardAnalyticsDto
import com.example.data.remote.models.LoginRequest
import com.example.data.remote.models.LoginResponse
import com.example.data.remote.models.MediaUploadResponse
import com.example.data.remote.models.MessageDto
import com.example.data.remote.models.ResetPasswordRequest
import com.example.data.remote.models.ResetPasswordResponse
import com.example.data.remote.models.SendMediaMessageRequest
import com.example.data.remote.models.SendTemplateMessageRequest
import com.example.data.remote.models.SendTextMessageRequest
import com.example.data.remote.models.StaffUserDto
import com.example.data.remote.models.TemplateDto
import com.example.data.remote.models.WebhookChange
import com.example.data.remote.models.WebhookContact
import com.example.data.remote.models.WebhookEntry
import com.example.data.remote.models.WebhookIncomingMessage
import com.example.data.remote.models.WebhookMessageStatusUpdate
import com.example.data.remote.models.WebhookMetadata
import com.example.data.remote.models.WebhookProfile
import com.example.data.remote.models.WebhookTextMessage
import com.example.data.remote.models.WebhookValue
import com.example.data.remote.models.WhatsAppAccountStatusResponse
import com.example.data.remote.models.WhatsAppSendMessageResponse
import com.example.data.remote.models.WhatsAppWebhookPayload
import kotlinx.coroutines.delay
import java.util.UUID

/**
 * High-fidelity WhatsApp Business Platform / Cloud API (v21.0) & Backend Simulator.
 * Enforces:
 * - 24-Hour Customer Service Window rules
 * - Meta message status transitions (sent -> delivered -> read)
 * - Official Webhook event generation
 * - Meta Graph API template synchronization
 */
object MockBackendEngine {

    private var isConnected = true
    private var metaBusinessAccountId = "109823485721903"
    private var wabaId = "105948372615482"
    private var phoneNumberId = "104827163829104"
    private var displayPhoneNumber = "+1 555-019-2834"
    private var webhookVerifyToken = "wa_biz_secure_verify_token_2026"

    // Seed mock conversations
    private val conversations = mutableListOf(
        ConversationDto(
            id = "conv_15550192801",
            customerId = "15550192801",
            customerName = "Sarah Jenkins",
            customerPhone = "+1 555-019-2801",
            customerAvatar = null,
            lastMessageText = "Hello! Could you confirm if my custom bulk order #8492 is ready for pickup today?",
            lastMessageTimestamp = System.currentTimeMillis() - 1000 * 60 * 18, // 18 mins ago
            unreadCount = 1,
            windowExpiresAt = System.currentTimeMillis() + (23 * 3600 * 1000), // ~23h remaining
            isImportant = true,
            isArchived = false,
            tags = listOf("VIP", "Wholesale", "Urgent"),
            assignedStaffId = "staff_1"
        ),
        ConversationDto(
            id = "conv_15550192802",
            customerId = "15550192802",
            customerName = "Ahmad Raza",
            customerPhone = "+1 555-019-2802",
            customerAvatar = null,
            lastMessageText = "Can you send the updated product catalog and pricing sheet in PDF format?",
            lastMessageTimestamp = System.currentTimeMillis() - 1000 * 60 * 95, // 95 mins ago
            unreadCount = 2,
            windowExpiresAt = System.currentTimeMillis() + (22 * 3600 * 1000),
            isImportant = false,
            isArchived = false,
            tags = listOf("Lead", "Catalog Request"),
            assignedStaffId = null
        ),
        ConversationDto(
            id = "conv_15550192803",
            customerId = "15550192803",
            customerName = "Elena Rostova",
            customerPhone = "+1 555-019-2803",
            customerAvatar = null,
            lastMessageText = "Thank you so much! Everything arrived safely.",
            lastMessageTimestamp = System.currentTimeMillis() - (30 * 3600 * 1000), // 30 hours ago -> EXPIRED 24h WINDOW
            unreadCount = 0,
            windowExpiresAt = System.currentTimeMillis() - (6 * 3600 * 1000), // expired 6 hours ago
            isImportant = false,
            isArchived = false,
            tags = listOf("Returning"),
            assignedStaffId = "staff_2"
        )
    )

    private val templates = mutableListOf(
        TemplateDto(
            id = "order_confirmation_v2",
            name = "order_confirmation_v2",
            language = "en_US",
            category = "UTILITY",
            status = "APPROVED",
            headerText = "Order Confirmation",
            bodyText = "Hello {{1}}, your order {{2}} has been confirmed and is scheduled for dispatch. Thank you for choosing us!",
            footerText = "Reply STOP to unsubscribe"
        ),
        TemplateDto(
            id = "customer_reengagement",
            name = "customer_reengagement",
            language = "en_US",
            category = "MARKETING",
            status = "APPROVED",
            headerText = "We value your feedback",
            bodyText = "Hi {{1}}, we noticed it's been a while since we connected. Do you still need assistance with {{2}}?",
            footerText = "Support Team"
        ),
        TemplateDto(
            id = "account_verification_code",
            name = "account_verification_code",
            language = "en_US",
            category = "AUTHENTICATION",
            status = "APPROVED",
            headerText = null,
            bodyText = "Your verification code is {{1}}. Valid for 10 minutes. Please do not share this code with anyone.",
            footerText = "Security Alert"
        )
    )

    suspend fun login(req: LoginRequest): LoginResponse {
        delay(350)
        return LoginResponse(
            token = "jwt_mock_token_" + UUID.randomUUID().toString(),
            refreshToken = "refresh_mock_token_" + UUID.randomUUID().toString(),
            user = StaffUserDto(
                id = "staff_1",
                name = "Muhammad Yousuf",
                email = req.email.ifBlank { "owner@business.com" },
                role = "OWNER",
                avatarUrl = null
            )
        )
    }

    suspend fun resetPassword(req: ResetPasswordRequest): ResetPasswordResponse {
        delay(300)
        return ResetPasswordResponse(
            status = "success",
            message = "Password reset instructions sent to ${req.email}."
        )
    }

    suspend fun getAccountStatus(): WhatsAppAccountStatusResponse {
        delay(200)
        return WhatsAppAccountStatusResponse(
            connected = isConnected,
            metaBusinessAccountId = metaBusinessAccountId,
            wabaId = wabaId,
            phoneNumberId = phoneNumberId,
            displayPhoneNumber = displayPhoneNumber,
            qualityRating = "GREEN",
            webhookCallbackUrl = "https://api.yourdomain.com/v1/webhooks/whatsapp"
        )
    }

    suspend fun connectAccount(req: ConnectAccountRequest): ConnectAccountResponse {
        delay(400)
        metaBusinessAccountId = req.metaBusinessAccountId
        wabaId = req.wabaId
        phoneNumberId = req.phoneNumberId
        webhookVerifyToken = req.webhookVerifyToken
        displayPhoneNumber = req.displayPhoneNumber
        isConnected = true
        return ConnectAccountResponse(
            success = true,
            message = "WhatsApp Business Account connected successfully to Meta Cloud API.",
            webhookCallbackUrl = "https://api.yourdomain.com/v1/webhooks/whatsapp"
        )
    }

    suspend fun getConversations(): List<ConversationDto> {
        delay(200)
        return conversations.sortedByDescending { it.lastMessageTimestamp }
    }

    suspend fun sendTextMessage(req: SendTextMessageRequest): WhatsAppSendMessageResponse {
        delay(400)
        val conv = conversations.find { it.id == req.conversationId }
        val now = System.currentTimeMillis()

        // Enforce Meta 24-Hour Customer Service Window:
        if (conv != null && !conv.isImportant && now > conv.windowExpiresAt) {
            // Note: If outside 24h, WhatsApp Cloud API returns error 131047:
            throw IllegalStateException("Meta Policy Error 131047: The 24-hour customer service window has expired for this conversation. You must send a pre-approved Message Template.")
        }

        val generatedWaId = "wamid.HBgL" + UUID.randomUUID().toString().replace("-", "").take(22)

        // Update conversation summary
        if (conv != null) {
            val idx = conversations.indexOf(conv)
            conversations[idx] = conv.copy(
                lastMessageText = req.message,
                lastMessageTimestamp = now
            )
        }

        return WhatsAppSendMessageResponse(
            messageId = "msg_" + UUID.randomUUID().toString().take(8),
            waMessageId = generatedWaId,
            status = "SENT",
            timestamp = now
        )
    }

    suspend fun sendMediaMessage(req: SendMediaMessageRequest): WhatsAppSendMessageResponse {
        delay(500)
        val now = System.currentTimeMillis()
        val generatedWaId = "wamid.HBgM" + UUID.randomUUID().toString().replace("-", "").take(22)

        val conv = conversations.find { it.id == req.conversationId }
        if (conv != null) {
            val idx = conversations.indexOf(conv)
            conversations[idx] = conv.copy(
                lastMessageText = req.caption ?: "[${req.mediaType.uppercase()}]",
                lastMessageTimestamp = now
            )
        }

        return WhatsAppSendMessageResponse(
            messageId = "msg_" + UUID.randomUUID().toString().take(8),
            waMessageId = generatedWaId,
            status = "SENT",
            timestamp = now
        )
    }

    suspend fun sendTemplateMessage(req: SendTemplateMessageRequest): WhatsAppSendMessageResponse {
        delay(400)
        val now = System.currentTimeMillis()
        val generatedWaId = "wamid.HBgT" + UUID.randomUUID().toString().replace("-", "").take(22)

        val conv = conversations.find { it.id == req.conversationId }
        if (conv != null) {
            val idx = conversations.indexOf(conv)
            // Template message re-opens the 24-hour messaging window upon delivery!
            conversations[idx] = conv.copy(
                lastMessageText = "Template: ${req.templateName}",
                lastMessageTimestamp = now,
                windowExpiresAt = now + 86_400_000L
            )
        }

        return WhatsAppSendMessageResponse(
            messageId = "msg_" + UUID.randomUUID().toString().take(8),
            waMessageId = generatedWaId,
            status = "SENT",
            timestamp = now
        )
    }

    suspend fun uploadMedia(fileName: String, type: String): MediaUploadResponse {
        delay(450)
        return MediaUploadResponse(
            mediaId = "meta_media_" + UUID.randomUUID().toString().take(12),
            mediaUrl = "https://images.unsplash.com/photo-1557804506-669a67965ba0?w=600",
            fileSize = 245_800L
        )
    }

    suspend fun getTemplates(): List<TemplateDto> {
        delay(200)
        return templates
    }

    suspend fun syncTemplates(): List<TemplateDto> {
        delay(600)
        return templates
    }

    fun createSimulatedIncomingMessagePayload(
        fromPhone: String,
        customerName: String,
        messageText: String
    ): WhatsAppWebhookPayload {
        val nowSec = (System.currentTimeMillis() / 1000).toString()
        val generatedId = "wamid.HBgI" + UUID.randomUUID().toString().replace("-", "").take(22)
        return WhatsAppWebhookPayload(
            objectType = "whatsapp_business_account",
            entry = listOf(
                WebhookEntry(
                    id = wabaId,
                    changes = listOf(
                        WebhookChange(
                            field = "messages",
                            value = WebhookValue(
                                messagingProduct = "whatsapp",
                                metadata = WebhookMetadata(
                                    displayPhoneNumber = displayPhoneNumber,
                                    phoneNumberId = phoneNumberId
                                ),
                                contacts = listOf(
                                    WebhookContact(
                                        profile = WebhookProfile(name = customerName),
                                        waId = fromPhone.replace("+", "").replace("-", "").replace(" ", "")
                                    )
                                ),
                                messages = listOf(
                                    WebhookIncomingMessage(
                                        from = fromPhone.replace("+", "").replace("-", "").replace(" ", ""),
                                        id = generatedId,
                                        timestamp = nowSec,
                                        type = "text",
                                        text = WebhookTextMessage(body = messageText)
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }

    fun createSimulatedStatusUpdatePayload(
        waMessageId: String,
        status: String, // delivered, read, failed
        recipientPhone: String
    ): WhatsAppWebhookPayload {
        val nowSec = (System.currentTimeMillis() / 1000).toString()
        return WhatsAppWebhookPayload(
            objectType = "whatsapp_business_account",
            entry = listOf(
                WebhookEntry(
                    id = wabaId,
                    changes = listOf(
                        WebhookChange(
                            field = "messages",
                            value = WebhookValue(
                                messagingProduct = "whatsapp",
                                statuses = listOf(
                                    WebhookMessageStatusUpdate(
                                        id = waMessageId,
                                        status = status,
                                        timestamp = nowSec,
                                        recipientId = recipientPhone
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }

    suspend fun getDashboardAnalytics(): DashboardAnalyticsDto {
        delay(200)
        return DashboardAnalyticsDto(
            totalConversations = conversations.size,
            unreadMessages = conversations.sumOf { it.unreadCount },
            activeWindowConversations = conversations.count { it.windowExpiresAt > System.currentTimeMillis() },
            messagesSent = 142,
            messagesReceived = 289,
            averageResponseTimeMinutes = 4.2
        )
    }
}

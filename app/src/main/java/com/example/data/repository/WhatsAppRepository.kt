package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entities.AuditLogEntity
import com.example.data.local.entities.BusinessEntity
import com.example.data.local.entities.CallLogEntity
import com.example.data.local.entities.ConversationEntity
import com.example.data.local.entities.ConversationWithCustomer
import com.example.data.local.entities.CustomerEntity
import com.example.data.local.entities.MessageEntity
import com.example.data.local.entities.StaffEntity
import com.example.data.local.entities.TemplateEntity
import com.example.data.notification.UnifiedNotificationManager
import com.example.data.remote.engine.MockBackendEngine
import com.example.data.remote.models.ConnectAccountRequest
import com.example.data.remote.models.LoginRequest
import com.example.data.remote.models.ResetPasswordRequest
import com.example.data.remote.models.SendMediaMessageRequest
import com.example.data.remote.models.SendTemplateMessageRequest
import com.example.data.remote.models.SendTextMessageRequest
import com.example.data.remote.models.WhatsAppWebhookPayload
import com.example.domain.model.CallDirection
import com.example.domain.model.CallStatus
import com.example.domain.model.CallType
import com.example.domain.model.InboxFilter
import com.example.domain.model.MessageStatus
import com.example.domain.model.MessageType
import com.example.domain.model.SenderType
import com.example.domain.model.StaffRole
import com.example.domain.model.TemplateCategory
import com.example.domain.model.TemplateStatus
import com.example.domain.model.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class WhatsAppRepository(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getInstance(context),
    private val notificationManager: UnifiedNotificationManager = UnifiedNotificationManager(context)
) {
    private val dao = database.whatsAppDao()
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _sessionState = MutableStateFlow(
        UserSession(
            isAuthenticated = true, // default authenticated for seamless instant preview
            userId = "staff_1",
            name = "Muhammad Yousuf",
            email = "owner@business.com",
            role = StaffRole.OWNER,
            token = "jwt_session_token_sample",
            isConnectedToWhatsApp = true,
            displayPhoneNumber = "+1 555-019-2834"
        )
    )
    val sessionState: Flow<UserSession> = _sessionState.asStateFlow()

    private val _serverUrl = MutableStateFlow("https://api.yourdomain.com/v1")
    val serverUrl: Flow<String> = _serverUrl.asStateFlow()

    private val _isLiveServerMode = MutableStateFlow(false)
    val isLiveServerMode: Flow<Boolean> = _isLiveServerMode.asStateFlow()

    init {
        scope.launch {
            seedInitialDataIfEmpty()
        }
    }

    private suspend fun seedInitialDataIfEmpty() {
        val existingBiz = dao.getBusiness()
        if (existingBiz == null) {
            val biz = BusinessEntity(
                id = "biz_default",
                name = "Yousuf Enterprise Solutions",
                metaBusinessAccountId = "109823485721903",
                wabaId = "105948372615482",
                phoneNumberId = "104827163829104",
                displayPhoneNumber = "+1 555-019-2834",
                webhookVerifyToken = "wa_biz_secure_verify_token_2026",
                qualityRating = "GREEN",
                isConnected = true
            )
            dao.saveBusiness(biz)

            // Seed staff
            dao.insertStaff(
                StaffEntity(
                    id = "staff_1",
                    name = "Muhammad Yousuf",
                    email = "owner@business.com",
                    role = StaffRole.OWNER,
                    isActive = true
                )
            )
            dao.insertStaff(
                StaffEntity(
                    id = "staff_2",
                    name = "Zainab Ali",
                    email = "zainab@business.com",
                    role = StaffRole.ADMIN,
                    isActive = true
                )
            )
            dao.insertStaff(
                StaffEntity(
                    id = "staff_3",
                    name = "Bilal Khan",
                    email = "bilal@business.com",
                    role = StaffRole.AGENT,
                    isActive = true
                )
            )

            // Seed templates
            val t1 = TemplateEntity(
                id = "order_confirmation_v2",
                name = "order_confirmation_v2",
                language = "en_US",
                category = TemplateCategory.UTILITY,
                status = TemplateStatus.APPROVED,
                headerText = "Order Confirmation",
                bodyText = "Hello {{1}}, your order {{2}} has been confirmed and is scheduled for dispatch. Thank you for choosing us!",
                footerText = "Reply STOP to opt-out"
            )
            val t2 = TemplateEntity(
                id = "customer_reengagement",
                name = "customer_reengagement",
                language = "en_US",
                category = TemplateCategory.MARKETING,
                status = TemplateStatus.APPROVED,
                headerText = "We value your business",
                bodyText = "Hi {{1}}, we noticed it has been a while since we connected. Do you still require assistance with {{2}}?",
                footerText = "Support Team"
            )
            val t3 = TemplateEntity(
                id = "account_verification_code",
                name = "account_verification_code",
                language = "en_US",
                category = TemplateCategory.AUTHENTICATION,
                status = TemplateStatus.APPROVED,
                bodyText = "Your WhatsApp verification code is {{1}}. Do not share this code with anyone.",
                footerText = "Security Alert"
            )
            dao.insertTemplates(listOf(t1, t2, t3))

            // Seed initial conversations & messages
            val remoteConvs = MockBackendEngine.getConversations()
            for (c in remoteConvs) {
                val cust = CustomerEntity(
                    id = c.customerId,
                    name = c.customerName,
                    phoneNumber = c.customerPhone,
                    tags = c.tags
                )
                dao.insertOrUpdateCustomer(cust)

                val conv = ConversationEntity(
                    id = c.id,
                    customerId = c.customerId,
                    lastMessageText = c.lastMessageText,
                    lastMessageTimestamp = c.lastMessageTimestamp,
                    lastCustomerMessageTimestamp = c.lastMessageTimestamp,
                    unreadCount = c.unreadCount,
                    assignedStaffId = c.assignedStaffId,
                    isImportant = c.isImportant,
                    isArchived = c.isArchived,
                    windowExpiresAt = c.windowExpiresAt
                )
                dao.insertOrUpdateConversation(conv)

                // Seed messages for this thread
                val msg1 = MessageEntity(
                    id = "msg_${c.id}_1",
                    conversationId = c.id,
                    waMessageId = "wamid.HBgL" + UUID.randomUUID().toString().take(16),
                    senderType = SenderType.CUSTOMER,
                    content = "Hello there!",
                    status = MessageStatus.READ,
                    timestamp = c.lastMessageTimestamp - 1000 * 60 * 30
                )
                val msg2 = MessageEntity(
                    id = "msg_${c.id}_2",
                    conversationId = c.id,
                    waMessageId = "wamid.HBgL" + UUID.randomUUID().toString().take(16),
                    senderType = SenderType.BUSINESS,
                    senderStaffId = "staff_1",
                    content = "Hi! Welcome to our WhatsApp Business support channel. How may we assist you?",
                    status = MessageStatus.READ,
                    timestamp = c.lastMessageTimestamp - 1000 * 60 * 25
                )
                val msg3 = MessageEntity(
                    id = "msg_${c.id}_3",
                    conversationId = c.id,
                    waMessageId = "wamid.HBgL" + UUID.randomUUID().toString().take(16),
                    senderType = SenderType.CUSTOMER,
                    content = c.lastMessageText,
                    status = MessageStatus.DELIVERED,
                    timestamp = c.lastMessageTimestamp
                )
                dao.insertMessages(listOf(msg1, msg2, msg3))
            }

            dao.insertAuditLog(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    action = "INITIALIZE_INBOX",
                    staffEmail = "system@business.com",
                    details = "Unified inbox initialized with WhatsApp Cloud API v21.0 configuration."
                )
            )
        }
    }

    // --- CONVERSATIONS OBSERVERS ---
    fun getConversations(filter: InboxFilter, query: String): Flow<List<ConversationWithCustomer>> {
        return if (query.isNotBlank()) {
            dao.searchConversations(query.trim())
        } else {
            when (filter) {
                InboxFilter.ALL -> dao.getAllActiveConversations()
                InboxFilter.UNREAD -> dao.getUnreadConversations()
                InboxFilter.IMPORTANT -> dao.getImportantConversations()
                InboxFilter.ARCHIVED -> dao.getArchivedConversations()
                InboxFilter.ASSIGNED_TO_ME -> {
                    val staffId = _sessionState.value.userId
                    dao.getConversationsAssignedTo(staffId)
                }
            }
        }
    }

    suspend fun getConversationById(id: String): ConversationWithCustomer? = dao.getConversationById(id)

    suspend fun markConversationAsRead(id: String) {
        dao.markConversationAsRead(id)
        notificationManager.cancelNotification(id)
    }

    suspend fun toggleImportant(id: String, isImportant: Boolean) {
        dao.setImportant(id, isImportant)
    }

    suspend fun toggleArchived(id: String, isArchived: Boolean) {
        dao.setArchived(id, isArchived)
    }

    suspend fun assignConversation(conversationId: String, staffId: String?) {
        dao.assignConversationStaff(conversationId, staffId)
        val staffName = if (staffId != null) dao.getAllActiveStaff().firstOrNull()?.find { it.id == staffId }?.name else "Unassigned"
        dao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                action = "ASSIGN_CONVERSATION",
                staffEmail = _sessionState.value.email,
                details = "Assigned conversation $conversationId to $staffName"
            )
        )
    }

    // --- MESSAGES ---
    fun getMessages(conversationId: String): Flow<List<MessageEntity>> = dao.getMessagesForConversation(conversationId)

    suspend fun sendTextMessage(
        conversationId: String,
        recipientPhone: String,
        text: String,
        replyToMessageId: String? = null
    ): Result<MessageEntity> = withContext(Dispatchers.IO) {
        val tempId = "msg_local_" + UUID.randomUUID().toString().take(8)
        val now = System.currentTimeMillis()

        // 1. Write pending message immediately to Room for instant feedback
        val localMsg = MessageEntity(
            id = tempId,
            conversationId = conversationId,
            waMessageId = null,
            senderType = SenderType.BUSINESS,
            senderStaffId = _sessionState.value.userId,
            messageType = MessageType.TEXT,
            content = text,
            status = MessageStatus.PENDING,
            timestamp = now,
            replyToMessageId = replyToMessageId
        )
        dao.insertMessage(localMsg)

        // Update conversation preview
        val conv = dao.getConversationById(conversationId)?.conversation
        if (conv != null) {
            dao.insertOrUpdateConversation(
                conv.copy(
                    lastMessageText = text,
                    lastMessageTimestamp = now
                )
            )
        }

        // 2. Dispatch to WhatsApp Backend / Engine
        try {
            val response = MockBackendEngine.sendTextMessage(
                SendTextMessageRequest(
                    conversationId = conversationId,
                    recipientPhone = recipientPhone,
                    message = text,
                    replyToMessageId = replyToMessageId
                )
            )

            // Update to SENT with official waMessageId
            val updated = localMsg.copy(
                waMessageId = response.waMessageId,
                status = MessageStatus.SENT
            )
            dao.updateMessage(updated)

            // Schedule realistic delivery & read transitions
            scope.launch {
                delay(1200)
                dao.updateMessageStatusById(localMsg.id, MessageStatus.DELIVERED)
                delay(2000)
                dao.updateMessageStatusById(localMsg.id, MessageStatus.READ)
            }

            Result.success(updated)
        } catch (e: Exception) {
            val failed = localMsg.copy(
                status = MessageStatus.FAILED,
                failureReason = e.message ?: "Failed to send message via WhatsApp Cloud API"
            )
            dao.updateMessage(failed)
            Result.failure(e)
        }
    }

    suspend fun sendTemplateMessage(
        conversationId: String,
        recipientPhone: String,
        template: TemplateEntity,
        parameters: List<String>
    ): Result<MessageEntity> = withContext(Dispatchers.IO) {
        val tempId = "msg_tpl_" + UUID.randomUUID().toString().take(8)
        val now = System.currentTimeMillis()

        var formattedBody = template.bodyText
        parameters.forEachIndexed { index, param ->
            formattedBody = formattedBody.replace("{{${index + 1}}}", param)
        }

        val localMsg = MessageEntity(
            id = tempId,
            conversationId = conversationId,
            waMessageId = null,
            senderType = SenderType.BUSINESS,
            senderStaffId = _sessionState.value.userId,
            messageType = MessageType.TEMPLATE,
            content = "[${template.name}]\n$formattedBody",
            status = MessageStatus.PENDING,
            timestamp = now
        )
        dao.insertMessage(localMsg)

        // Template message successfully re-opens 24h window!
        val conv = dao.getConversationById(conversationId)?.conversation
        if (conv != null) {
            dao.insertOrUpdateConversation(
                conv.copy(
                    lastMessageText = "[Template] ${template.name}",
                    lastMessageTimestamp = now,
                    windowExpiresAt = now + 86_400_000L
                )
            )
        }

        try {
            val response = MockBackendEngine.sendTemplateMessage(
                SendTemplateMessageRequest(
                    conversationId = conversationId,
                    recipientPhone = recipientPhone,
                    templateName = template.name,
                    languageCode = template.language,
                    parameters = parameters
                )
            )

            val updated = localMsg.copy(
                waMessageId = response.waMessageId,
                status = MessageStatus.SENT
            )
            dao.updateMessage(updated)

            scope.launch {
                delay(1200)
                dao.updateMessageStatusById(localMsg.id, MessageStatus.DELIVERED)
                delay(2200)
                dao.updateMessageStatusById(localMsg.id, MessageStatus.READ)
            }

            Result.success(updated)
        } catch (e: Exception) {
            val failed = localMsg.copy(
                status = MessageStatus.FAILED,
                failureReason = e.message ?: "Failed to send WhatsApp template message"
            )
            dao.updateMessage(failed)
            Result.failure(e)
        }
    }

    suspend fun sendMediaMessage(
        conversationId: String,
        recipientPhone: String,
        type: MessageType,
        caption: String?,
        fileName: String? = null,
        mediaUrl: String? = null,
        mediaSize: Long? = null,
        mediaDurationMs: Long? = null,
        thumbnailUrl: String? = null
    ): Result<MessageEntity> = withContext(Dispatchers.IO) {
        val tempId = "msg_media_" + UUID.randomUUID().toString().take(8)
        val now = System.currentTimeMillis()

        val resolvedMediaUrl = mediaUrl ?: when (type) {
            MessageType.IMAGE -> "https://images.unsplash.com/photo-1557804506-669a67965ba0?w=600"
            MessageType.DOCUMENT -> "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"
            MessageType.AUDIO -> "https://actions.google.com/sounds/v1/alarms/beep_short.ogg"
            MessageType.VOICE -> "https://actions.google.com/sounds/v1/human_voices/applause.ogg"
            MessageType.VIDEO -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
            else -> null
        }

        val localMsg = MessageEntity(
            id = tempId,
            conversationId = conversationId,
            senderType = SenderType.BUSINESS,
            senderStaffId = _sessionState.value.userId,
            messageType = type,
            content = caption ?: if (type == MessageType.VOICE) "Voice message" else "[${type.name}]",
            mediaUrl = resolvedMediaUrl,
            mediaFileName = fileName ?: when (type) {
                MessageType.DOCUMENT -> "document.pdf"
                MessageType.AUDIO -> "audio.mp3"
                MessageType.VIDEO -> "video.mp4"
                MessageType.VOICE -> "voice_note.m4a"
                else -> null
            },
            mediaSize = mediaSize ?: when (type) {
                MessageType.VIDEO -> 4_200_000L
                MessageType.AUDIO -> 2_100_000L
                MessageType.VOICE -> 420_000L
                MessageType.IMAGE -> 320_000L
                else -> 120_000L
            },
            mediaDurationMs = mediaDurationMs ?: when (type) {
                MessageType.VOICE -> 14_000L
                MessageType.AUDIO -> 160_000L
                MessageType.VIDEO -> 32_000L
                else -> null
            },
            thumbnailUrl = thumbnailUrl ?: if (type == MessageType.VIDEO) "https://images.unsplash.com/photo-1536240478700-b869070f9279?w=600" else null,
            status = MessageStatus.PENDING,
            timestamp = now
        )
        dao.insertMessage(localMsg)

        // Update conversation last message
        val conv = dao.getConversationById(conversationId)?.conversation
        if (conv != null) {
            dao.insertOrUpdateConversation(
                conv.copy(
                    lastMessageText = if (type == MessageType.VOICE) "🎤 Voice message" else "[${type.name}] ${caption ?: ""}".trim(),
                    lastMessageTimestamp = now
                )
            )
        }

        try {
            val response = MockBackendEngine.sendMediaMessage(
                SendMediaMessageRequest(
                    conversationId = conversationId,
                    recipientPhone = recipientPhone,
                    mediaType = type.name.lowercase(),
                    mediaUrl = resolvedMediaUrl ?: "",
                    caption = caption,
                    fileName = fileName
                )
            )

            val updated = localMsg.copy(
                waMessageId = response.waMessageId,
                status = MessageStatus.SENT
            )
            dao.updateMessage(updated)

            scope.launch {
                delay(1200)
                dao.updateMessageStatusById(localMsg.id, MessageStatus.DELIVERED)
                delay(2200)
                dao.updateMessageStatusById(localMsg.id, MessageStatus.READ)
            }

            Result.success(updated)
        } catch (e: Exception) {
            val failed = localMsg.copy(
                status = MessageStatus.FAILED,
                failureReason = e.message ?: "Media message send failed"
            )
            dao.updateMessage(failed)
            Result.failure(e)
        }
    }

    suspend fun updateConversationWallpaper(conversationId: String, wallpaper: String?) = withContext(Dispatchers.IO) {
        val conv = dao.getConversationById(conversationId)?.conversation ?: return@withContext
        dao.insertOrUpdateConversation(conv.copy(customWallpaper = wallpaper))
    }

    // --- WEBHOOK EVENT PROCESSING (RECEIVE INCOMING MESSAGES & STATUS UPDATES) ---
    suspend fun processIncomingWebhook(payload: WhatsAppWebhookPayload) = withContext(Dispatchers.IO) {
        for (entry in payload.entry) {
            for (change in entry.changes) {
                val value = change.value

                // 1. Process new incoming messages
                value.messages?.forEach { incoming ->
                    val fromPhone = incoming.from
                    val convId = "conv_$fromPhone"
                    val contactName = value.contacts?.find { it.waId == fromPhone }?.profile?.name ?: "+$fromPhone"
                    val msgText = incoming.text?.body ?: incoming.image?.caption ?: incoming.document?.caption ?: "[${incoming.type.uppercase()}]"
                    val now = System.currentTimeMillis()

                    // Ensure Customer exists or is updated
                    val existingCust = dao.getCustomerById(fromPhone)
                    if (existingCust == null) {
                        dao.insertOrUpdateCustomer(
                            CustomerEntity(
                                id = fromPhone,
                                name = contactName,
                                phoneNumber = "+$fromPhone",
                                tags = listOf("New Customer")
                            )
                        )
                    }

                    // Update or insert Conversation (re-opens 24-hour customer service window)
                    val existingConv = dao.getConversationById(convId)?.conversation
                    val unread = (existingConv?.unreadCount ?: 0) + 1
                    val updatedConv = ConversationEntity(
                        id = convId,
                        customerId = fromPhone,
                        lastMessageText = msgText,
                        lastMessageTimestamp = now,
                        lastCustomerMessageTimestamp = now,
                        unreadCount = unread,
                        assignedStaffId = existingConv?.assignedStaffId,
                        isImportant = existingConv?.isImportant ?: false,
                        isArchived = false, // Unarchive on new incoming customer message
                        windowExpiresAt = now + 86_400_000L // 24-hour window starts now!
                    )
                    dao.insertOrUpdateConversation(updatedConv)

                    // Insert Message
                    val incomingMsg = MessageEntity(
                        id = "msg_in_${incoming.id}",
                        conversationId = convId,
                        waMessageId = incoming.id,
                        senderType = SenderType.CUSTOMER,
                        messageType = when (incoming.type) {
                            "image" -> MessageType.IMAGE
                            "document" -> MessageType.DOCUMENT
                            "audio" -> MessageType.AUDIO
                            else -> MessageType.TEXT
                        },
                        content = msgText,
                        mediaUrl = if (incoming.type == "image") "https://images.unsplash.com/photo-1557804506-669a67965ba0?w=600" else null,
                        mediaFileName = incoming.document?.filename,
                        status = MessageStatus.DELIVERED,
                        timestamp = now,
                        replyToMessageId = incoming.context?.id
                    )
                    dao.insertMessage(incomingMsg)

                    // Trigger Push Notification (Supports GMS / HMS / Fallback)
                    notificationManager.showIncomingMessageNotification(
                        conversationId = convId,
                        customerName = contactName,
                        messageText = msgText,
                        phoneNumber = "+$fromPhone"
                    )
                }

                // 2. Process message status updates
                value.statuses?.forEach { statusUpdate ->
                    val statusEnum = when (statusUpdate.status.lowercase()) {
                        "sent" -> MessageStatus.SENT
                        "delivered" -> MessageStatus.DELIVERED
                        "read" -> MessageStatus.READ
                        "failed" -> MessageStatus.FAILED
                        else -> MessageStatus.SENT
                    }
                    val failure = statusUpdate.errors?.firstOrNull()?.let { "${it.title}: ${it.message ?: ""}" }
                    dao.updateMessageStatusByWaId(statusUpdate.id, statusEnum, failure)
                }
            }
        }
    }

    // --- CUSTOMER MANAGEMENT ---
    suspend fun updateCustomerNotes(customerId: String, notes: String) {
        dao.updateCustomerNotes(customerId, notes)
    }

    suspend fun updateCustomerTags(customerId: String, tags: List<String>) {
        dao.updateCustomerTags(customerId, tags.joinToString(separator = "|||"))
    }

    suspend fun blockCustomer(customerId: String, blocked: Boolean) {
        dao.setCustomerBlocked(customerId, blocked)
    }

    // --- TEMPLATES & STAFF ---
    fun getAllTemplates(): Flow<List<TemplateEntity>> = dao.getAllTemplates()

    suspend fun syncTemplates(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val remote = MockBackendEngine.syncTemplates()
            val entities = remote.map {
                TemplateEntity(
                    id = it.id,
                    name = it.name,
                    language = it.language,
                    category = runCatching { TemplateCategory.valueOf(it.category) }.getOrDefault(TemplateCategory.UTILITY),
                    status = runCatching { TemplateStatus.valueOf(it.status) }.getOrDefault(TemplateStatus.APPROVED),
                    headerText = it.headerText,
                    bodyText = it.bodyText,
                    footerText = it.footerText
                )
            }
            dao.insertTemplates(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAllStaff(): Flow<List<StaffEntity>> = dao.getAllActiveStaff()

    suspend fun addStaff(name: String, email: String, role: StaffRole) {
        val staff = StaffEntity(
            id = "staff_" + UUID.randomUUID().toString().take(6),
            name = name,
            email = email,
            role = role,
            isActive = true
        )
        dao.insertStaff(staff)
        dao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                action = "ADD_STAFF",
                staffEmail = _sessionState.value.email,
                details = "Added staff member $name ($email) with role $role"
            )
        )
    }

    suspend fun deactivateStaff(staffId: String) {
        dao.deactivateStaff(staffId)
        dao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                action = "DEACTIVATE_STAFF",
                staffEmail = _sessionState.value.email,
                details = "Deactivated staff member ID $staffId"
            )
        )
    }

    // --- CONNECTION SETTINGS ---
    fun getBusinessFlow(): Flow<BusinessEntity?> = dao.getBusinessFlow()

    suspend fun connectWhatsAppAccount(
        metaBusinessAccountId: String,
        wabaId: String,
        phoneNumberId: String,
        webhookVerifyToken: String,
        displayPhone: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val res = MockBackendEngine.connectAccount(
                ConnectAccountRequest(
                    metaBusinessAccountId = metaBusinessAccountId,
                    wabaId = wabaId,
                    phoneNumberId = phoneNumberId,
                    webhookVerifyToken = webhookVerifyToken,
                    displayPhoneNumber = displayPhone
                )
            )

            val existing = dao.getBusiness() ?: BusinessEntity(id = "biz_default", name = "My Business")
            dao.saveBusiness(
                existing.copy(
                    metaBusinessAccountId = metaBusinessAccountId,
                    wabaId = wabaId,
                    phoneNumberId = phoneNumberId,
                    webhookVerifyToken = webhookVerifyToken,
                    displayPhoneNumber = displayPhone,
                    isConnected = true
                )
            )

            _sessionState.value = _sessionState.value.copy(
                isConnectedToWhatsApp = true,
                displayPhoneNumber = displayPhone
            )

            dao.insertAuditLog(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    action = "CONNECT_WABA",
                    staffEmail = _sessionState.value.email,
                    details = "Connected WABA ID $wabaId and Phone Number ID $phoneNumberId"
                )
            )

            Result.success(res.webhookCallbackUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun setServerUrl(url: String) {
        _serverUrl.value = url
    }

    fun setLiveServerMode(enabled: Boolean) {
        _isLiveServerMode.value = enabled
    }

    // --- AUTHENTICATION ---
    suspend fun login(email: String, pass: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val res = MockBackendEngine.login(LoginRequest(email, pass))
            val roleEnum = runCatching { StaffRole.valueOf(res.user.role) }.getOrDefault(StaffRole.OWNER)
            _sessionState.value = UserSession(
                isAuthenticated = true,
                userId = res.user.id,
                name = res.user.name,
                email = res.user.email,
                role = roleEnum,
                token = res.token,
                isConnectedToWhatsApp = true,
                displayPhoneNumber = "+1 555-019-2834"
            )
            dao.insertAuditLog(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    action = "LOGIN",
                    staffEmail = email,
                    details = "Staff login successful from Android client"
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(email: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val res = MockBackendEngine.resetPassword(ResetPasswordRequest(email))
            Result.success(res.message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        _sessionState.value = UserSession(isAuthenticated = false)
    }

    // --- METRICS ---
    fun getTotalActiveConversations() = dao.getTotalActiveConversationsCount()
    fun getActiveWindowConversations() = dao.getActiveWindowConversationsCount()
    fun getMessagesSent() = dao.getMessagesSentCount()
    fun getMessagesReceived() = dao.getMessagesReceivedCount()
    fun getTotalUnread() = dao.getTotalUnreadCountFlow()

    // --- CALLS & CALL LOGS ---
    fun getAllCallLogs(): Flow<List<CallLogEntity>> = dao.getAllCallLogs()

    fun getCallLogsForConversation(conversationId: String): Flow<List<CallLogEntity>> =
        dao.getCallLogsForConversation(conversationId)

    suspend fun recordCallLog(callLog: CallLogEntity): Unit = withContext(Dispatchers.IO) {
        dao.insertCallLog(callLog)

        // Also record a call event message inside the conversation thread
        val isVideo = callLog.callType == CallType.VIDEO
        val callTypeDesc = if (isVideo) "Video call" else "Audio call"
        val statusText = when (callLog.status) {
            CallStatus.MISSED -> "Missed $callTypeDesc"
            CallStatus.REJECTED -> "Declined $callTypeDesc"
            CallStatus.ENDED, CallStatus.CONNECTED -> {
                val mins = callLog.durationSeconds / 60
                val secs = callLog.durationSeconds % 60
                val durStr = if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
                val dir = if (callLog.direction == CallDirection.OUTGOING) "Outgoing" else "Incoming"
                "$dir $callTypeDesc ($durStr)"
            }
            else -> "$callTypeDesc (${callLog.status.name.lowercase()})"
        }

        val callMessage = MessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = callLog.conversationId,
            senderType = if (callLog.direction == CallDirection.OUTGOING) SenderType.BUSINESS else SenderType.CUSTOMER,
            messageType = if (isVideo) MessageType.VIDEO_CALL else MessageType.AUDIO_CALL,
            content = statusText,
            mediaDurationMs = callLog.durationSeconds * 1000L,
            status = MessageStatus.DELIVERED,
            timestamp = callLog.timestamp
        )
        dao.insertMessage(callMessage)

        // Update conversation's last message snippet
        val conv = dao.getConversationById(callLog.conversationId)?.conversation
        if (conv != null) {
            val snippet = if (isVideo) "📹 $statusText" else "📞 $statusText"
            dao.insertOrUpdateConversation(
                conv.copy(
                    lastMessageText = snippet,
                    lastMessageTimestamp = callLog.timestamp
                )
            )
        }
    }

    suspend fun deleteCallLog(id: String) = withContext(Dispatchers.IO) {
        dao.deleteCallLog(id)
    }

    suspend fun clearAllCallLogs() = withContext(Dispatchers.IO) {
        dao.clearAllCallLogs()
    }

    companion object {
        @Volatile
        private var INSTANCE: WhatsAppRepository? = null

        fun getInstance(context: Context): WhatsAppRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WhatsAppRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

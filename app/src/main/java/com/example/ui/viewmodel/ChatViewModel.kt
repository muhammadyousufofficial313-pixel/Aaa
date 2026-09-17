package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.calling.CallManager
import com.example.data.local.entities.ConversationWithCustomer
import com.example.data.local.entities.MessageEntity
import com.example.data.local.entities.StaffEntity
import com.example.data.local.entities.TemplateEntity
import com.example.data.repository.WhatsAppRepository
import com.example.domain.model.CallType
import com.example.domain.model.MessageType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed class ChatUiEvent {
    data class ShowSnackbar(val message: String) : ChatUiEvent()
    object MessageSent : ChatUiEvent()
}

class ChatViewModel(
    val conversationId: String,
    private val repository: WhatsAppRepository,
    private val callManager: CallManager? = null
) : ViewModel() {

    private val _conversation = MutableStateFlow<ConversationWithCustomer?>(null)
    val conversation: StateFlow<ConversationWithCustomer?> = _conversation.asStateFlow()

    val messages: StateFlow<List<MessageEntity>> = repository.getMessages(conversationId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val templates: StateFlow<List<TemplateEntity>> = repository.getAllTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val staffList: StateFlow<List<StaffEntity>> = repository.getAllStaff()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _replyingTo = MutableStateFlow<MessageEntity?>(null)
    val replyingTo: StateFlow<MessageEntity?> = _replyingTo.asStateFlow()

    private val _remainingWindowMillis = MutableStateFlow<Long>(0L)
    val remainingWindowMillis: StateFlow<Long> = _remainingWindowMillis.asStateFlow()

    private val _events = MutableSharedFlow<ChatUiEvent>()
    val events: SharedFlow<ChatUiEvent> = _events.asSharedFlow()

    init {
        loadConversation()
        startWindowCountdown()
    }

    private fun loadConversation() {
        viewModelScope.launch {
            _conversation.value = repository.getConversationById(conversationId)
            repository.markConversationAsRead(conversationId)
            updateWindowCalculation()
        }
    }

    private fun updateWindowCalculation() {
        val conv = _conversation.value?.conversation ?: return
        val remaining = (conv.windowExpiresAt - System.currentTimeMillis()).coerceAtLeast(0L)
        _remainingWindowMillis.value = remaining
    }

    private fun startWindowCountdown() {
        viewModelScope.launch {
            while (isActive) {
                delay(1000)
                updateWindowCalculation()
            }
        }
    }

    fun setReplyingTo(message: MessageEntity?) {
        _replyingTo.value = message
    }

    fun sendTextMessage(text: String) {
        val conv = _conversation.value ?: return
        if (text.isBlank()) return

        // 24-hour service window warning check:
        if (conv.conversation.windowExpiresAt < System.currentTimeMillis() && !conv.conversation.isImportant) {
            viewModelScope.launch {
                _events.emit(ChatUiEvent.ShowSnackbar("24-Hour Messaging Window Expired: You must send a pre-approved WhatsApp Template to re-engage this customer."))
            }
            return
        }

        viewModelScope.launch {
            _isSending.value = true
            val replyId = _replyingTo.value?.waMessageId
            val result = repository.sendTextMessage(
                conversationId = conversationId,
                recipientPhone = conv.customer?.phoneNumber ?: conv.conversation.customerId,
                text = text.trim(),
                replyToMessageId = replyId
            )
            _isSending.value = false
            _replyingTo.value = null

            result.fold(
                onSuccess = {
                    loadConversation()
                    _events.emit(ChatUiEvent.MessageSent)
                },
                onFailure = { err ->
                    _events.emit(ChatUiEvent.ShowSnackbar(err.message ?: "Failed to send message via WhatsApp"))
                }
            )
        }
    }

    fun sendTemplateMessage(template: TemplateEntity, parameters: List<String>) {
        val conv = _conversation.value ?: return

        viewModelScope.launch {
            _isSending.value = true
            val result = repository.sendTemplateMessage(
                conversationId = conversationId,
                recipientPhone = conv.customer?.phoneNumber ?: conv.conversation.customerId,
                template = template,
                parameters = parameters
            )
            _isSending.value = false

            result.fold(
                onSuccess = {
                    loadConversation()
                    _events.emit(ChatUiEvent.ShowSnackbar("Template message sent successfully. 24-hour window reopened!"))
                },
                onFailure = { err ->
                    _events.emit(ChatUiEvent.ShowSnackbar(err.message ?: "Failed to send template"))
                }
            )
        }
    }

    fun sendMediaMessage(
        type: MessageType,
        caption: String?,
        fileName: String? = null,
        mediaUrl: String? = null,
        mediaSize: Long? = null,
        mediaDurationMs: Long? = null,
        thumbnailUrl: String? = null
    ) {
        val conv = _conversation.value ?: return

        viewModelScope.launch {
            _isSending.value = true
            val result = repository.sendMediaMessage(
                conversationId = conversationId,
                recipientPhone = conv.customer?.phoneNumber ?: conv.conversation.customerId,
                type = type,
                caption = caption,
                fileName = fileName,
                mediaUrl = mediaUrl,
                mediaSize = mediaSize,
                mediaDurationMs = mediaDurationMs,
                thumbnailUrl = thumbnailUrl
            )
            _isSending.value = false

            result.fold(
                onSuccess = {
                    loadConversation()
                    _events.emit(ChatUiEvent.MessageSent)
                },
                onFailure = { err ->
                    _events.emit(ChatUiEvent.ShowSnackbar(err.message ?: "Failed to send media"))
                }
            )
        }
    }

    fun sendVoiceMessage(durationMs: Long, sizeBytes: Long, audioUri: String? = null) {
        sendMediaMessage(
            type = MessageType.VOICE,
            caption = "Voice message",
            fileName = "voice_note_${System.currentTimeMillis()}.m4a",
            mediaUrl = audioUri,
            mediaSize = sizeBytes,
            mediaDurationMs = durationMs
        )
    }

    fun sendAudioFileMessage(fileName: String, durationMs: Long, sizeBytes: Long, audioUri: String) {
        sendMediaMessage(
            type = MessageType.AUDIO,
            caption = fileName,
            fileName = fileName,
            mediaUrl = audioUri,
            mediaSize = sizeBytes,
            mediaDurationMs = durationMs
        )
    }

    fun sendVideoMessage(caption: String, videoUri: String, durationMs: Long, sizeBytes: Long) {
        sendMediaMessage(
            type = MessageType.VIDEO,
            caption = caption.ifBlank { "Video message" },
            fileName = "video_${System.currentTimeMillis()}.mp4",
            mediaUrl = videoUri,
            mediaSize = sizeBytes,
            mediaDurationMs = durationMs,
            thumbnailUrl = videoUri
        )
    }

    fun sendImageMessage(caption: String, imageUri: String, sizeBytes: Long) {
        sendMediaMessage(
            type = MessageType.IMAGE,
            caption = caption,
            fileName = "image_${System.currentTimeMillis()}.jpg",
            mediaUrl = imageUri,
            mediaSize = sizeBytes
        )
    }

    fun setConversationWallpaper(wallpaperValue: String?) {
        viewModelScope.launch {
            repository.updateConversationWallpaper(conversationId, wallpaperValue)
            loadConversation()
            _events.emit(ChatUiEvent.ShowSnackbar("Wallpaper updated for this chat."))
        }
    }

    fun updateCustomerNotes(notes: String) {
        val cust = _conversation.value?.customer ?: return
        viewModelScope.launch {
            repository.updateCustomerNotes(cust.id, notes)
            loadConversation()
            _events.emit(ChatUiEvent.ShowSnackbar("Customer notes saved."))
        }
    }

    fun updateCustomerTags(tags: List<String>) {
        val cust = _conversation.value?.customer ?: return
        viewModelScope.launch {
            repository.updateCustomerTags(cust.id, tags)
            loadConversation()
        }
    }

    fun assignStaff(staffId: String?) {
        viewModelScope.launch {
            repository.assignConversation(conversationId, staffId)
            loadConversation()
            _events.emit(ChatUiEvent.ShowSnackbar("Conversation assigned successfully."))
        }
    }

    fun toggleImportant() {
        val current = _conversation.value?.conversation?.isImportant ?: false
        viewModelScope.launch {
            repository.toggleImportant(conversationId, !current)
            loadConversation()
        }
    }

    fun toggleArchived() {
        val current = _conversation.value?.conversation?.isArchived ?: false
        viewModelScope.launch {
            repository.toggleArchived(conversationId, !current)
            loadConversation()
            _events.emit(ChatUiEvent.ShowSnackbar(if (!current) "Conversation archived" else "Conversation unarchived"))
        }
    }

    fun blockCustomer(blocked: Boolean) {
        val cust = _conversation.value?.customer ?: return
        viewModelScope.launch {
            repository.blockCustomer(cust.id, blocked)
            loadConversation()
            _events.emit(ChatUiEvent.ShowSnackbar(if (blocked) "Customer blocked" else "Customer unblocked"))
        }
    }

    // --- AUDIO & VIDEO CALLS ---
    fun startAudioCall() {
        val conv = _conversation.value ?: return
        val customerName = conv.customer?.name ?: conv.conversation.customerId
        val customerPhone = conv.customer?.phoneNumber ?: conv.conversation.customerId
        callManager?.startOutgoingCall(
            conversationId = conversationId,
            customerId = conv.conversation.customerId,
            customerName = customerName,
            customerPhone = customerPhone,
            callType = CallType.AUDIO
        )
    }

    fun startVideoCall() {
        val conv = _conversation.value ?: return
        val customerName = conv.customer?.name ?: conv.conversation.customerId
        val customerPhone = conv.customer?.phoneNumber ?: conv.conversation.customerId
        callManager?.startOutgoingCall(
            conversationId = conversationId,
            customerId = conv.conversation.customerId,
            customerName = customerName,
            customerPhone = customerPhone,
            callType = CallType.VIDEO
        )
    }

    fun simulateIncomingCall(callType: CallType = CallType.AUDIO) {
        val conv = _conversation.value ?: return
        val customerName = conv.customer?.name ?: conv.conversation.customerId
        val customerPhone = conv.customer?.phoneNumber ?: conv.conversation.customerId
        callManager?.simulateIncomingCall(
            conversationId = conversationId,
            customerId = conv.conversation.customerId,
            customerName = customerName,
            customerPhone = customerPhone,
            callType = callType
        )
    }
}

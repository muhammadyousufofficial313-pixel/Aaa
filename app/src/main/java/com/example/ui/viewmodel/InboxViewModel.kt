package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.ConversationWithCustomer
import com.example.data.remote.engine.MockBackendEngine
import com.example.data.repository.WhatsAppRepository
import com.example.domain.model.InboxFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class InboxUiEvent {
    data class ShowMessage(val text: String) : InboxUiEvent()
    data class OpenChat(val conversationId: String) : InboxUiEvent()
}

@OptIn(ExperimentalCoroutinesApi::class)
class InboxViewModel(private val repository: WhatsAppRepository) : ViewModel() {

    private val _activeFilter = MutableStateFlow(InboxFilter.ALL)
    val activeFilter: StateFlow<InboxFilter> = _activeFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _events = MutableSharedFlow<InboxUiEvent>()
    val events: SharedFlow<InboxUiEvent> = _events.asSharedFlow()

    val totalUnreadCount: StateFlow<Int> = repository.getTotalUnread()
        .combine(_searchQuery) { count, _ -> count ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val conversations: StateFlow<List<ConversationWithCustomer>> = combine(
        _activeFilter,
        _searchQuery
    ) { filter, query -> Pair(filter, query) }
        .flatMapLatest { (filter, query) ->
            repository.getConversations(filter, query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(filter: InboxFilter) {
        _activeFilter.value = filter
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun toggleImportant(conversationId: String, currentImportant: Boolean) {
        viewModelScope.launch {
            repository.toggleImportant(conversationId, !currentImportant)
        }
    }

    fun toggleArchived(conversationId: String, currentArchived: Boolean) {
        viewModelScope.launch {
            repository.toggleArchived(conversationId, !currentArchived)
            val msg = if (!currentArchived) "Conversation archived" else "Conversation unarchived"
            _events.emit(InboxUiEvent.ShowMessage(msg))
        }
    }

    fun simulateIncomingCustomerMessage(
        phoneNumber: String,
        customerName: String,
        messageText: String
    ) {
        viewModelScope.launch {
            val payload = MockBackendEngine.createSimulatedIncomingMessagePayload(
                fromPhone = phoneNumber,
                customerName = customerName,
                messageText = messageText
            )
            repository.processIncomingWebhook(payload)
            _events.emit(InboxUiEvent.ShowMessage("New WhatsApp message received from $customerName"))
        }
    }
}

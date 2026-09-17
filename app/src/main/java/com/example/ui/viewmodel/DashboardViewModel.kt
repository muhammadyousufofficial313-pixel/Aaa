package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.AuditLogEntity
import com.example.data.repository.WhatsAppRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DashboardMetrics(
    val totalConversations: Int = 0,
    val unreadCount: Int = 0,
    val activeWindowConversations: Int = 0,
    val messagesSent: Int = 0,
    val messagesReceived: Int = 0,
    val deliveryRatePercent: Int = 99
)

class DashboardViewModel(private val repository: WhatsAppRepository) : ViewModel() {

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    val metrics: StateFlow<DashboardMetrics> = combine(
        repository.getTotalActiveConversations(),
        repository.getTotalUnread(),
        repository.getActiveWindowConversations(),
        repository.getMessagesSent(),
        repository.getMessagesReceived()
    ) { total, unread, activeWindow, sent, received ->
        val totalMsgs = sent + received
        val deliveryRate = if (totalMsgs > 0) 99 else 100
        DashboardMetrics(
            totalConversations = total,
            unreadCount = unread ?: 0,
            activeWindowConversations = activeWindow,
            messagesSent = sent,
            messagesReceived = received,
            deliveryRatePercent = deliveryRate
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DashboardMetrics()
    )

    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.getBusinessFlow()
        .combine(repository.getAllTemplates()) { _, _ -> emptyList<AuditLogEntity>() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

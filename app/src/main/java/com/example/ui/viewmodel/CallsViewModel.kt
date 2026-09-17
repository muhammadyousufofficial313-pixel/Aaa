package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.calling.CallManager
import com.example.data.local.entities.CallLogEntity
import com.example.data.repository.WhatsAppRepository
import com.example.domain.model.ActiveCall
import com.example.domain.model.CallType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class CallFilter {
    ALL,
    MISSED,
    AUDIO,
    VIDEO
}

class CallsViewModel(
    private val repository: WhatsAppRepository,
    private val callManager: CallManager
) : ViewModel() {

    val activeCall: StateFlow<ActiveCall?> = callManager.activeCall

    private val _selectedFilter = MutableStateFlow(CallFilter.ALL)
    val selectedFilter: StateFlow<CallFilter> = _selectedFilter.asStateFlow()

    private val allCallLogs = repository.getAllCallLogs()

    val filteredCallLogs: StateFlow<List<CallLogEntity>> = combine(allCallLogs, _selectedFilter) { logs, filter ->
        when (filter) {
            CallFilter.ALL -> logs
            CallFilter.MISSED -> logs.filter { it.status == com.example.domain.model.CallStatus.MISSED }
            CallFilter.AUDIO -> logs.filter { it.callType == CallType.AUDIO }
            CallFilter.VIDEO -> logs.filter { it.callType == CallType.VIDEO }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(filter: CallFilter) {
        _selectedFilter.value = filter
    }

    fun startCall(
        conversationId: String,
        customerId: String,
        customerName: String,
        customerPhone: String,
        callType: CallType
    ) {
        callManager.startOutgoingCall(conversationId, customerId, customerName, customerPhone, callType)
    }

    fun simulateIncomingCall(
        conversationId: String,
        customerId: String,
        customerName: String,
        customerPhone: String,
        callType: CallType
    ) {
        callManager.simulateIncomingCall(conversationId, customerId, customerName, customerPhone, callType)
    }

    fun acceptCall() = callManager.acceptCall()
    fun rejectCall() = callManager.rejectCall()
    fun endCall() = callManager.endCall()
    fun toggleMute() = callManager.toggleMute()
    fun toggleSpeaker() = callManager.toggleSpeaker()
    fun toggleVideo() = callManager.toggleVideo()
    fun switchCamera() = callManager.switchCamera()
    fun minimizeCall(minimized: Boolean) = callManager.minimizeCall(minimized)

    fun deleteCallLog(id: String) {
        viewModelScope.launch {
            repository.deleteCallLog(id)
        }
    }

    fun clearAllCallLogs() {
        viewModelScope.launch {
            repository.clearAllCallLogs()
        }
    }
}

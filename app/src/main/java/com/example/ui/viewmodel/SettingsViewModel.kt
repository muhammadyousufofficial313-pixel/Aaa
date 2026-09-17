package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.BusinessEntity
import com.example.data.local.entities.StaffEntity
import com.example.data.local.entities.TemplateEntity
import com.example.data.notification.UnifiedNotificationManager
import com.example.data.repository.WhatsAppRepository
import com.example.domain.model.StaffRole
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class SettingsUiEvent {
    data class ShowSnackbar(val message: String) : SettingsUiEvent()
}

class SettingsViewModel(
    private val repository: WhatsAppRepository,
    private val notificationManager: UnifiedNotificationManager
) : ViewModel() {

    val business: StateFlow<BusinessEntity?> = repository.getBusinessFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val templates: StateFlow<List<TemplateEntity>> = repository.getAllTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val staffList: StateFlow<List<StaffEntity>> = repository.getAllStaff()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val serverUrl: StateFlow<String> = repository.serverUrl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "https://api.yourdomain.com/v1")

    val isLiveServerMode: StateFlow<Boolean> = repository.isLiveServerMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _isUrduLanguage = MutableStateFlow(false)
    val isUrduLanguage: StateFlow<Boolean> = _isUrduLanguage.asStateFlow()

    private val _events = MutableSharedFlow<SettingsUiEvent>()
    val events: SharedFlow<SettingsUiEvent> = _events.asSharedFlow()

    val pushPlatform = notificationManager.detectDevicePushPlatform()

    fun toggleLanguage(isUrdu: Boolean) {
        _isUrduLanguage.value = isUrdu
    }

    fun setServerUrl(url: String) {
        repository.setServerUrl(url)
    }

    fun setLiveServerMode(enabled: Boolean) {
        repository.setLiveServerMode(enabled)
        viewModelScope.launch {
            val mode = if (enabled) "Live Backend Server API" else "Local Sandbox Engine"
            _events.emit(SettingsUiEvent.ShowSnackbar("Switched to $mode"))
        }
    }

    fun connectWhatsApp(
        metaBusinessAccountId: String,
        wabaId: String,
        phoneNumberId: String,
        webhookVerifyToken: String,
        displayPhone: String
    ) {
        if (wabaId.isBlank() || phoneNumberId.isBlank() || webhookVerifyToken.isBlank()) {
            viewModelScope.launch {
                _events.emit(SettingsUiEvent.ShowSnackbar("Please fill in WABA ID, Phone Number ID and Webhook Verification Token."))
            }
            return
        }

        viewModelScope.launch {
            val result = repository.connectWhatsAppAccount(
                metaBusinessAccountId = metaBusinessAccountId.trim(),
                wabaId = wabaId.trim(),
                phoneNumberId = phoneNumberId.trim(),
                webhookVerifyToken = webhookVerifyToken.trim(),
                displayPhone = displayPhone.trim()
            )
            result.fold(
                onSuccess = { callbackUrl ->
                    _events.emit(SettingsUiEvent.ShowSnackbar("Connected! Webhook Callback URL: $callbackUrl"))
                },
                onFailure = { err ->
                    _events.emit(SettingsUiEvent.ShowSnackbar(err.message ?: "Connection failed"))
                }
            )
        }
    }

    fun syncTemplates() {
        viewModelScope.launch {
            val res = repository.syncTemplates()
            res.fold(
                onSuccess = {
                    _events.emit(SettingsUiEvent.ShowSnackbar("Templates synchronized successfully from Meta Graph API."))
                },
                onFailure = { err ->
                    _events.emit(SettingsUiEvent.ShowSnackbar(err.message ?: "Failed to sync templates"))
                }
            )
        }
    }

    fun addStaff(name: String, email: String, role: StaffRole) {
        if (name.isBlank() || email.isBlank()) return
        viewModelScope.launch {
            repository.addStaff(name.trim(), email.trim(), role)
            _events.emit(SettingsUiEvent.ShowSnackbar("Staff member added: $name ($role)"))
        }
    }

    fun deactivateStaff(staffId: String) {
        viewModelScope.launch {
            repository.deactivateStaff(staffId)
            _events.emit(SettingsUiEvent.ShowSnackbar("Staff member deactivated."))
        }
    }
}

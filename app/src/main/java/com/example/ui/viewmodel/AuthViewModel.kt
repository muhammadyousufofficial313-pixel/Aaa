package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.WhatsAppRepository
import com.example.domain.model.UserSession
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AuthUiEvent {
    data class ShowSnackbar(val message: String) : AuthUiEvent()
    object LoginSuccess : AuthUiEvent()
}

class AuthViewModel(private val repository: WhatsAppRepository) : ViewModel() {

    val sessionState: StateFlow<UserSession> = repository.sessionState
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UserSession(
                isAuthenticated = true,
                userId = "staff_1",
                name = "Muhammad Yousuf",
                email = "owner@business.com",
                role = com.example.domain.model.StaffRole.OWNER,
                isConnectedToWhatsApp = true,
                displayPhoneNumber = "+1 555-019-2834"
            )
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _events = MutableSharedFlow<AuthUiEvent>()
    val events: SharedFlow<AuthUiEvent> = _events.asSharedFlow()

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            viewModelScope.launch {
                _events.emit(AuthUiEvent.ShowSnackbar("Please enter your business email and password."))
            }
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.login(email.trim(), pass)
            _isLoading.value = false
            result.fold(
                onSuccess = {
                    _events.emit(AuthUiEvent.LoginSuccess)
                },
                onFailure = { err ->
                    _events.emit(AuthUiEvent.ShowSnackbar(err.message ?: "Authentication failed."))
                }
            )
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            viewModelScope.launch {
                _events.emit(AuthUiEvent.ShowSnackbar("Please enter your email to reset password."))
            }
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.resetPassword(email.trim())
            _isLoading.value = false
            result.fold(
                onSuccess = { msg ->
                    _events.emit(AuthUiEvent.ShowSnackbar(msg))
                },
                onFailure = { err ->
                    _events.emit(AuthUiEvent.ShowSnackbar(err.message ?: "Could not request password reset."))
                }
            )
        }
    }

    fun logout() {
        repository.logout()
    }
}

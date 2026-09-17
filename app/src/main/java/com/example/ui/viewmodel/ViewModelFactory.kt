package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.calling.CallManager
import com.example.data.notification.UnifiedNotificationManager
import com.example.data.repository.WhatsAppRepository

class AppViewModelFactory(
    private val context: Context,
    private val repository: WhatsAppRepository = WhatsAppRepository.getInstance(context),
    private val notificationManager: UnifiedNotificationManager = UnifiedNotificationManager(context),
    private val callManager: CallManager = CallManager.getInstance(context, repository)
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(repository) as T
            }
            modelClass.isAssignableFrom(InboxViewModel::class.java) -> {
                InboxViewModel(repository) as T
            }
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(repository) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(repository, notificationManager) as T
            }
            modelClass.isAssignableFrom(CallsViewModel::class.java) -> {
                CallsViewModel(repository, callManager) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    fun createChatViewModel(conversationId: String): ChatViewModel {
        return ChatViewModel(conversationId, repository, callManager)
    }

    fun getRepository(): WhatsAppRepository = repository
    fun getCallManager(): CallManager = callManager
}

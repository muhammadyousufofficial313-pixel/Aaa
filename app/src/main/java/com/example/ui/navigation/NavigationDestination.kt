package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Inbox : Screen("inbox")
    data class Chat(val conversationId: String) : Screen("chat/$conversationId") {
        companion object {
            const val ROUTE = "chat/{conversationId}"
            fun createRoute(conversationId: String) = "chat/$conversationId"
        }
    }
    object Dashboard : Screen("dashboard")
    object Templates : Screen("templates")
    object Staff : Screen("staff")
    object Calls : Screen("calls")
    object ConnectionSetup : Screen("connection_setup")
    object Settings : Screen("settings")
    data class ChatCustomization(val conversationId: String? = null) : Screen(
        if (conversationId != null) "chat_customization?conversationId=$conversationId" else "chat_customization"
    ) {
        companion object {
            const val ROUTE = "chat_customization?conversationId={conversationId}"
            fun createRoute(conversationId: String? = null) =
                if (conversationId != null) "chat_customization?conversationId=$conversationId" else "chat_customization"
        }
    }
}

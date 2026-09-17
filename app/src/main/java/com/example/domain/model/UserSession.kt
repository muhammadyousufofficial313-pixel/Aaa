package com.example.domain.model

data class UserSession(
    val isAuthenticated: Boolean = false,
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val role: StaffRole = StaffRole.AGENT,
    val token: String = "",
    val isConnectedToWhatsApp: Boolean = false,
    val displayPhoneNumber: String = ""
) {
    val isOwner: Boolean get() = role == StaffRole.OWNER
    val isAdmin: Boolean get() = role == StaffRole.ADMIN || role == StaffRole.OWNER
}

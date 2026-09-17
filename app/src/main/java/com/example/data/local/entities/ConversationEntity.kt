package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "conversations",
    indices = [
        Index(value = ["customerId"]),
        Index(value = ["lastMessageTimestamp"]),
        Index(value = ["isArchived"]),
        Index(value = ["isImportant"])
    ]
)
data class ConversationEntity(
    @PrimaryKey val id: String, // e.g. conv_15559876543
    val businessId: String = "default_biz",
    val customerId: String,
    val lastMessageText: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val lastCustomerMessageTimestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val assignedStaffId: String? = null,
    val isImportant: Boolean = false,
    val isArchived: Boolean = false,
    val windowExpiresAt: Long = System.currentTimeMillis() + 86_400_000L, // 24 hours customer service window
    val customWallpaper: String? = null,
    val customBubbleColor: String? = null
) {
    val isWindowActive: Boolean
        get() = System.currentTimeMillis() < windowExpiresAt

    val remainingWindowMillis: Long
        get() = (windowExpiresAt - System.currentTimeMillis()).coerceAtLeast(0L)
}

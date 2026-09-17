package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val id: String, // E.164 phone number, e.g. 15559876543
    val businessId: String = "default_biz",
    val name: String,
    val phoneNumber: String,
    val avatarUrl: String? = null,
    val notes: String = "",
    val tags: List<String> = emptyList(), // e.g. VIP, Urgent, Lead, Returning
    val isBlocked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

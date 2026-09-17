package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "businesses")
data class BusinessEntity(
    @PrimaryKey val id: String,
    val name: String,
    val metaBusinessAccountId: String = "",
    val wabaId: String = "",
    val phoneNumberId: String = "",
    val displayPhoneNumber: String = "",
    val webhookVerifyToken: String = "",
    val qualityRating: String = "GREEN",
    val isConnected: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

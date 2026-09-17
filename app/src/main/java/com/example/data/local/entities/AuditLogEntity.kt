package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val action: String,
    val staffEmail: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

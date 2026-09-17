package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.model.CallDirection
import com.example.domain.model.CallStatus
import com.example.domain.model.CallType

@Entity(
    tableName = "call_logs",
    indices = [
        Index(value = ["conversationId"]),
        Index(value = ["customerId"]),
        Index(value = ["timestamp"])
    ]
)
data class CallLogEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val callType: CallType,
    val direction: CallDirection,
    val status: CallStatus,
    val durationSeconds: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
)

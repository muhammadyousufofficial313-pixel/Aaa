package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.StaffRole

@Entity(tableName = "staff_members")
data class StaffEntity(
    @PrimaryKey val id: String,
    val businessId: String = "default_biz",
    val name: String,
    val email: String,
    val role: StaffRole = StaffRole.AGENT,
    val avatarUrl: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

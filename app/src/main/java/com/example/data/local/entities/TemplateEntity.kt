package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.TemplateCategory
import com.example.domain.model.TemplateStatus

@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey val id: String, // e.g. "order_confirmation_v2"
    val name: String,
    val language: String = "en_US",
    val category: TemplateCategory = TemplateCategory.UTILITY,
    val status: TemplateStatus = TemplateStatus.APPROVED,
    val headerText: String? = null,
    val bodyText: String,
    val footerText: String? = null,
    val buttonsJson: String = "[]",
    val syncedAt: Long = System.currentTimeMillis()
)

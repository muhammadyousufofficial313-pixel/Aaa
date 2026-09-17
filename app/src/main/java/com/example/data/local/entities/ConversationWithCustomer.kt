package com.example.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

data class ConversationWithCustomer(
    @Embedded val conversation: ConversationEntity,
    @Relation(
        parentColumn = "customerId",
        entityColumn = "id"
    )
    val customer: CustomerEntity?
)

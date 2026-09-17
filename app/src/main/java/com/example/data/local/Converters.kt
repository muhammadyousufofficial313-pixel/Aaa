package com.example.data.local

import androidx.room.TypeConverter
import com.example.domain.model.CallDirection
import com.example.domain.model.CallStatus
import com.example.domain.model.CallType
import com.example.domain.model.MessageStatus
import com.example.domain.model.MessageType
import com.example.domain.model.SenderType
import com.example.domain.model.StaffRole
import com.example.domain.model.TemplateCategory
import com.example.domain.model.TemplateStatus

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return value?.joinToString(separator = "|||") ?: ""
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split("|||").filter { it.isNotBlank() }
    }

    @TypeConverter
    fun fromSenderType(value: SenderType): String = value.name

    @TypeConverter
    fun toSenderType(value: String): SenderType = runCatching {
        SenderType.valueOf(value)
    }.getOrDefault(SenderType.CUSTOMER)

    @TypeConverter
    fun fromMessageType(value: MessageType): String = value.name

    @TypeConverter
    fun toMessageType(value: String): MessageType = runCatching {
        MessageType.valueOf(value)
    }.getOrDefault(MessageType.TEXT)

    @TypeConverter
    fun fromMessageStatus(value: MessageStatus): String = value.name

    @TypeConverter
    fun toMessageStatus(value: String): MessageStatus = runCatching {
        MessageStatus.valueOf(value)
    }.getOrDefault(MessageStatus.PENDING)

    @TypeConverter
    fun fromStaffRole(value: StaffRole): String = value.name

    @TypeConverter
    fun toStaffRole(value: String): StaffRole = runCatching {
        StaffRole.valueOf(value)
    }.getOrDefault(StaffRole.AGENT)

    @TypeConverter
    fun fromTemplateCategory(value: TemplateCategory): String = value.name

    @TypeConverter
    fun toTemplateCategory(value: String): TemplateCategory = runCatching {
        TemplateCategory.valueOf(value)
    }.getOrDefault(TemplateCategory.UTILITY)

    @TypeConverter
    fun fromTemplateStatus(value: TemplateStatus): String = value.name

    @TypeConverter
    fun toTemplateStatus(value: String): TemplateStatus = runCatching {
        TemplateStatus.valueOf(value)
    }.getOrDefault(TemplateStatus.APPROVED)

    @TypeConverter
    fun fromCallType(value: CallType): String = value.name

    @TypeConverter
    fun toCallType(value: String): CallType = runCatching {
        CallType.valueOf(value)
    }.getOrDefault(CallType.AUDIO)

    @TypeConverter
    fun fromCallDirection(value: CallDirection): String = value.name

    @TypeConverter
    fun toCallDirection(value: String): CallDirection = runCatching {
        CallDirection.valueOf(value)
    }.getOrDefault(CallDirection.OUTGOING)

    @TypeConverter
    fun fromCallStatus(value: CallStatus): String = value.name

    @TypeConverter
    fun toCallStatus(value: String): CallStatus = runCatching {
        CallStatus.valueOf(value)
    }.getOrDefault(CallStatus.ENDED)
}

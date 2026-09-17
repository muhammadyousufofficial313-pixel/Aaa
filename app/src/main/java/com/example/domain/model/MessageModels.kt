package com.example.domain.model

enum class SenderType {
    CUSTOMER,
    BUSINESS
}

enum class MessageType {
    TEXT,
    IMAGE,
    DOCUMENT,
    AUDIO,
    VOICE,
    VIDEO,
    TEMPLATE,
    AUDIO_CALL,
    VIDEO_CALL
}

enum class MessageStatus {
    PENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED
}

enum class StaffRole {
    OWNER,
    ADMIN,
    AGENT
}

enum class TemplateCategory {
    MARKETING,
    UTILITY,
    AUTHENTICATION
}

enum class TemplateStatus {
    APPROVED,
    PENDING,
    REJECTED
}

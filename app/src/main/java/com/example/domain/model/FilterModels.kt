package com.example.domain.model

enum class InboxFilter {
    ALL,
    UNREAD,
    IMPORTANT,
    ARCHIVED,
    ASSIGNED_TO_ME
}

data class SearchFilterState(
    val query: String = "",
    val activeFilter: InboxFilter = InboxFilter.ALL,
    val selectedTag: String? = null
)

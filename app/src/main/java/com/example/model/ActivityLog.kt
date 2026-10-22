package com.example.model

enum class ActivityType {
    CALL,
    EMAIL,
    NOTE,
    MEETING,
    STAGE_CHANGE
}

data class ActivityLog(
    val id: String,
    val dealId: String,
    val type: ActivityType,
    val title: String,
    val description: String,
    val timestamp: String,
    val authorName: String,
    val outcome: String = "Positive"
)

package com.example.model

enum class TaskType {
    CALL,
    EMAIL,
    MEETING,
    DEMO,
    REVIEW
}

data class TaskItem(
    val id: String,
    val title: String,
    val dealId: String? = null,
    val dealTitle: String? = null,
    val company: String = "",
    val dueDate: String, // e.g. "2026-10-22" or formatted
    val isOverdue: Boolean = false,
    val isCompleted: Boolean = false,
    val priority: DealPriority = DealPriority.MEDIUM,
    val type: TaskType = TaskType.CALL,
    val assignedRepName: String = "Sarah Connor",
    val notes: String = ""
)

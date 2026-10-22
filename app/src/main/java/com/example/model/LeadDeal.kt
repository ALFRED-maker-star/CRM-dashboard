package com.example.model

enum class DealPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

data class LeadDeal(
    val id: String,
    val title: String,
    val company: String,
    val value: Double,
    val stage: PipelineStage,
    val probability: Int, // e.g., 75%
    val expectedCloseDate: String, // e.g., "Nov 15, 2026"
    val assignedRepId: String,
    val assignedRepName: String,
    val assignedRepInitials: String,
    val contactName: String,
    val contactEmail: String,
    val contactPhone: String,
    val contactRole: String = "Decision Maker",
    val priority: DealPriority = DealPriority.MEDIUM,
    val notes: String = "",
    val industry: String = "Technology",
    val companySize: String = "250-500",
    val website: String = "example.com",
    val daysInStage: Int = 4,
    val createdAt: String = "Oct 01, 2026"
)

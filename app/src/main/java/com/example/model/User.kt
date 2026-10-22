package com.example.model

enum class UserRole {
    MANAGER,
    SALES_REP
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val title: String,
    val initials: String,
    val avatarBgHex: Long = 0xFF1D4ED8,
    val team: String = "Enterprise North America",
    val monthlyQuota: Double = 150000.0
)

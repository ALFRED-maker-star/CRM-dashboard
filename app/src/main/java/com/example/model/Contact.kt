package com.example.model

data class Contact(
    val id: String,
    val name: String,
    val company: String,
    val jobTitle: String,
    val email: String,
    val phone: String,
    val linkedDealsCount: Int,
    val totalValue: Double,
    val status: String = "Active", // Active, Lead, Prospect
    val location: String = "New York, NY",
    val tags: List<String> = listOf("VIP", "Decision Maker")
)

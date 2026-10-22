package com.example.model

data class TeamRep(
    val id: String,
    val name: String,
    val title: String,
    val initials: String,
    val avatarBgHex: Long,
    val quotaTarget: Double,
    val wonValue: Double,
    val wonDealsCount: Int,
    val activeDealsCount: Int,
    val pipelineValue: Double,
    val winRate: Int, // percentage
    val avgCycleDays: Int
) {
    val quotaAttainmentPercentage: Int
        get() = if (quotaTarget > 0) ((wonValue / quotaTarget) * 100).toInt() else 0
}

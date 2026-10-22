package com.example.model

data class StageMetric(
    val stage: PipelineStage,
    val count: Int,
    val totalValue: Double,
    val avgDays: Int,
    val conversionRateFromPrev: Int
)

data class MonthlyTrend(
    val monthName: String,
    val pipelineValue: Double,
    val wonValue: Double,
    val dealsClosed: Int
)

data class ForecastSummary(
    val quarter: String,
    val targetQuota: Double,
    val commitValue: Double,
    val bestCaseValue: Double,
    val pipelineWeightedValue: Double,
    val closedWonValue: Double
)

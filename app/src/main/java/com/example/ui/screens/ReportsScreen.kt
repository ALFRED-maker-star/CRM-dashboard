package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CrmRepository
import com.example.model.LeadDeal
import com.example.model.PipelineStage
import com.example.ui.components.MonthlyPipelineBarChart
import com.example.ui.components.WinLossDonutChart
import com.example.ui.theme.*

@Composable
fun ReportsScreen(
    deals: List<LeadDeal>,
    modifier: Modifier = Modifier
) {
    val wonDeals = remember(deals) { deals.filter { it.stage == PipelineStage.WON } }
    val lostDeals = remember(deals) { deals.filter { it.stage == PipelineStage.LOST } }
    val wonValue = remember(wonDeals) { wonDeals.sumOf { it.value } }
    val lostValue = remember(lostDeals) { lostDeals.sumOf { it.value } }

    val activeDeals = remember(deals) { deals.filter { it.stage != PipelineStage.WON && it.stage != PipelineStage.LOST } }
    val weightedPipeline = remember(activeDeals) {
        activeDeals.sumOf { it.value * (it.probability / 100.0) }
    }
    val bestCase = remember(activeDeals, wonValue) {
        wonValue + activeDeals.sumOf { it.value }
    }
    val q4Target = 500000.0
    val forecastTotal = wonValue + weightedPipeline
    val attainmentPct = ((forecastTotal / q4Target) * 100).toInt()

    LazyColumn(
        modifier = modifier
            .testTag("reports_screen")
            .fillMaxSize()
            .background(AppBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Revenue Intelligence & Reports",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "Forecast accuracy, conversion health, and cycle duration analytics",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }

                OutlinedButton(
                    onClick = { /* Export */ },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("export_report_button")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export CSV / PDF", color = TextPrimary, fontSize = 13.sp)
                }
            }
        }

        // Revenue Forecast vs Q4 Target
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
                color = SurfaceWhite
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Q4 Revenue Forecast Model",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Weighted probability algorithm vs Quarterly Target",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (attainmentPct >= 95) StageWonBg else StageProposalBg)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$attainmentPct% Projected Target Attainment",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (attainmentPct >= 95) StageWonText else StageProposalText
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4 Forecast metric pillars
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ForecastMetricBlock("Quarterly Target", CrmRepository.formatCurrency(q4Target), TextSecondary)
                        ForecastMetricBlock("Closed Won (Actual)", CrmRepository.formatCurrency(wonValue), SuccessGreen)
                        ForecastMetricBlock("Weighted Forecast", CrmRepository.formatCurrency(forecastTotal), BluePrimary)
                        ForecastMetricBlock("Best Case Upside", CrmRepository.formatCurrency(bestCase), PurpleAccent)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Segmented Forecast Visualizer Bar
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Pace: ${CrmRepository.formatCurrency(forecastTotal)} of ${CrmRepository.formatCurrency(q4Target)}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                            )
                            Text(
                                text = "Q4 Target",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = BlueDark)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(14.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(SurfaceSubtle)
                        ) {
                            // Won segment
                            val wonRatio = (wonValue / q4Target).toFloat().coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(wonRatio)
                                    .fillMaxHeight()
                                    .background(SuccessGreen)
                            )
                            // Weighted addition
                            val totalRatio = (forecastTotal / q4Target).toFloat().coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(totalRatio)
                                    .fillMaxHeight()
                                    .background(BluePrimary.copy(alpha = 0.5f))
                            )
                        }
                    }
                }
            }
        }

        // Charts: Monthly Trajectory & Win/Loss Donut
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MonthlyPipelineBarChart(modifier = Modifier.weight(1.3f))
                WinLossDonutChart(
                    wonCount = wonDeals.size,
                    lostCount = lostDeals.size,
                    wonValue = wonValue,
                    lostValue = lostValue,
                    modifier = Modifier.weight(1.1f)
                )
            }
        }

        // Average Deal Cycle Length Breakdown Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
                color = SurfaceWhite
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Average Deal Velocity & Stage Duration",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Average time enterprise opportunities spend in each pipeline stage",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp)
                            )
                        }

                        Text(
                            text = "26.4 Days Total Cycle",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = BluePrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val stageCycles = listOf(
                        Triple("New Inbound", "3.2 days", PipelineStage.NEW),
                        Triple("Contacted & Discovery", "4.8 days", PipelineStage.CONTACTED),
                        Triple("Qualified & Tech Validation", "8.5 days", PipelineStage.QUALIFIED),
                        Triple("Proposal & Legal Terms", "9.9 days", PipelineStage.PROPOSAL)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        stageCycles.forEach { (label, duration, stage) ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp)),
                                color = SurfaceSubtle.copy(alpha = 0.5f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(3.dp)
                                            .background(stage.barColor())
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = duration,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    )
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ForecastMetricBlock(label: String, value: String, valueColor: Color) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 11.sp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = valueColor
            )
        )
    }
}

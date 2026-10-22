package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.model.User
import com.example.ui.components.FunnelChart
import com.example.ui.components.StageBadge
import com.example.ui.components.StatCard
import com.example.ui.theme.*
import com.example.viewmodel.DashboardMetrics
import com.example.viewmodel.ScreenDestination

@Composable
fun DashboardScreen(
    currentUser: User?,
    metrics: DashboardMetrics,
    deals: List<LeadDeal>,
    onNavigate: (ScreenDestination) -> Unit,
    onOpenLeadDetail: (String) -> Unit,
    onOpenAddLeadModal: () -> Unit,
    onOpenAddContactModal: () -> Unit,
    onOpenAddTaskModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .testTag("dashboard_screen")
            .fillMaxSize()
            .background(AppBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Quick Actions Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Sales Operations Dashboard",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "Welcome back, ${currentUser?.name ?: "Sales Executive"} • Enterprise Pipeline Overview",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onOpenAddLeadModal,
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("quick_add_deal_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Deal", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = onOpenAddTaskModal,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("quick_add_task_button")
                    ) {
                        Icon(Icons.Default.AddTask, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New Task", color = TextPrimary, fontSize = 13.sp)
                    }
                }
            }
        }

        // 4 Stat Cards Grid / Adaptive Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Total Pipeline Value",
                        value = CrmRepository.formatCurrency(metrics.totalPipelineValue),
                        subtitle = "Active opportunities",
                        icon = Icons.Default.AccountBalanceWallet,
                        iconColor = BluePrimary,
                        iconBgColor = BlueContainer,
                        trendText = "+14.2% MoM",
                        isTrendPositive = true,
                        modifier = Modifier.weight(1f),
                        testTag = "stat_pipeline_value"
                    )

                    StatCard(
                        title = "Deals Won This Month",
                        value = CrmRepository.formatCurrency(metrics.dealsWonThisMonthValue),
                        subtitle = "${metrics.dealsWonCount} closed agreements",
                        icon = Icons.Default.CheckCircle,
                        iconColor = SuccessGreen,
                        iconBgColor = StageWonBg,
                        trendText = "On Target",
                        isTrendPositive = true,
                        modifier = Modifier.weight(1f),
                        testTag = "stat_deals_won"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Conversion Rate",
                        value = "${metrics.conversionRate}%",
                        subtitle = "+4.8% above quota target",
                        icon = Icons.Default.TrendingUp,
                        iconColor = PurpleAccent,
                        iconBgColor = StageQualifiedBg,
                        trendText = "Top Tier",
                        isTrendPositive = true,
                        modifier = Modifier.weight(1f),
                        testTag = "stat_conversion_rate"
                    )

                    StatCard(
                        title = "Tasks Due Today",
                        value = "${metrics.tasksDueTodayCount}",
                        subtitle = if (metrics.overdueTasksCount > 0) "${metrics.overdueTasksCount} overdue action items" else "All tasks on schedule",
                        icon = Icons.Default.AssignmentLate,
                        iconColor = if (metrics.overdueTasksCount > 0) DangerRed else WarningAmber,
                        iconBgColor = if (metrics.overdueTasksCount > 0) StageLostBg else StageProposalBg,
                        isWarning = metrics.overdueTasksCount > 0,
                        trendText = if (metrics.overdueTasksCount > 0) "Needs Action" else null,
                        isTrendPositive = false,
                        modifier = Modifier.weight(1f),
                        testTag = "stat_tasks_due"
                    )
                }
            }
        }

        // Funnel Chart
        item {
            FunnelChart(
                deals = deals,
                onStageClick = {
                    onNavigate(ScreenDestination.LEADS_KANBAN)
                }
            )
        }

        // Recent High-Value Deals Section
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
                                text = "High-Impact Opportunities",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Top enterprise accounts requiring attention",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp)
                            )
                        }

                        TextButton(
                            onClick = { onNavigate(ScreenDestination.LEADS_KANBAN) },
                            modifier = Modifier.testTag("view_kanban_button")
                        ) {
                            Text("Open Pipeline Board →", color = BluePrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        deals.take(5).forEach { deal ->
                            Row(
                                modifier = Modifier
                                    .testTag("deal_row_${deal.id}")
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceSubtle.copy(alpha = 0.6f))
                                    .clickable { onOpenLeadDetail(deal.id) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(BlueContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = deal.assignedRepInitials,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = OnBlueContainer
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Text(
                                            text = deal.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            ),
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${deal.company} • Rep: ${deal.assignedRepName}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = TextMuted,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    StageBadge(stage = deal.stage)
                                    Text(
                                        text = CrmRepository.formatCurrency(deal.value),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = TextPrimary
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

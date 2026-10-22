package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CrmRepository
import com.example.model.*
import com.example.ui.components.StageBadge
import com.example.ui.theme.*

@Composable
fun LeadDetailScreen(
    deal: LeadDeal?,
    activities: List<ActivityLog>,
    onBack: () -> Unit,
    onStageSelect: (PipelineStage) -> Unit,
    onOpenAddTask: () -> Unit,
    onOpenLogActivity: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    if (deal == null) {
        Box(
            modifier = modifier.fillMaxSize().background(AppBackground),
            contentAlignment = Alignment.Center
        ) {
            Text("Lead not found or has been removed.")
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .testTag("lead_detail_screen")
            .fillMaxSize()
            .background(AppBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back Navigation Bar & Action Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_from_lead_detail")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to list",
                            tint = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = deal.company,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = BluePrimary,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Text(
                            text = deal.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onOpenLogActivity,
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("log_activity_lead_detail_button")
                    ) {
                        Icon(Icons.Default.PhoneCallback, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Log Activity", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = onOpenAddTask,
                        colors = ButtonDefaults.buttonColors(containerColor = BlueContainer),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("add_task_lead_detail_button")
                    ) {
                        Icon(Icons.Default.AddTask, contentDescription = null, tint = OnBlueContainer, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Task", color = OnBlueContainer, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Interactive Stage Selector / Stepper Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
                color = SurfaceWhite
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Pipeline Stage:",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            StageBadge(stage = deal.stage)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (deal.stage != PipelineStage.WON) {
                                FilledTonalButton(
                                    onClick = { onStageSelect(PipelineStage.WON) },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = StageWonBg,
                                        contentColor = StageWonText
                                    ),
                                    modifier = Modifier.testTag("mark_won_button")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Mark Won", fontWeight = FontWeight.Bold)
                                }
                            }

                            if (deal.stage != PipelineStage.LOST) {
                                OutlinedButton(
                                    onClick = { onStageSelect(PipelineStage.LOST) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(DangerRed.copy(alpha = 0.5f))),
                                    modifier = Modifier.testTag("mark_lost_button")
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Mark Lost")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stages Stepper Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            PipelineStage.NEW,
                            PipelineStage.CONTACTED,
                            PipelineStage.QUALIFIED,
                            PipelineStage.PROPOSAL,
                            PipelineStage.WON
                        ).forEach { stage ->
                            val isCurrent = deal.stage == stage
                            val isPassed = deal.stage.order > stage.order && deal.stage != PipelineStage.LOST

                            Box(
                                modifier = Modifier
                                    .testTag("stage_pill_${stage.name.lowercase()}")
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            isCurrent -> stage.backgroundColor()
                                            isPassed -> BlueContainer.copy(alpha = 0.4f)
                                            else -> SurfaceSubtle
                                        }
                                    )
                                    .border(
                                        width = if (isCurrent) 1.5.dp else 1.dp,
                                        color = if (isCurrent) stage.barColor() else BorderSubtle,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onStageSelect(stage) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isPassed || isCurrent) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = if (isCurrent) stage.barColor() else BluePrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = stage.displayName,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isCurrent) stage.textColor() else TextSecondary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Deal Info & Contact Details Split / Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Key Deal Metrics
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
                    color = SurfaceWhite
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Deal Commercials",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        DetailMetricRow("Deal Value", CrmRepository.formatCurrency(deal.value), isBold = true, isBlue = true)
                        DetailMetricRow("Win Probability", "${deal.probability}%")
                        DetailMetricRow("Weighted Forecast", CrmRepository.formatCurrency(deal.value * (deal.probability / 100.0)))
                        DetailMetricRow("Target Close Date", deal.expectedCloseDate)
                        DetailMetricRow("Days in Stage", "${deal.daysInStage} days")
                        DetailMetricRow("Priority", deal.priority.name)
                        DetailMetricRow("Assigned Rep", deal.assignedRepName)
                    }
                }

                // Primary Contact Person Card
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
                    color = SurfaceWhite
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Primary Contact",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(BlueContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = deal.contactName.take(2).uppercase(),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = OnBlueContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = deal.contactName,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = deal.contactRole,
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = BorderSubtle)
                        Spacer(modifier = Modifier.height(12.dp))

                        DetailIconRow(Icons.Default.Email, deal.contactEmail)
                        DetailIconRow(Icons.Default.Phone, deal.contactPhone)
                        DetailIconRow(Icons.Default.Business, "${deal.company} (${deal.companySize} emp)")
                        DetailIconRow(Icons.Default.Language, deal.website)
                    }
                }
            }
        }

        // Deal Scope / Discovery Notes
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
                color = SurfaceWhite
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Executive Summary & Notes",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (deal.notes.isNotBlank()) deal.notes else "No notes provided yet.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, lineHeight = 20.sp)
                    )
                }
            }
        }

        // Activity Timeline Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Activity Timeline (${activities.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                TextButton(onClick = onOpenLogActivity) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = BluePrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log Entry", color = BluePrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (activities.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
                    color = SurfaceWhite
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No activities recorded for this deal yet. Click 'Log Activity' to record a call, email, or meeting.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                        )
                    }
                }
            }
        } else {
            items(activities, key = { it.id }) { log ->
                ActivityLogCard(log = log)
            }
        }
    }
}

@Composable
fun DetailMetricRow(label: String, value: String, isBold: Boolean = false, isBlue: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
                color = if (isBlue) BluePrimary else TextPrimary
            )
        )
    }
}

@Composable
fun DetailIconRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = text, style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary))
    }
}

@Composable
fun ActivityLogCard(log: ActivityLog) {
    val (icon, iconColor, iconBg) = when (log.type) {
        ActivityType.CALL -> Triple(Icons.Default.Phone, BluePrimary, BlueContainer)
        ActivityType.EMAIL -> Triple(Icons.Default.Email, PurpleAccent, StageQualifiedBg)
        ActivityType.MEETING -> Triple(Icons.Default.Groups, WarningAmber, StageProposalBg)
        ActivityType.NOTE -> Triple(Icons.Default.EditNote, SuccessGreen, StageWonBg)
        ActivityType.STAGE_CHANGE -> Triple(Icons.Default.SwapHoriz, BlueDark, BlueContainer)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp)),
        color = SurfaceWhite
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = log.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = log.timestamp,
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 11.sp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = log.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Logged by ${log.authorName}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

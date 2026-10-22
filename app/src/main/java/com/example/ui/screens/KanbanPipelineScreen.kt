package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.example.model.DealPriority
import com.example.model.LeadDeal
import com.example.model.PipelineStage
import com.example.model.User
import com.example.ui.components.StageBadge
import com.example.ui.theme.*

@Composable
fun KanbanPipelineScreen(
    deals: List<LeadDeal>,
    users: List<User>,
    onOpenLeadDetail: (String) -> Unit,
    onAdvanceStage: (String) -> Unit,
    onPreviousStage: (String) -> Unit,
    onSetStage: (String, PipelineStage) -> Unit,
    onOpenAddLeadModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedRepFilter by remember { mutableStateOf<String?>(null) }
    var showWonLostToggle by remember { mutableStateOf(false) } // toggle won vs lost in final column

    val filteredDeals = remember(deals, searchQuery, selectedRepFilter) {
        deals.filter { deal ->
            val matchesQuery = searchQuery.isBlank() ||
                    deal.title.contains(searchQuery, ignoreCase = true) ||
                    deal.company.contains(searchQuery, ignoreCase = true) ||
                    deal.contactName.contains(searchQuery, ignoreCase = true)
            val matchesRep = selectedRepFilter == null || deal.assignedRepId == selectedRepFilter
            matchesQuery && matchesRep
        }
    }

    val columns = listOf(
        PipelineStage.NEW,
        PipelineStage.CONTACTED,
        PipelineStage.QUALIFIED,
        PipelineStage.PROPOSAL,
        if (showWonLostToggle) PipelineStage.LOST else PipelineStage.WON
    )

    Column(
        modifier = modifier
            .testTag("kanban_pipeline_screen")
            .fillMaxSize()
            .background(AppBackground)
            .padding(16.dp)
    ) {
        // Kanban Header & Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Leads Pipeline Board",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = "Manage sales opportunities and stage transitions across the funnel",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Toggle between Won and Lost
                FilterChip(
                    selected = showWonLostToggle,
                    onClick = { showWonLostToggle = !showWonLostToggle },
                    label = { Text(if (showWonLostToggle) "Showing Lost" else "Showing Won") },
                    leadingIcon = {
                        Icon(
                            imageVector = if (showWonLostToggle) Icons.Default.Close else Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )

                Button(
                    onClick = onOpenAddLeadModal,
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_lead_kanban_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Lead", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search & Rep Filter Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Filter leads by title, company, contact...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("kanban_search_input"),
                singleLine = true
            )

            // Rep Filter Pills
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedRepFilter == null,
                    onClick = { selectedRepFilter = null },
                    label = { Text("All Reps") }
                )
                users.filter { it.role != com.example.model.UserRole.MANAGER }.forEach { user ->
                    FilterChip(
                        selected = selectedRepFilter == user.id,
                        onClick = { selectedRepFilter = if (selectedRepFilter == user.id) null else user.id },
                        label = { Text(user.name) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Kanban Columns Row (Horizontally Scrollable)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            columns.forEach { stage ->
                val stageDeals = filteredDeals.filter { it.stage == stage }
                val totalColumnValue = stageDeals.sumOf { it.value }

                KanbanColumn(
                    stage = stage,
                    deals = stageDeals,
                    totalValue = totalColumnValue,
                    onOpenLeadDetail = onOpenLeadDetail,
                    onAdvanceStage = onAdvanceStage,
                    onPreviousStage = onPreviousStage,
                    onSetStage = onSetStage,
                    modifier = Modifier.width(285.dp)
                )
            }
        }
    }
}

@Composable
fun KanbanColumn(
    stage: PipelineStage,
    deals: List<LeadDeal>,
    totalValue: Double,
    onOpenLeadDetail: (String) -> Unit,
    onAdvanceStage: (String) -> Unit,
    onPreviousStage: (String) -> Unit,
    onSetStage: (String, PipelineStage) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .testTag("kanban_column_${stage.name.lowercase()}")
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
        color = SurfaceSubtle.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            // Column Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(stage.barColor())
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StageBadge(stage = stage)
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SurfaceWhite)
                            .border(1.dp, BorderSubtle, CircleShape)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = deals.size.toString(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Text(
                    text = CrmRepository.formatCurrency(totalValue),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Deals Cards List
            if (deals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                        .background(SurfaceWhite.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No deals in ${stage.displayName}",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(deals, key = { it.id }) { deal ->
                        KanbanLeadCard(
                            deal = deal,
                            onClick = { onOpenLeadDetail(deal.id) },
                            onAdvance = { onAdvanceStage(deal.id) },
                            onPrevious = { onPreviousStage(deal.id) },
                            onSetStage = { newStage -> onSetStage(deal.id, newStage) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KanbanLeadCard(
    deal: LeadDeal,
    onClick: () -> Unit,
    onAdvance: () -> Unit,
    onPrevious: () -> Unit,
    onSetStage: (PipelineStage) -> Unit,
    modifier: Modifier = Modifier
) {
    var showStageMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .testTag("lead_card_${deal.id}")
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
            .clickable { onClick() },
        color = SurfaceWhite,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Priority & Value row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = deal.company.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                )

                Text(
                    text = CrmRepository.formatCurrency(deal.value),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = deal.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                ),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Contact & Probability
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = deal.contactName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    )
                }

                Text(
                    text = "${deal.probability}% win prob",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (deal.probability >= 50) StageWonText else TextMuted,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = BorderSubtle, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Footer: Assigned Rep & Stage Move Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(BlueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = deal.assignedRepInitials,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = OnBlueContainer,
                                fontSize = 10.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${deal.daysInStage}d",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    )
                }

                // Stage Stepping Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (deal.stage != PipelineStage.NEW) {
                        IconButton(
                            onClick = onPrevious,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Move to previous stage",
                                tint = TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Box {
                        IconButton(
                            onClick = { showStageMenu = !showStageMenu },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "Pick stage",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showStageMenu,
                            onDismissRequest = { showStageMenu = false },
                            modifier = Modifier.background(SurfaceWhite)
                        ) {
                            Text(
                                text = "MOVE TO STAGE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                            PipelineStage.entries.forEach { targetStage ->
                                DropdownMenuItem(
                                    text = { Text(targetStage.displayName) },
                                    onClick = {
                                        onSetStage(targetStage)
                                        showStageMenu = false
                                    },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(targetStage.barColor())
                                        )
                                    }
                                )
                            }
                        }
                    }

                    if (deal.stage != PipelineStage.WON && deal.stage != PipelineStage.LOST) {
                        IconButton(
                            onClick = onAdvance,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Advance to next stage",
                                tint = BluePrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

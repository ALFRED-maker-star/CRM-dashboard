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
import com.example.ui.components.StageBadge
import com.example.ui.theme.*

enum class DealSortOption {
    VALUE_DESC,
    VALUE_ASC,
    PROBABILITY_DESC,
    CLOSE_DATE
}

@Composable
fun DealsScreen(
    deals: List<LeadDeal>,
    users: List<User>,
    onOpenLeadDetail: (String) -> Unit,
    onOpenAddDeal: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStage by remember { mutableStateOf<PipelineStage?>(null) }
    var selectedRepId by remember { mutableStateOf<String?>(null) }
    var sortOption by remember { mutableStateOf(DealSortOption.VALUE_DESC) }

    val filteredAndSortedDeals = remember(deals, searchQuery, selectedStage, selectedRepId, sortOption) {
        val filtered = deals.filter { deal ->
            val matchesQuery = searchQuery.isBlank() ||
                    deal.title.contains(searchQuery, ignoreCase = true) ||
                    deal.company.contains(searchQuery, ignoreCase = true) ||
                    deal.contactName.contains(searchQuery, ignoreCase = true)
            val matchesStage = selectedStage == null || deal.stage == selectedStage
            val matchesRep = selectedRepId == null || deal.assignedRepId == selectedRepId
            matchesQuery && matchesStage && matchesRep
        }

        when (sortOption) {
            DealSortOption.VALUE_DESC -> filtered.sortedByDescending { it.value }
            DealSortOption.VALUE_ASC -> filtered.sortedBy { it.value }
            DealSortOption.PROBABILITY_DESC -> filtered.sortedByDescending { it.probability }
            DealSortOption.CLOSE_DATE -> filtered.sortedBy { it.expectedCloseDate }
        }
    }

    val totalFilteredValue = remember(filteredAndSortedDeals) {
        filteredAndSortedDeals.sumOf { it.value }
    }

    Column(
        modifier = modifier
            .testTag("deals_screen")
            .fillMaxSize()
            .background(AppBackground)
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Deals Ledger & Commercials",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = "Total Filtered Value: ${CrmRepository.formatCurrency(totalFilteredValue)} across ${filteredAndSortedDeals.size} opportunities",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }

            Button(
                onClick = onOpenAddDeal,
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("add_deal_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Deal", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search and Sort controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Filter deals by name, company, or rep...") },
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
                    .testTag("deals_search_input"),
                singleLine = true
            )

            // Sort Dropdown button
            var showSortMenu by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(
                    onClick = { showSortMenu = true },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(50.dp)
                ) {
                    Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (sortOption) {
                            DealSortOption.VALUE_DESC -> "Value (High → Low)"
                            DealSortOption.VALUE_ASC -> "Value (Low → High)"
                            DealSortOption.PROBABILITY_DESC -> "Probability (%)"
                            DealSortOption.CLOSE_DATE -> "Close Date"
                        },
                        color = TextPrimary,
                        fontSize = 12.sp
                    )
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false },
                    modifier = Modifier.background(SurfaceWhite)
                ) {
                    DropdownMenuItem(
                        text = { Text("Value: Highest First") },
                        onClick = { sortOption = DealSortOption.VALUE_DESC; showSortMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Value: Lowest First") },
                        onClick = { sortOption = DealSortOption.VALUE_ASC; showSortMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Highest Probability") },
                        onClick = { sortOption = DealSortOption.PROBABILITY_DESC; showSortMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Close Date") },
                        onClick = { sortOption = DealSortOption.CLOSE_DATE; showSortMenu = false }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Stage Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedStage == null,
                onClick = { selectedStage = null },
                label = { Text("All Stages (${deals.size})") }
            )
            PipelineStage.entries.forEach { stage ->
                val stageCount = deals.count { it.stage == stage }
                FilterChip(
                    selected = selectedStage == stage,
                    onClick = { selectedStage = if (selectedStage == stage) null else stage },
                    label = { Text("${stage.displayName} ($stageCount)") }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Deals Data Table
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
            color = SurfaceWhite
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Table Headers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceSubtle)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DEAL & COMPANY",
                        modifier = Modifier.weight(2.4f),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Text(
                        text = "STAGE",
                        modifier = Modifier.weight(1.3f),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Text(
                        text = "VALUE ($)",
                        modifier = Modifier.weight(1.4f),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Text(
                        text = "WIN PROB",
                        modifier = Modifier.weight(1.2f),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Text(
                        text = "CLOSE DATE",
                        modifier = Modifier.weight(1.4f),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Text(
                        text = "ASSIGNED REP",
                        modifier = Modifier.weight(1.6f),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                HorizontalDivider(color = BorderSubtle)

                if (filteredAndSortedDeals.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No deals match the selected criteria.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filteredAndSortedDeals, key = { it.id }) { deal ->
                            DealTableRow(
                                deal = deal,
                                onClick = { onOpenLeadDetail(deal.id) }
                            )
                            HorizontalDivider(color = BorderSubtle.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DealTableRow(
    deal: LeadDeal,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .testTag("deal_table_row_${deal.id}")
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Title & Company
        Column(modifier = Modifier.weight(2.4f)) {
            Text(
                text = deal.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                ),
                maxLines = 1
            )
            Text(
                text = "${deal.company} • ${deal.contactName}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            )
        }

        // Stage
        Box(modifier = Modifier.weight(1.3f)) {
            StageBadge(stage = deal.stage)
        }

        // Value
        Column(modifier = Modifier.weight(1.4f)) {
            Text(
                text = CrmRepository.formatCurrency(deal.value),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
        }

        // Probability
        Column(modifier = Modifier.weight(1.2f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (deal.probability >= 50) SuccessGreen else WarningAmber)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${deal.probability}%",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                )
            }
        }

        // Close Date
        Column(modifier = Modifier.weight(1.4f)) {
            Text(
                text = deal.expectedCloseDate,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary
                )
            )
        }

        // Assigned Rep
        Row(
            modifier = Modifier.weight(1.6f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(BlueContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = deal.assignedRepInitials,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = OnBlueContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = deal.assignedRepName,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )
        }
    }
}

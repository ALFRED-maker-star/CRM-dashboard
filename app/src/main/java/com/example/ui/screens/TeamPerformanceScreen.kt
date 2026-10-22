package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.model.TeamRep
import com.example.ui.theme.*

@Composable
fun TeamPerformanceScreen(
    teamReps: List<TeamRep>,
    modifier: Modifier = Modifier
) {
    val sortedReps = remember(teamReps) {
        teamReps.sortedByDescending { it.wonValue }
    }

    val totalTeamWon = remember(teamReps) { teamReps.sumOf { it.wonValue } }
    val totalTeamQuota = remember(teamReps) { teamReps.sumOf { it.quotaTarget } }
    val overallAttainment = if (totalTeamQuota > 0) ((totalTeamWon / totalTeamQuota) * 100).toInt() else 0

    LazyColumn(
        modifier = modifier
            .testTag("team_performance_screen")
            .fillMaxSize()
            .background(AppBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Sales Team Performance & Leaderboard",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = "Quota attainment, closed revenue velocity, and individual rep rankings",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
        }

        // Summary Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
                    color = SurfaceWhite
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "TOTAL TEAM BOOKINGS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = CrmRepository.formatCurrency(totalTeamWon),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = BluePrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$overallAttainment% of collective Q4 target",
                            style = MaterialTheme.typography.bodySmall.copy(color = SuccessGreen, fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
                    color = SurfaceWhite
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "TOP PERFORMER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = sortedReps.firstOrNull()?.name ?: "Sarah Connor",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${sortedReps.firstOrNull()?.quotaAttainmentPercentage ?: 150}% Quota • ${sortedReps.firstOrNull()?.wonDealsCount ?: 2} Won",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                        )
                    }
                }
            }
        }

        // Leaderboard Table
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
                color = SurfaceWhite
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Quarterly Rep Leaderboard",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "Ranked by booked closed won volume",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Table Column Headers
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceSubtle)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RANK & SALES REP",
                            modifier = Modifier.weight(2.5f),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextSecondary)
                        )
                        Text(
                            text = "QUOTA ATTAINMENT",
                            modifier = Modifier.weight(2.5f),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextSecondary)
                        )
                        Text(
                            text = "WON VALUE",
                            modifier = Modifier.weight(1.5f),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextSecondary)
                        )
                        Text(
                            text = "WIN RATE",
                            modifier = Modifier.weight(1.2f),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextSecondary)
                        )
                        Text(
                            text = "AVG CYCLE",
                            modifier = Modifier.weight(1.2f),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextSecondary)
                        )
                    }

                    HorizontalDivider(color = BorderSubtle)

                    sortedReps.forEachIndexed { index, rep ->
                        RepLeaderboardRow(rank = index + 1, rep = rep)
                        if (index < sortedReps.size - 1) {
                            HorizontalDivider(color = BorderSubtle.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }

        // Individual Performance Cards Grid
        item {
            Text(
                text = "Individual Rep Pipeline Deep Dive",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
        }

        itemsIndexed(sortedReps) { _, rep ->
            RepDeepDiveCard(rep = rep)
        }
    }
}

@Composable
fun RepLeaderboardRow(
    rank: Int,
    rep: TeamRep
) {
    val rankBadgeBg = when (rank) {
        1 -> Color(0xFFFEF3C7) // Gold
        2 -> Color(0xFFF1F5F9) // Silver
        3 -> Color(0xFFFFEDD5) // Bronze
        else -> SurfaceSubtle
    }
    val rankBadgeText = when (rank) {
        1 -> Color(0xFFD97706)
        2 -> Color(0xFF64748B)
        3 -> Color(0xFFC2410C)
        else -> TextSecondary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank & Rep
        Row(
            modifier = Modifier.weight(2.5f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(rankBadgeBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#$rank",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = rankBadgeText,
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(rep.avatarBgHex)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = rep.initials,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = rep.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = rep.title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                )
            }
        }

        // Quota Progress Bar
        Column(modifier = Modifier.weight(2.5f), verticalArrangement = Arrangement.Center) {
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${rep.quotaAttainmentPercentage}%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (rep.quotaAttainmentPercentage >= 100) SuccessGreen else TextPrimary
                    )
                )
                Text(
                    text = "Quota: ${CrmRepository.formatCurrency(rep.quotaTarget)}",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            val progress = (rep.quotaAttainmentPercentage / 100f).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (rep.quotaAttainmentPercentage >= 100) SuccessGreen else BluePrimary,
                trackColor = SurfaceSubtle
            )
        }

        // Won Value
        Column(modifier = Modifier.weight(1.5f)) {
            Text(
                text = CrmRepository.formatCurrency(rep.wonValue),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Text(
                text = "${rep.wonDealsCount} deals closed",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
            )
        }

        // Win Rate
        Column(modifier = Modifier.weight(1.2f)) {
            Text(
                text = "${rep.winRate}%",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = SuccessGreen
                )
            )
        }

        // Avg Cycle
        Column(modifier = Modifier.weight(1.2f)) {
            Text(
                text = "${rep.avgCycleDays} days",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            )
        }
    }
}

@Composable
fun RepDeepDiveCard(rep: TeamRep) {
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
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(rep.avatarBgHex)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = rep.initials,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = rep.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text(text = rep.title, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (rep.quotaAttainmentPercentage >= 100) StageWonBg else BlueContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (rep.quotaAttainmentPercentage >= 100) "Quota Exceeded" else "Active Pipeline",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (rep.quotaAttainmentPercentage >= 100) StageWonText else BluePrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = BorderSubtle)
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricColumn("Closed Revenue", CrmRepository.formatCurrency(rep.wonValue), isGreen = true)
                MetricColumn("In-Flight Pipeline", CrmRepository.formatCurrency(rep.pipelineValue))
                MetricColumn("Active Deals", "${rep.activeDealsCount} accounts")
                MetricColumn("Win Ratio", "${rep.winRate}%")
                MetricColumn("Avg Deal Cycle", "${rep.avgCycleDays} days")
            }
        }
    }
}

@Composable
fun MetricColumn(label: String, value: String, isGreen: Boolean = false) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (isGreen) SuccessGreen else TextPrimary
            )
        )
    }
}

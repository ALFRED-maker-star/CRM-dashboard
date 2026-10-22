package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CrmRepository
import com.example.model.LeadDeal
import com.example.model.PipelineStage
import com.example.ui.theme.*

data class FunnelStageData(
    val stage: PipelineStage,
    val count: Int,
    val totalValue: Double,
    val conversionRate: Int
)

@Composable
fun FunnelChart(
    deals: List<LeadDeal>,
    onStageClick: (PipelineStage) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val stages = listOf(
        PipelineStage.NEW,
        PipelineStage.CONTACTED,
        PipelineStage.QUALIFIED,
        PipelineStage.PROPOSAL,
        PipelineStage.WON
    )

    val funnelData = remember(deals) {
        val totalDealsCount = deals.filter { it.stage != PipelineStage.LOST }.size.coerceAtLeast(1)
        stages.map { stage ->
            val stageDeals = deals.filter { it.stage == stage }
            val count = stageDeals.size
            val sumVal = stageDeals.sumOf { it.value }
            val rate = ((count.toDouble() / totalDealsCount) * 100).toInt().coerceIn(12, 100)
            FunnelStageData(
                stage = stage,
                count = count,
                totalValue = sumVal,
                conversionRate = rate
            )
        }
    }

    Surface(
        modifier = modifier
            .testTag("pipeline_funnel_chart")
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
                        text = "Pipeline Conversion Funnel",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "Deal volume and drop-off velocity across active sales stages",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceSubtle)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Overall Conversion: 34.8%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = BluePrimary,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                funnelData.forEachIndexed { index, item ->
                    val animatedWidth by animateFloatAsState(
                        targetValue = (item.conversionRate / 100f).coerceIn(0.18f, 1.0f),
                        animationSpec = tween(durationMillis = 600),
                        label = "funnel_bar_width"
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onStageClick(item.stage) }
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                StageBadge(stage = item.stage)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${item.count} ${if (item.count == 1) "deal" else "deals"}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = TextSecondary
                                    )
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = CrmRepository.formatCurrency(item.totalValue),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                if (index < funnelData.size - 1) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Funnel Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(SurfaceSubtle)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedWidth)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                item.stage.barColor().copy(alpha = 0.85f),
                                                item.stage.barColor()
                                            )
                                        )
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

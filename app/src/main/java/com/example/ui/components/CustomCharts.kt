package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CrmRepository
import com.example.ui.theme.*

@Composable
fun WinLossDonutChart(
    wonCount: Int,
    lostCount: Int,
    wonValue: Double,
    lostValue: Double,
    modifier: Modifier = Modifier
) {
    val total = (wonCount + lostCount).coerceAtLeast(1)
    val winPercentage = ((wonCount.toFloat() / total) * 100).toInt()
    val wonSweep = (wonCount.toFloat() / total) * 360f

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
        color = SurfaceWhite
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Win / Loss Ratio",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Text(
                text = "Closed deals conversion effectiveness",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Donut Chart
                Box(
                    modifier = Modifier.size(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(110.dp)) {
                        val strokeWidth = 18.dp.toPx()
                        val arcSize = size.width - strokeWidth
                        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                        // Lost Arc (background ring)
                        drawArc(
                            color = DangerRed.copy(alpha = 0.85f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = strokeWidth)
                        )

                        // Won Arc
                        drawArc(
                            color = SuccessGreen,
                            startAngle = -90f,
                            sweepAngle = wonSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$winPercentage%",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 22.sp
                            )
                        )
                        Text(
                            text = "Win Rate",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                // Legend and Details
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(SuccessGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Won ($wonCount deals)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = CrmRepository.formatCurrency(wonValue),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = StageWonText,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(DangerRed)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Lost ($lostCount deals)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = CrmRepository.formatCurrency(lostValue),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = DangerRed,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyPipelineBarChart(
    modifier: Modifier = Modifier
) {
    val months = listOf(
        Triple("Jul", 920000.0, 240000.0),
        Triple("Aug", 1150000.0, 310000.0),
        Triple("Sep", 1280000.0, 390000.0),
        Triple("Oct", 1485000.0, 420000.0),
        Triple("Nov (Est)", 1650000.0, 480000.0),
        Triple("Dec (Est)", 1820000.0, 560000.0)
    )

    val maxPipeline = 2000000.0

    Surface(
        modifier = modifier
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
                        text = "Pipeline & Revenue Trajectory",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "Active pipeline volume vs booked closed won revenue",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(BluePrimary))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pipeline", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 11.sp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SuccessGreen))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Won", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 11.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bars row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                months.forEach { (label, pipeVal, wonVal) ->
                    val pipeHeight = ((pipeVal / maxPipeline) * 110).dp
                    val wonHeight = ((wonVal / maxPipeline) * 110).dp

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(14.dp)
                                    .height(pipeHeight)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(BluePrimary)
                            )
                            Box(
                                modifier = Modifier
                                    .width(14.dp)
                                    .height(wonHeight)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(SuccessGreen)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}

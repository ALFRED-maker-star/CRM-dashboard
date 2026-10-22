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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DealPriority
import com.example.model.TaskItem
import com.example.model.TaskType
import com.example.ui.theme.*
import com.example.viewmodel.TaskFilterTab

@Composable
fun TasksScreen(
    tasks: List<TaskItem>,
    currentFilter: TaskFilterTab,
    onFilterChange: (TaskFilterTab) -> Unit,
    onToggleCompletion: (String) -> Unit,
    onOpenAddTask: () -> Unit,
    onOpenDealDetail: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val filteredTasks = remember(tasks, currentFilter) {
        when (currentFilter) {
            TaskFilterTab.ALL -> tasks
            TaskFilterTab.TODAY -> tasks.filter { it.dueDate.contains("Today", ignoreCase = true) }
            TaskFilterTab.UPCOMING -> tasks.filter { !it.isOverdue && !it.dueDate.contains("Today", ignoreCase = true) && !it.isCompleted }
            TaskFilterTab.OVERDUE -> tasks.filter { it.isOverdue && !it.isCompleted }
            TaskFilterTab.COMPLETED -> tasks.filter { it.isCompleted }
        }
    }

    val overdueCount = remember(tasks) { tasks.count { it.isOverdue && !it.isCompleted } }
    val todayCount = remember(tasks) { tasks.count { it.dueDate.contains("Today", ignoreCase = true) && !it.isCompleted } }

    Column(
        modifier = modifier
            .testTag("tasks_screen")
            .fillMaxSize()
            .background(AppBackground)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Sales Tasks & Follow-ups",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = "Track customer touchpoints, scheduled calls, and action deliverables",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }

            Button(
                onClick = onOpenAddTask,
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("add_task_main_button")
            ) {
                Icon(Icons.Default.AddTask, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Task", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Filter Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceWhite)
                .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TaskFilterTab.entries.forEach { tab ->
                val isSelected = currentFilter == tab
                val badgeText = when (tab) {
                    TaskFilterTab.ALL -> "${tasks.size}"
                    TaskFilterTab.TODAY -> if (todayCount > 0) "$todayCount" else null
                    TaskFilterTab.OVERDUE -> if (overdueCount > 0) "$overdueCount" else null
                    TaskFilterTab.COMPLETED -> "${tasks.count { it.isCompleted }}"
                    TaskFilterTab.UPCOMING -> null
                }
                val isAlert = tab == TaskFilterTab.OVERDUE && overdueCount > 0

                Box(
                    modifier = Modifier
                        .testTag("task_filter_${tab.name.lowercase()}")
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) BlueContainer else Color.Transparent)
                        .clickable { onFilterChange(tab) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = when (tab) {
                                TaskFilterTab.ALL -> "All"
                                TaskFilterTab.TODAY -> "Today"
                                TaskFilterTab.UPCOMING -> "Upcoming"
                                TaskFilterTab.OVERDUE -> "Overdue"
                                TaskFilterTab.COMPLETED -> "Completed"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) BluePrimary else if (isAlert) DangerRed else TextSecondary,
                                fontSize = 11.sp
                            )
                        )
                        if (badgeText != null) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isAlert) DangerRed else if (isSelected) BluePrimary else SurfaceSubtle)
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isAlert || isSelected) Color.White else TextSecondary,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tasks List
        if (filteredTasks.isEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
                color = SurfaceWhite
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircleOutline,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No tasks in this category!",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "You are all caught up on sales deliverables.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredTasks, key = { it.id }) { task ->
                    TaskCardItem(
                        task = task,
                        onToggle = { onToggleCompletion(task.id) },
                        onDealClick = {
                            if (task.dealId != null && onOpenDealDetail != null) {
                                onOpenDealDetail(task.dealId)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskCardItem(
    task: TaskItem,
    onToggle: () -> Unit,
    onDealClick: () -> Unit
) {
    val isOverdueAlert = task.isOverdue && !task.isCompleted

    Surface(
        modifier = Modifier
            .testTag("task_item_${task.id}")
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = if (isOverdueAlert) 1.5.dp else 1.dp,
                color = if (isOverdueAlert) DangerRed.copy(alpha = 0.7f) else BorderSubtle,
                shape = RoundedCornerShape(10.dp)
            ),
        color = if (isOverdueAlert) StageLostBg.copy(alpha = 0.3f) else SurfaceWhite,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Completion Checkbox
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = SuccessGreen,
                    uncheckedColor = if (isOverdueAlert) DangerRed else TextMuted
                ),
                modifier = Modifier.testTag("task_checkbox_${task.id}")
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (task.isCompleted) TextMuted else TextPrimary
                        )
                    )

                    // Priority Badge
                    val (prioBg, prioColor) = when (task.priority) {
                        DealPriority.CRITICAL -> Pair(StageLostBg, DangerRed)
                        DealPriority.HIGH -> Pair(StageProposalBg, WarningAmber)
                        DealPriority.MEDIUM -> Pair(BlueContainer, BluePrimary)
                        DealPriority.LOW -> Pair(SurfaceSubtle, TextSecondary)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(prioBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = task.priority.name,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = prioColor,
                                fontSize = 9.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Deal reference
                if (task.dealTitle != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onDealClick() }
                    ) {
                        Text(
                            text = "${task.company} • ${task.dealTitle}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = BluePrimary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                if (task.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.notes,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Footer with Due Date & Overdue highlight
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val typeIcon: ImageVector = when (task.type) {
                            TaskType.CALL -> Icons.Default.Phone
                            TaskType.EMAIL -> Icons.Default.Email
                            TaskType.MEETING -> Icons.Default.Groups
                            TaskType.DEMO -> Icons.Default.Computer
                            TaskType.REVIEW -> Icons.Default.FactCheck
                        }
                        Icon(imageVector = typeIcon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = task.type.name,
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 11.sp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isOverdueAlert) {
                            Icon(Icons.Default.Warning, contentDescription = "Overdue", tint = DangerRed, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "OVERDUE • ${task.dueDate}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = DangerRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        } else {
                            Icon(Icons.Default.Schedule, contentDescription = "Due", tint = TextMuted, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = task.dueDate,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (task.dueDate.contains("Today")) WarningAmber else TextSecondary,
                                    fontWeight = if (task.dueDate.contains("Today")) FontWeight.Bold else FontWeight.Normal,
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

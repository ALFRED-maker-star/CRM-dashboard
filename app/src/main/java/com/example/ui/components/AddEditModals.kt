package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.window.Dialog
import com.example.model.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLeadDealDialog(
    onDismiss: () -> Unit,
    onConfirm: (LeadDeal) -> Unit,
    availableReps: List<User>
) {
    var title by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var valueStr by remember { mutableStateOf("") }
    var contactName by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var selectedStage by remember { mutableStateOf(PipelineStage.NEW) }
    var selectedRep by remember { mutableStateOf(availableReps.firstOrNull()?.name ?: "Sarah Connor") }
    var priority by remember { mutableStateOf(DealPriority.MEDIUM) }
    var closeDate by remember { mutableStateOf("Nov 30, 2026") }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .testTag("add_lead_dialog")
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(14.dp)),
            color = SurfaceWhite
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New Sales Lead / Deal",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_add_lead_dialog")) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(color = BorderSubtle)
                Spacer(modifier = Modifier.height(14.dp))

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = DangerRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Deal Title / Project Name *") },
                    placeholder = { Text("e.g. Enterprise Cloud Modernization") },
                    modifier = Modifier.fillMaxWidth().testTag("input_lead_title"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = company,
                        onValueChange = { company = it },
                        label = { Text("Company Name *") },
                        placeholder = { Text("e.g. Vertex Systems") },
                        modifier = Modifier.weight(1f).testTag("input_lead_company"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = valueStr,
                        onValueChange = { valueStr = it },
                        label = { Text("Deal Value ($) *") },
                        placeholder = { Text("e.g. 150000") },
                        modifier = Modifier.weight(1f).testTag("input_lead_value"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Initial Pipeline Stage",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(PipelineStage.NEW, PipelineStage.CONTACTED, PipelineStage.QUALIFIED, PipelineStage.PROPOSAL).forEach { stage ->
                        val isSelected = selectedStage == stage
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) stage.backgroundColor() else SurfaceSubtle)
                                .border(1.dp, if (isSelected) stage.barColor() else BorderSubtle, RoundedCornerShape(6.dp))
                                .clickable { selectedStage = stage }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = stage.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) stage.textColor() else TextSecondary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = contactName,
                    onValueChange = { contactName = it },
                    label = { Text("Primary Contact Person *") },
                    placeholder = { Text("e.g. Rachel Cooper") },
                    modifier = Modifier.fillMaxWidth().testTag("input_lead_contact_name"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = contactEmail,
                        onValueChange = { contactEmail = it },
                        label = { Text("Contact Email") },
                        placeholder = { Text("rachel@vertex.com") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        label = { Text("Phone") },
                        placeholder = { Text("+1 (555) 902-1234") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = selectedRep,
                        onValueChange = { selectedRep = it },
                        label = { Text("Assigned Sales Rep") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = closeDate,
                        onValueChange = { closeDate = it },
                        label = { Text("Est. Close Date") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Initial Discovery Notes / Scope") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsedVal = valueStr.toDoubleOrNull()
                            if (title.isBlank() || company.isBlank() || contactName.isBlank()) {
                                errorMessage = "Please fill in Title, Company, and Contact Person."
                            } else if (parsedVal == null || parsedVal <= 0) {
                                errorMessage = "Please provide a valid deal value."
                            } else {
                                val rep = availableReps.find { it.name.equals(selectedRep, ignoreCase = true) }
                                    ?: availableReps.firstOrNull()
                                val deal = LeadDeal(
                                    id = "deal_${System.currentTimeMillis()}",
                                    title = title.trim(),
                                    company = company.trim(),
                                    value = parsedVal,
                                    stage = selectedStage,
                                    probability = selectedStage.defaultProbability,
                                    expectedCloseDate = closeDate,
                                    assignedRepId = rep?.id ?: "user_rep1",
                                    assignedRepName = rep?.name ?: selectedRep,
                                    assignedRepInitials = rep?.initials ?: "SR",
                                    contactName = contactName.trim(),
                                    contactEmail = if (contactEmail.isNotBlank()) contactEmail.trim() else "contact@${company.lowercase().replace(" ", "")}.com",
                                    contactPhone = if (contactPhone.isNotBlank()) contactPhone.trim() else "+1 (555) 000-1234",
                                    priority = priority,
                                    notes = notes.trim()
                                )
                                onConfirm(deal)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        modifier = Modifier.testTag("submit_lead_button")
                    ) {
                        Text("Create Deal", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddContactDialog(
    onDismiss: () -> Unit,
    onConfirm: (Contact) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var jobTitle by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Lead") }
    var location by remember { mutableStateOf("New York, NY") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .testTag("add_contact_dialog")
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(14.dp)),
            color = SurfaceWhite
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add CRM Contact",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(color = BorderSubtle)
                Spacer(modifier = Modifier.height(14.dp))

                if (errorMessage != null) {
                    Text(text = errorMessage ?: "", color = DangerRed, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name *") },
                    placeholder = { Text("e.g. Jordan Sterling") },
                    modifier = Modifier.fillMaxWidth().testTag("input_contact_name"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = company,
                        onValueChange = { company = it },
                        label = { Text("Company *") },
                        placeholder = { Text("e.g. Apex Fintech") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = jobTitle,
                        onValueChange = { jobTitle = it },
                        label = { Text("Job Title") },
                        placeholder = { Text("e.g. VP Infrastructure") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email *") },
                        placeholder = { Text("jordan@apex.com") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone") },
                        placeholder = { Text("+1 (555) 345-6789") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("City, State / Region") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank() || company.isBlank() || email.isBlank()) {
                                errorMessage = "Name, Company, and Email are required."
                            } else {
                                val newContact = Contact(
                                    id = "c_${System.currentTimeMillis()}",
                                    name = name.trim(),
                                    company = company.trim(),
                                    jobTitle = if (jobTitle.isNotBlank()) jobTitle.trim() else "Executive",
                                    email = email.trim(),
                                    phone = if (phone.isNotBlank()) phone.trim() else "+1 (555) 123-4567",
                                    linkedDealsCount = 0,
                                    totalValue = 0.0,
                                    status = status,
                                    location = location
                                )
                                onConfirm(newContact)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        modifier = Modifier.testTag("submit_contact_button")
                    ) {
                        Text("Save Contact", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    deals: List<LeadDeal>,
    preselectedDealId: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (TaskItem) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedDeal by remember { mutableStateOf(deals.find { it.id == preselectedDealId } ?: deals.firstOrNull()) }
    var dueDate by remember { mutableStateOf("Today, 4:00 PM") }
    var taskType by remember { mutableStateOf(TaskType.CALL) }
    var priority by remember { mutableStateOf(DealPriority.HIGH) }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .testTag("add_task_dialog")
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(14.dp)),
            color = SurfaceWhite
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Schedule Follow-up Task",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(color = BorderSubtle)
                Spacer(modifier = Modifier.height(14.dp))

                if (errorMessage != null) {
                    Text(text = errorMessage ?: "", color = DangerRed, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Action *") },
                    placeholder = { Text("e.g. Follow up on technical demo feedback") },
                    modifier = Modifier.fillMaxWidth().testTag("input_task_title"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Activity Type", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(TaskType.CALL, TaskType.EMAIL, TaskType.MEETING, TaskType.REVIEW).forEach { type ->
                        val isSelected = taskType == type
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) BlueContainer else SurfaceSubtle)
                                .border(1.dp, if (isSelected) BluePrimary else BorderSubtle, RoundedCornerShape(6.dp))
                                .clickable { taskType = type }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = type.name,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) BluePrimary else TextSecondary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date / Time") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Context & Objectives") },
                    placeholder = { Text("Key questions or agenda items...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                errorMessage = "Please enter a task title."
                            } else {
                                val newTask = TaskItem(
                                    id = "task_${System.currentTimeMillis()}",
                                    title = title.trim(),
                                    dealId = selectedDeal?.id,
                                    dealTitle = selectedDeal?.title,
                                    company = selectedDeal?.company ?: "Client",
                                    dueDate = dueDate.trim(),
                                    isOverdue = false,
                                    isCompleted = false,
                                    priority = priority,
                                    type = taskType,
                                    notes = notes.trim()
                                )
                                onConfirm(newTask)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        modifier = Modifier.testTag("submit_task_button")
                    ) {
                        Text("Add Task", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun LogActivityDialog(
    deal: LeadDeal,
    onDismiss: () -> Unit,
    onConfirm: (ActivityType, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var activityType by remember { mutableStateOf(ActivityType.CALL) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .testTag("log_activity_dialog")
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(14.dp)),
            color = SurfaceWhite
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Log Sales Activity",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "Deal: ${deal.title} (${deal.company})",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )

                HorizontalDivider(color = BorderSubtle, modifier = Modifier.padding(vertical = 10.dp))

                if (errorMessage != null) {
                    Text(text = errorMessage ?: "", color = DangerRed, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Text("Channel / Type", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(ActivityType.CALL, ActivityType.EMAIL, ActivityType.NOTE, ActivityType.MEETING).forEach { type ->
                        val isSelected = activityType == type
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) BlueContainer else SurfaceSubtle)
                                .border(1.dp, if (isSelected) BluePrimary else BorderSubtle, RoundedCornerShape(6.dp))
                                .clickable { activityType = type }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = type.name,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) BluePrimary else TextSecondary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Subject / Summary *") },
                    placeholder = { Text("e.g. Discovery call on security compliance") },
                    modifier = Modifier.fillMaxWidth().testTag("input_activity_title"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Details & Key Takeaways *") },
                    placeholder = { Text("Spoke with client about timeline and budget approval...") },
                    modifier = Modifier.fillMaxWidth().testTag("input_activity_description"),
                    minLines = 3,
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isBlank() || description.isBlank()) {
                                errorMessage = "Please enter subject and details."
                            } else {
                                onConfirm(activityType, title.trim(), description.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        modifier = Modifier.testTag("submit_activity_button")
                    ) {
                        Text("Save Activity Log", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

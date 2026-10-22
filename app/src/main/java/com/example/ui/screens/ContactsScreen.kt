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
import com.example.model.Contact
import com.example.ui.theme.*

@Composable
fun ContactsScreen(
    contacts: List<Contact>,
    onOpenAddContact: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf<String?>(null) }

    val filteredContacts = remember(contacts, searchQuery, selectedStatusFilter) {
        contacts.filter { contact ->
            val matchesQuery = searchQuery.isBlank() ||
                    contact.name.contains(searchQuery, ignoreCase = true) ||
                    contact.company.contains(searchQuery, ignoreCase = true) ||
                    contact.email.contains(searchQuery, ignoreCase = true) ||
                    contact.jobTitle.contains(searchQuery, ignoreCase = true)
            val matchesStatus = selectedStatusFilter == null || contact.status.equals(selectedStatusFilter, ignoreCase = true)
            matchesQuery && matchesStatus
        }
    }

    Column(
        modifier = modifier
            .testTag("contacts_screen")
            .fillMaxSize()
            .background(AppBackground)
            .padding(16.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Customer & Prospect Directory",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = "Searchable records of key decision makers and accounts",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }

            Button(
                onClick = onOpenAddContact,
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("add_contact_button")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Contact", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search & Filter Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name, company, email or title...") },
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
                    .testTag("contact_search_input"),
                singleLine = true
            )

            // Status Filter Chips
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedStatusFilter == null,
                    onClick = { selectedStatusFilter = null },
                    label = { Text("All (${contacts.size})") }
                )
                listOf("Active", "Customer", "Lead", "Prospect").forEach { status ->
                    val count = contacts.count { it.status.equals(status, ignoreCase = true) }
                    FilterChip(
                        selected = selectedStatusFilter == status,
                        onClick = { selectedStatusFilter = if (selectedStatusFilter == status) null else status },
                        label = { Text("$status ($count)") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Data Table / Responsive Card List
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
            color = SurfaceWhite
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Table Header row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceSubtle)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CONTACT NAME & ROLE",
                        modifier = Modifier.weight(2.2f),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Text(
                        text = "COMPANY & LOCATION",
                        modifier = Modifier.weight(1.8f),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Text(
                        text = "COMMUNICATION",
                        modifier = Modifier.weight(2f),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Text(
                        text = "LINKED DEALS",
                        modifier = Modifier.weight(1.5f),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Text(
                        text = "STATUS",
                        modifier = Modifier.weight(1.2f),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                HorizontalDivider(color = BorderSubtle)

                if (filteredContacts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No contacts found matching criteria.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filteredContacts, key = { it.id }) { contact ->
                            ContactTableRow(contact = contact)
                            HorizontalDivider(color = BorderSubtle.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContactTableRow(contact: Contact) {
    Row(
        modifier = Modifier
            .testTag("contact_row_${contact.id}")
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Name & Role
        Row(
            modifier = Modifier.weight(2.2f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(BlueContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = OnBlueContainer,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = contact.jobTitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                )
            }
        }

        // Company & Location
        Column(modifier = Modifier.weight(1.8f)) {
            Text(
                text = contact.company,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            )
            Text(
                text = contact.location,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextMuted,
                    fontSize = 11.sp
                )
            )
        }

        // Email & Phone
        Column(modifier = Modifier.weight(2f)) {
            Text(
                text = contact.email,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = BluePrimary,
                    fontWeight = FontWeight.Medium
                )
            )
            Text(
                text = contact.phone,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            )
        }

        // Linked Deals
        Column(modifier = Modifier.weight(1.5f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (contact.linkedDealsCount > 0) SuccessGreen else TextMuted)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${contact.linkedDealsCount} ${if (contact.linkedDealsCount == 1) "deal" else "deals"}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            }
            if (contact.totalValue > 0) {
                Text(
                    text = CrmRepository.formatCurrency(contact.totalValue),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                )
            }
        }

        // Status Badge
        Box(modifier = Modifier.weight(1.2f)) {
            val (statusBg, statusText) = when (contact.status.lowercase()) {
                "active", "customer" -> Pair(StageWonBg, StageWonText)
                "lead" -> Pair(StageContactedBg, StageContactedText)
                else -> Pair(StageNewBg, StageNewText)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(statusBg)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = contact.status,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = statusText,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

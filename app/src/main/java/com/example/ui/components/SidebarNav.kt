package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.model.User
import com.example.model.UserRole
import com.example.ui.theme.*
import com.example.viewmodel.ScreenDestination

data class NavItem(
    val destination: ScreenDestination,
    val title: String,
    val icon: ImageVector,
    val badgeCount: Int? = null,
    val isAlertBadge: Boolean = false
)

@Composable
fun DesktopSidebar(
    currentDestination: ScreenDestination,
    currentUser: User?,
    allUsers: List<User>,
    onNavigate: (ScreenDestination) -> Unit,
    onSwitchUser: (User) -> Unit,
    onLogout: () -> Unit,
    leadsCount: Int = 0,
    tasksDueCount: Int = 0,
    modifier: Modifier = Modifier
) {
    var showUserMenu by remember { mutableStateOf(false) }

    val navItems = listOf(
        NavItem(ScreenDestination.DASHBOARD, "Dashboard", Icons.Default.Dashboard),
        NavItem(ScreenDestination.LEADS_KANBAN, "Leads Pipeline", Icons.Default.ViewWeek, badgeCount = leadsCount),
        NavItem(ScreenDestination.DEALS, "Deals Table", Icons.Default.MonetizationOn),
        NavItem(ScreenDestination.CONTACTS, "Contacts", Icons.Default.People),
        NavItem(ScreenDestination.TASKS, "Tasks / Follow-ups", Icons.Default.AssignmentTurnedIn, badgeCount = if (tasksDueCount > 0) tasksDueCount else null, isAlertBadge = true),
        NavItem(ScreenDestination.TEAM_PERFORMANCE, "Team Performance", Icons.Default.EmojiEvents),
        NavItem(ScreenDestination.REPORTS, "Reports & Forecast", Icons.Default.Insights)
    )

    Surface(
        modifier = modifier
            .testTag("desktop_sidebar")
            .width(260.dp)
            .fillMaxHeight(),
        color = SurfaceWhite,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .border(width = 1.dp, color = BorderSubtle)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Brand Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp, top = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(BluePrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "PipelinePro Logo",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "PipelinePro",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                fontSize = 18.sp,
                                letterSpacing = (-0.5).sp
                            )
                        )
                        Text(
                            text = "Enterprise CRM Suite",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Workspace pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceSubtle)
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SuccessGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Acme Global Corp",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        )
                    }
                    Text(
                        text = "Q4 Active",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = BluePrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "MAIN NAVIGATION",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                )

                // Navigation Items
                navItems.forEach { item ->
                    val isSelected = currentDestination == item.destination
                    val itemBg = if (isSelected) BlueContainer.copy(alpha = 0.7f) else Color.Transparent
                    val itemContentColor = if (isSelected) BluePrimary else TextSecondary
                    val itemFontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium

                    Row(
                        modifier = Modifier
                            .testTag("nav_${item.destination.name.lowercase()}")
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(itemBg)
                            .clickable { onNavigate(item.destination) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = itemContentColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = itemFontWeight,
                                    color = itemContentColor,
                                    fontSize = 13.5.sp
                                )
                            )
                        }

                        if (item.badgeCount != null) {
                            val badgeBg = if (item.isAlertBadge) DangerRed else BluePrimary
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(badgeBg)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = item.badgeCount.toString(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // User Profile Section
            Column {
                HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                if (currentUser != null) {
                    Box {
                        Row(
                            modifier = Modifier
                                .testTag("user_profile_pill")
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceSubtle)
                                .clickable { showUserMenu = !showUserMenu }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(currentUser.avatarBgHex)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = currentUser.initials,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = currentUser.name,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    )
                                    Text(
                                        text = if (currentUser.role == UserRole.MANAGER) "Sales Manager" else "Sales Rep",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.UnfoldMore,
                                contentDescription = "Switch user",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showUserMenu,
                            onDismissRequest = { showUserMenu = false },
                            modifier = Modifier.background(SurfaceWhite)
                        ) {
                            Text(
                                text = "SWITCH USER PROFILE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )

                            allUsers.forEach { user ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = user.name,
                                                fontWeight = if (user.id == currentUser.id) FontWeight.Bold else FontWeight.Normal
                                            )
                                            Text(
                                                text = "${user.title} (${user.role.name})",
                                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                                            )
                                        }
                                    },
                                    onClick = {
                                        onSwitchUser(user)
                                        showUserMenu = false
                                    },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color(user.avatarBgHex)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = user.initials,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp
                                                )
                                            )
                                        }
                                    }
                                )
                            }

                            HorizontalDivider()

                            DropdownMenuItem(
                                text = { Text("Log out / Switch account", color = DangerRed) },
                                onClick = {
                                    showUserMenu = false
                                    onLogout()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Logout,
                                        contentDescription = "Logout",
                                        tint = DangerRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompactTopAndBottomNav(
    currentDestination: ScreenDestination,
    currentUser: User?,
    onNavigate: (ScreenDestination) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Bottom bar for compact / mobile screens
    val primaryScreens = listOf(
        NavItem(ScreenDestination.DASHBOARD, "Dashboard", Icons.Default.Dashboard),
        NavItem(ScreenDestination.LEADS_KANBAN, "Leads", Icons.Default.ViewWeek),
        NavItem(ScreenDestination.DEALS, "Deals", Icons.Default.MonetizationOn),
        NavItem(ScreenDestination.CONTACTS, "Contacts", Icons.Default.People),
        NavItem(ScreenDestination.TASKS, "Tasks", Icons.Default.AssignmentTurnedIn)
    )

    NavigationBar(
        modifier = modifier
            .testTag("compact_bottom_nav")
            .navigationBarsPadding(),
        containerColor = SurfaceWhite,
        tonalElevation = 6.dp
    ) {
        primaryScreens.forEach { item ->
            val isSelected = currentDestination == item.destination
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.destination) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BluePrimary,
                    selectedTextColor = BluePrimary,
                    indicatorColor = BlueContainer,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                )
            )
        }
    }
}

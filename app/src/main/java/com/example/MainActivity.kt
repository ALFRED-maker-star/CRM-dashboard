package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.ActivityType
import com.example.model.UserRole
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.CrmViewModel
import com.example.viewmodel.ScreenDestination

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PipelineProApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PipelineProApp(
    viewModel: CrmViewModel = viewModel()
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val activeScreen by viewModel.activeScreen.collectAsStateWithLifecycle()
    val selectedDealId by viewModel.selectedDealId.collectAsStateWithLifecycle()
    val deals by viewModel.deals.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val activitiesMap by viewModel.activities.collectAsStateWithLifecycle()
    val dashboardMetrics by viewModel.dashboardMetrics.collectAsStateWithLifecycle()
    val taskFilter by viewModel.taskFilter.collectAsStateWithLifecycle()

    val showAddLeadModal by viewModel.showAddLeadModal.collectAsStateWithLifecycle()
    val showAddContactModal by viewModel.showAddContactModal.collectAsStateWithLifecycle()
    val showAddTaskModal by viewModel.showAddTaskModal.collectAsStateWithLifecycle()
    val showLogActivityModal by viewModel.showLogActivityModal.collectAsStateWithLifecycle()

    val allUsers = viewModel.repository.users
    val teamReps = remember(deals) { viewModel.repository.getTeamReps() }

    // Selected deal for details
    val selectedDeal = remember(deals, selectedDealId) {
        deals.find { it.id == selectedDealId } ?: deals.firstOrNull()
    }
    val currentActivities = remember(activitiesMap, selectedDealId) {
        if (selectedDealId != null) activitiesMap[selectedDealId] ?: emptyList() else emptyList()
    }

    // Modal dialogs
    if (showAddLeadModal) {
        AddLeadDealDialog(
            availableReps = allUsers.filter { it.role == UserRole.SALES_REP },
            onDismiss = { viewModel.showAddLeadModal.value = false },
            onConfirm = { deal -> viewModel.addDeal(deal) }
        )
    }

    if (showAddContactModal) {
        AddContactDialog(
            onDismiss = { viewModel.showAddContactModal.value = false },
            onConfirm = { contact -> viewModel.addContact(contact) }
        )
    }

    if (showAddTaskModal) {
        AddTaskDialog(
            deals = deals,
            preselectedDealId = selectedDealId,
            onDismiss = { viewModel.showAddTaskModal.value = false },
            onConfirm = { task -> viewModel.addTask(task) }
        )
    }

    if (showLogActivityModal && selectedDeal != null) {
        LogActivityDialog(
            deal = selectedDeal,
            onDismiss = { viewModel.showLogActivityModal.value = false },
            onConfirm = { type: ActivityType, title: String, desc: String ->
                viewModel.logActivity(selectedDeal.id, type, title, desc)
            }
        )
    }

    // Handle back button
    BackHandler(enabled = activeScreen != ScreenDestination.DASHBOARD && activeScreen != ScreenDestination.LOGIN) {
        viewModel.navigateBack()
    }

    // If on login screen
    if (activeScreen == ScreenDestination.LOGIN || currentUser == null) {
        LoginScreen(
            users = allUsers,
            onLoginSuccess = { user ->
                viewModel.setCurrentUser(user)
                viewModel.navigateTo(ScreenDestination.DASHBOARD)
            }
        )
        return
    }

    // Responsive Desktop-first Layout container
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        val isWideScreen = maxWidth >= 760.dp

        if (isWideScreen) {
            // Desktop / Tablet Landscape Layout: Sidebar on Left, Content on Right
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                DesktopSidebar(
                    currentDestination = activeScreen,
                    currentUser = currentUser,
                    allUsers = allUsers,
                    onNavigate = { destination -> viewModel.navigateTo(destination) },
                    onSwitchUser = { user -> viewModel.setCurrentUser(user) },
                    onLogout = { viewModel.logout() },
                    leadsCount = deals.filter { it.stage != com.example.model.PipelineStage.WON && it.stage != com.example.model.PipelineStage.LOST }.size,
                    tasksDueCount = dashboardMetrics.tasksDueTodayCount + dashboardMetrics.overdueTasksCount
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(AppBackground)
                ) {
                    AppScreenContent(
                        activeScreen = activeScreen,
                        currentUser = currentUser,
                        metrics = dashboardMetrics,
                        deals = deals,
                        contacts = contacts,
                        tasks = tasks,
                        teamReps = teamReps,
                        selectedDeal = selectedDeal,
                        currentActivities = currentActivities,
                        allUsers = allUsers,
                        taskFilter = taskFilter,
                        onNavigate = { viewModel.navigateTo(it) },
                        onOpenLeadDetail = { viewModel.openLeadDetail(it) },
                        onBack = { viewModel.navigateBack() },
                        onAdvanceStage = { viewModel.advanceDealStage(it) },
                        onPreviousStage = { viewModel.previousDealStage(it) },
                        onSetStage = { id, stage -> viewModel.updateDealStage(id, stage) },
                        onTaskFilterChange = { viewModel.setTaskFilter(it) },
                        onToggleTask = { viewModel.toggleTaskCompletion(it) },
                        onOpenAddLeadModal = { viewModel.showAddLeadModal.value = true },
                        onOpenAddContactModal = { viewModel.showAddContactModal.value = true },
                        onOpenAddTaskModal = { viewModel.showAddTaskModal.value = true },
                        onOpenLogActivityModal = { viewModel.showLogActivityModal.value = true }
                    )
                }
            }
        } else {
            // Compact / Mobile Layout with TopAppBar and Bottom Navigation Bar
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BluePrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "PipelinePro",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                            }
                        },
                        actions = {
                            var showUserDropdown by remember { mutableStateOf(false) }

                            IconButton(onClick = { viewModel.showAddLeadModal.value = true }) {
                                Icon(Icons.Default.Add, contentDescription = "Add Deal", tint = BluePrimary)
                            }

                            Box {
                                IconButton(onClick = { showUserDropdown = !showUserDropdown }) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(currentUser?.avatarBgHex ?: 0xFF1D4ED8)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = currentUser?.initials ?: "U",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = showUserDropdown,
                                    onDismissRequest = { showUserDropdown = false },
                                    modifier = Modifier.background(SurfaceWhite)
                                ) {
                                    Text(
                                        text = "${currentUser?.name} (${currentUser?.title})",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text = { Text("Team Leaderboard") },
                                        onClick = {
                                            viewModel.navigateTo(ScreenDestination.TEAM_PERFORMANCE)
                                            showUserDropdown = false
                                        },
                                        leadingIcon = { Icon(Icons.Default.EmojiEvents, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Reports & Forecast") },
                                        onClick = {
                                            viewModel.navigateTo(ScreenDestination.REPORTS)
                                            showUserDropdown = false
                                        },
                                        leadingIcon = { Icon(Icons.Default.Insights, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Switch Rep / Logout", color = DangerRed) },
                                        onClick = {
                                            showUserDropdown = false
                                            viewModel.logout()
                                        },
                                        leadingIcon = { Icon(Icons.Default.Logout, contentDescription = null, tint = DangerRed) }
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = SurfaceWhite
                        )
                    )
                },
                bottomBar = {
                    CompactTopAndBottomNav(
                        currentDestination = activeScreen,
                        currentUser = currentUser,
                        onNavigate = { destination -> viewModel.navigateTo(destination) },
                        onLogout = { viewModel.logout() }
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(AppBackground)
                ) {
                    AppScreenContent(
                        activeScreen = activeScreen,
                        currentUser = currentUser,
                        metrics = dashboardMetrics,
                        deals = deals,
                        contacts = contacts,
                        tasks = tasks,
                        teamReps = teamReps,
                        selectedDeal = selectedDeal,
                        currentActivities = currentActivities,
                        allUsers = allUsers,
                        taskFilter = taskFilter,
                        onNavigate = { viewModel.navigateTo(it) },
                        onOpenLeadDetail = { viewModel.openLeadDetail(it) },
                        onBack = { viewModel.navigateBack() },
                        onAdvanceStage = { viewModel.advanceDealStage(it) },
                        onPreviousStage = { viewModel.previousDealStage(it) },
                        onSetStage = { id, stage -> viewModel.updateDealStage(id, stage) },
                        onTaskFilterChange = { viewModel.setTaskFilter(it) },
                        onToggleTask = { viewModel.toggleTaskCompletion(it) },
                        onOpenAddLeadModal = { viewModel.showAddLeadModal.value = true },
                        onOpenAddContactModal = { viewModel.showAddContactModal.value = true },
                        onOpenAddTaskModal = { viewModel.showAddTaskModal.value = true },
                        onOpenLogActivityModal = { viewModel.showLogActivityModal.value = true }
                    )
                }
            }
        }
    }
}

@Composable
fun AppScreenContent(
    activeScreen: ScreenDestination,
    currentUser: com.example.model.User?,
    metrics: com.example.viewmodel.DashboardMetrics,
    deals: List<com.example.model.LeadDeal>,
    contacts: List<com.example.model.Contact>,
    tasks: List<com.example.model.TaskItem>,
    teamReps: List<com.example.model.TeamRep>,
    selectedDeal: com.example.model.LeadDeal?,
    currentActivities: List<com.example.model.ActivityLog>,
    allUsers: List<com.example.model.User>,
    taskFilter: com.example.viewmodel.TaskFilterTab,
    onNavigate: (ScreenDestination) -> Unit,
    onOpenLeadDetail: (String) -> Unit,
    onBack: () -> Unit,
    onAdvanceStage: (String) -> Unit,
    onPreviousStage: (String) -> Unit,
    onSetStage: (String, com.example.model.PipelineStage) -> Unit,
    onTaskFilterChange: (com.example.viewmodel.TaskFilterTab) -> Unit,
    onToggleTask: (String) -> Unit,
    onOpenAddLeadModal: () -> Unit,
    onOpenAddContactModal: () -> Unit,
    onOpenAddTaskModal: () -> Unit,
    onOpenLogActivityModal: () -> Unit
) {
    when (activeScreen) {
        ScreenDestination.DASHBOARD -> DashboardScreen(
            currentUser = currentUser,
            metrics = metrics,
            deals = deals,
            onNavigate = onNavigate,
            onOpenLeadDetail = onOpenLeadDetail,
            onOpenAddLeadModal = onOpenAddLeadModal,
            onOpenAddContactModal = onOpenAddContactModal,
            onOpenAddTaskModal = onOpenAddTaskModal
        )
        ScreenDestination.LEADS_KANBAN -> KanbanPipelineScreen(
            deals = deals,
            users = allUsers,
            onOpenLeadDetail = onOpenLeadDetail,
            onAdvanceStage = onAdvanceStage,
            onPreviousStage = onPreviousStage,
            onSetStage = onSetStage,
            onOpenAddLeadModal = onOpenAddLeadModal
        )
        ScreenDestination.LEAD_DETAIL -> LeadDetailScreen(
            deal = selectedDeal,
            activities = currentActivities,
            onBack = onBack,
            onStageSelect = { newStage ->
                if (selectedDeal != null) {
                    onSetStage(selectedDeal.id, newStage)
                }
            },
            onOpenAddTask = onOpenAddTaskModal,
            onOpenLogActivity = onOpenLogActivityModal
        )
        ScreenDestination.CONTACTS -> ContactsScreen(
            contacts = contacts,
            onOpenAddContact = onOpenAddContactModal
        )
        ScreenDestination.DEALS -> DealsScreen(
            deals = deals,
            users = allUsers,
            onOpenLeadDetail = onOpenLeadDetail,
            onOpenAddDeal = onOpenAddLeadModal
        )
        ScreenDestination.TASKS -> TasksScreen(
            tasks = tasks,
            currentFilter = taskFilter,
            onFilterChange = onTaskFilterChange,
            onToggleCompletion = onToggleTask,
            onOpenAddTask = onOpenAddTaskModal,
            onOpenDealDetail = onOpenLeadDetail
        )
        ScreenDestination.TEAM_PERFORMANCE -> TeamPerformanceScreen(
            teamReps = teamReps
        )
        ScreenDestination.REPORTS -> ReportsScreen(
            deals = deals
        )
        ScreenDestination.LOGIN -> {
            // Handled at top level
        }
    }
}

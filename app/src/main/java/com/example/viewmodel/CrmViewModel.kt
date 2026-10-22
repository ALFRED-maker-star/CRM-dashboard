package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CrmRepository
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class ScreenDestination {
    LOGIN,
    DASHBOARD,
    LEADS_KANBAN,
    LEAD_DETAIL,
    CONTACTS,
    DEALS,
    TASKS,
    TEAM_PERFORMANCE,
    REPORTS
}

enum class TaskFilterTab {
    ALL,
    TODAY,
    UPCOMING,
    OVERDUE,
    COMPLETED
}

data class DashboardMetrics(
    val totalPipelineValue: Double = 0.0,
    val dealsWonThisMonthValue: Double = 0.0,
    val dealsWonCount: Int = 0,
    val conversionRate: Int = 0,
    val tasksDueTodayCount: Int = 0,
    val overdueTasksCount: Int = 0
)

class CrmViewModel(
    val repository: CrmRepository = CrmRepository()
) : ViewModel() {

    // Current User & Auth
    private val _currentUser = MutableStateFlow<User?>(repository.users[0]) // Defaults to Alex Morgan (Manager)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Navigation Stack
    private val _navigationStack = MutableStateFlow<List<ScreenDestination>>(listOf(ScreenDestination.DASHBOARD))
    val currentScreen: StateFlow<ScreenDestination> = MutableStateFlow(ScreenDestination.DASHBOARD).apply {
        // We will keep this in sync
    }

    private val _activeScreen = MutableStateFlow(ScreenDestination.DASHBOARD)
    val activeScreen: StateFlow<ScreenDestination> = _activeScreen.asStateFlow()

    // Selected Lead for Detail View
    private val _selectedDealId = MutableStateFlow<String?>("deal_1")
    val selectedDealId: StateFlow<String?> = _selectedDealId.asStateFlow()

    // Filters & Queries
    private val _dealsStageFilter = MutableStateFlow<PipelineStage?>(null)
    val dealsStageFilter: StateFlow<PipelineStage?> = _dealsStageFilter.asStateFlow()

    private val _dealsRepFilter = MutableStateFlow<String?>(null) // repId or null for All
    val dealsRepFilter: StateFlow<String?> = _dealsRepFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _taskFilter = MutableStateFlow(TaskFilterTab.ALL)
    val taskFilter: StateFlow<TaskFilterTab> = _taskFilter.asStateFlow()

    // Dialogs
    val showAddLeadModal = MutableStateFlow(false)
    val showAddContactModal = MutableStateFlow(false)
    val showAddTaskModal = MutableStateFlow(false)
    val showLogActivityModal = MutableStateFlow(false)
    val showEditLeadModal = MutableStateFlow(false)

    // Data streams
    val deals = repository.deals
    val contacts = repository.contacts
    val tasks = repository.tasks
    val activities = repository.activities

    // Computed Dashboard Metrics
    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(deals, tasks) { dealList, taskList ->
        val activeDeals = dealList.filter { it.stage != PipelineStage.WON && it.stage != PipelineStage.LOST }
        val wonDeals = dealList.filter { it.stage == PipelineStage.WON }
        val totalDecided = dealList.count { it.stage == PipelineStage.WON || it.stage == PipelineStage.LOST }
        val winRate = if (totalDecided > 0) ((wonDeals.size.toDouble() / totalDecided) * 100).toInt() else 35

        val todayTasks = taskList.count { it.dueDate.contains("Today", ignoreCase = true) && !it.isCompleted }
        val overdueTasks = taskList.count { it.isOverdue && !it.isCompleted }

        DashboardMetrics(
            totalPipelineValue = activeDeals.sumOf { it.value },
            dealsWonThisMonthValue = wonDeals.sumOf { it.value },
            dealsWonCount = wonDeals.size,
            conversionRate = winRate,
            tasksDueTodayCount = todayTasks,
            overdueTasksCount = overdueTasks
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, DashboardMetrics())

    fun navigateTo(destination: ScreenDestination) {
        if (_activeScreen.value != destination) {
            val updated = _navigationStack.value + destination
            _navigationStack.value = updated
            _activeScreen.value = destination
        }
    }

    fun navigateBack(): Boolean {
        val stack = _navigationStack.value
        return if (stack.size > 1) {
            val newStack = stack.dropLast(1)
            _navigationStack.value = newStack
            _activeScreen.value = newStack.last()
            true
        } else {
            false
        }
    }

    fun openLeadDetail(dealId: String) {
        _selectedDealId.value = dealId
        navigateTo(ScreenDestination.LEAD_DETAIL)
    }

    fun setCurrentUser(user: User) {
        _currentUser.value = user
    }

    fun logout() {
        _currentUser.value = null
        _navigationStack.value = listOf(ScreenDestination.LOGIN)
        _activeScreen.value = ScreenDestination.LOGIN
    }

    fun setDealsStageFilter(stage: PipelineStage?) {
        _dealsStageFilter.value = stage
    }

    fun setDealsRepFilter(repId: String?) {
        _dealsRepFilter.value = repId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setTaskFilter(filter: TaskFilterTab) {
        _taskFilter.value = filter
    }

    fun updateDealStage(dealId: String, newStage: PipelineStage) {
        val repName = _currentUser.value?.name ?: "Sales Rep"
        repository.updateDealStage(dealId, newStage, repName)
    }

    fun advanceDealStage(dealId: String) {
        val deal = deals.value.find { it.id == dealId } ?: return
        val nextStage = when (deal.stage) {
            PipelineStage.NEW -> PipelineStage.CONTACTED
            PipelineStage.CONTACTED -> PipelineStage.QUALIFIED
            PipelineStage.QUALIFIED -> PipelineStage.PROPOSAL
            PipelineStage.PROPOSAL -> PipelineStage.WON
            PipelineStage.WON -> null
            PipelineStage.LOST -> PipelineStage.NEW
        }
        if (nextStage != null) {
            updateDealStage(dealId, nextStage)
        }
    }

    fun previousDealStage(dealId: String) {
        val deal = deals.value.find { it.id == dealId } ?: return
        val prevStage = when (deal.stage) {
            PipelineStage.NEW -> null
            PipelineStage.CONTACTED -> PipelineStage.NEW
            PipelineStage.QUALIFIED -> PipelineStage.CONTACTED
            PipelineStage.PROPOSAL -> PipelineStage.QUALIFIED
            PipelineStage.WON -> PipelineStage.PROPOSAL
            PipelineStage.LOST -> PipelineStage.PROPOSAL
        }
        if (prevStage != null) {
            updateDealStage(dealId, prevStage)
        }
    }

    fun addDeal(deal: LeadDeal) {
        repository.addDeal(deal)
        showAddLeadModal.value = false
    }

    fun updateDeal(deal: LeadDeal) {
        repository.updateDeal(deal)
        showEditLeadModal.value = false
    }

    fun addContact(contact: Contact) {
        repository.addContact(contact)
        showAddContactModal.value = false
    }

    fun toggleTaskCompletion(taskId: String) {
        repository.toggleTaskCompletion(taskId)
    }

    fun addTask(task: TaskItem) {
        repository.addTask(task)
        showAddTaskModal.value = false
    }

    fun logActivity(dealId: String, type: ActivityType, title: String, description: String) {
        val authorName = _currentUser.value?.name ?: "Sales Rep"
        repository.logActivity(dealId, type, title, description, authorName)
        showLogActivityModal.value = false
    }
}

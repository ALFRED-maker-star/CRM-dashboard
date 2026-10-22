package com.example.data

import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.NumberFormat
import java.util.Locale

class CrmRepository {

    val users = listOf(
        User(
            id = "user_mgr",
            name = "Alex Morgan",
            email = "alex.morgan@pipelinepro.io",
            role = UserRole.MANAGER,
            title = "VP of Global Sales",
            initials = "AM",
            avatarBgHex = 0xFF1D4ED8,
            team = "Global Enterprise Revenue",
            monthlyQuota = 450000.0
        ),
        User(
            id = "user_rep1",
            name = "Sarah Connor",
            email = "sarah.connor@pipelinepro.io",
            role = UserRole.SALES_REP,
            title = "Sr. Enterprise Account Executive",
            initials = "SC",
            avatarBgHex = 0xFF0284C7,
            team = "Enterprise North America",
            monthlyQuota = 160000.0
        ),
        User(
            id = "user_rep2",
            name = "David Miller",
            email = "david.miller@pipelinepro.io",
            role = UserRole.SALES_REP,
            title = "Key Account Director",
            initials = "DM",
            avatarBgHex = 0xFF4338CA,
            team = "Enterprise EMEA",
            monthlyQuota = 140000.0
        ),
        User(
            id = "user_rep3",
            name = "Elena Rostova",
            email = "elena.rostova@pipelinepro.io",
            role = UserRole.SALES_REP,
            title = "Strategic Deals AE",
            initials = "ER",
            avatarBgHex = 0xFF059669,
            team = "FinTech & Cloud Solutions",
            monthlyQuota = 150000.0
        )
    )

    private val _deals = MutableStateFlow<List<LeadDeal>>(getInitialDeals())
    val deals: StateFlow<List<LeadDeal>> = _deals.asStateFlow()

    private val _contacts = MutableStateFlow<List<Contact>>(getInitialContacts())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    private val _tasks = MutableStateFlow<List<TaskItem>>(getInitialTasks())
    val tasks: StateFlow<List<TaskItem>> = _tasks.asStateFlow()

    private val _activities = MutableStateFlow<Map<String, List<ActivityLog>>>(getInitialActivities())
    val activities: StateFlow<Map<String, List<ActivityLog>>> = _activities.asStateFlow()

    fun updateDealStage(dealId: String, newStage: PipelineStage, repName: String = "Sales Rep") {
        val current = _deals.value
        val target = current.find { it.id == dealId } ?: return
        val updated = current.map {
            if (it.id == dealId) {
                it.copy(
                    stage = newStage,
                    probability = newStage.defaultProbability,
                    daysInStage = 0
                )
            } else it
        }
        _deals.value = updated

        // Log automatic activity
        logActivity(
            dealId = dealId,
            type = ActivityType.STAGE_CHANGE,
            title = "Stage updated to ${newStage.displayName}",
            description = "Pipeline status moved from ${target.stage.displayName} to ${newStage.displayName}.",
            authorName = repName
        )
    }

    fun addDeal(deal: LeadDeal) {
        _deals.value = listOf(deal) + _deals.value
        // If contact doesn't exist, create it
        val existing = _contacts.value.find { it.name.equals(deal.contactName, ignoreCase = true) }
        if (existing == null) {
            val newContact = Contact(
                id = "c_${System.currentTimeMillis()}",
                name = deal.contactName,
                company = deal.company,
                jobTitle = deal.contactRole,
                email = deal.contactEmail,
                phone = deal.contactPhone,
                linkedDealsCount = 1,
                totalValue = deal.value
            )
            _contacts.value = listOf(newContact) + _contacts.value
        } else {
            _contacts.value = _contacts.value.map {
                if (it.id == existing.id) {
                    it.copy(
                        linkedDealsCount = it.linkedDealsCount + 1,
                        totalValue = it.totalValue + deal.value
                    )
                } else it
            }
        }
    }

    fun updateDeal(updated: LeadDeal) {
        _deals.value = _deals.value.map { if (it.id == updated.id) updated else it }
    }

    fun addContact(contact: Contact) {
        _contacts.value = listOf(contact) + _contacts.value
    }

    fun toggleTaskCompletion(taskId: String) {
        _tasks.value = _tasks.value.map {
            if (it.id == taskId) it.copy(isCompleted = !it.isCompleted) else it
        }
    }

    fun addTask(task: TaskItem) {
        _tasks.value = listOf(task) + _tasks.value
    }

    fun logActivity(
        dealId: String,
        type: ActivityType,
        title: String,
        description: String,
        authorName: String
    ) {
        val newLog = ActivityLog(
            id = "act_${System.currentTimeMillis()}",
            dealId = dealId,
            type = type,
            title = title,
            description = description,
            timestamp = "Just now",
            authorName = authorName
        )
        val currentMap = _activities.value.toMutableMap()
        val list = currentMap[dealId]?.toMutableList() ?: mutableListOf()
        list.add(0, newLog)
        currentMap[dealId] = list
        _activities.value = currentMap
    }

    fun getTeamReps(): List<TeamRep> {
        val allDeals = _deals.value
        return users.filter { it.role == UserRole.SALES_REP }.map { user ->
            val userDeals = allDeals.filter { it.assignedRepId == user.id || it.assignedRepName == user.name }
            val wonDeals = userDeals.filter { it.stage == PipelineStage.WON }
            val activeDeals = userDeals.filter { it.stage != PipelineStage.WON && it.stage != PipelineStage.LOST }
            val wonValue = wonDeals.sumOf { it.value }
            val pipelineVal = activeDeals.sumOf { it.value }
            val totalDecided = userDeals.count { it.stage == PipelineStage.WON || it.stage == PipelineStage.LOST }
            val winRate = if (totalDecided > 0) ((wonDeals.size.toDouble() / totalDecided) * 100).toInt() else 65

            TeamRep(
                id = user.id,
                name = user.name,
                title = user.title,
                initials = user.initials,
                avatarBgHex = user.avatarBgHex,
                quotaTarget = user.monthlyQuota,
                wonValue = wonValue,
                wonDealsCount = wonDeals.size,
                activeDealsCount = activeDeals.size,
                pipelineValue = pipelineVal,
                winRate = winRate,
                avgCycleDays = when (user.initials) {
                    "SC" -> 22
                    "DM" -> 28
                    else -> 25
                }
            )
        }
    }

    companion object {
        fun formatCurrency(amount: Double): String {
            val format = NumberFormat.getCurrencyInstance(Locale.US)
            format.maximumFractionDigits = 0
            return format.format(amount)
        }
    }

    private fun getInitialDeals(): List<LeadDeal> = listOf(
        LeadDeal(
            id = "deal_1",
            title = "Global Cloud Infrastructure Upgrade",
            company = "Apex Financial Technologies",
            value = 185000.0,
            stage = PipelineStage.PROPOSAL,
            probability = 75,
            expectedCloseDate = "Nov 15, 2026",
            assignedRepId = "user_rep1",
            assignedRepName = "Sarah Connor",
            assignedRepInitials = "SC",
            contactName = "Marcus Vance",
            contactEmail = "m.vance@apexfintech.com",
            contactPhone = "+1 (555) 234-8901",
            contactRole = "Chief Information Officer",
            priority = DealPriority.HIGH,
            notes = "Completed executive architecture demo. RFP finalized. Security review passed with no blockers.",
            industry = "Financial Services",
            companySize = "1,000-5,000",
            website = "apexfintech.com",
            daysInStage = 5
        ),
        LeadDeal(
            id = "deal_2",
            title = "Omnichannel Logistics Platform",
            company = "SwiftStream Global",
            value = 120000.0,
            stage = PipelineStage.QUALIFIED,
            probability = 50,
            expectedCloseDate = "Dec 02, 2026",
            assignedRepId = "user_rep2",
            assignedRepName = "David Miller",
            assignedRepInitials = "DM",
            contactName = "Elena Chen",
            contactEmail = "elena.c@swiftstream.io",
            contactPhone = "+1 (555) 492-1188",
            contactRole = "VP of Supply Chain",
            priority = DealPriority.MEDIUM,
            notes = "Budget authorized for Q4. Needs API integration specs for their legacy ERP system.",
            industry = "Logistics & Transport",
            companySize = "500-1,000",
            website = "swiftstream.io",
            daysInStage = 8
        ),
        LeadDeal(
            id = "deal_3",
            title = "Enterprise Cyber Defense Suite",
            company = "Guardian Health Systems",
            value = 240000.0,
            stage = PipelineStage.WON,
            probability = 100,
            expectedCloseDate = "Oct 18, 2026",
            assignedRepId = "user_rep1",
            assignedRepName = "Sarah Connor",
            assignedRepInitials = "SC",
            contactName = "Dr. Robert Sterling",
            contactEmail = "rsterling@guardianhealth.org",
            contactPhone = "+1 (555) 789-3321",
            contactRole = "Chief Medical Security Officer",
            priority = DealPriority.CRITICAL,
            notes = "Contract executed for 3-year term. Onboarding scheduled for Nov 1.",
            industry = "Healthcare",
            companySize = "5,000+",
            website = "guardianhealth.org",
            daysInStage = 0
        ),
        LeadDeal(
            id = "deal_4",
            title = "AI-Powered Customer Success Suite",
            company = "HyperScale SaaS",
            value = 95000.0,
            stage = PipelineStage.CONTACTED,
            probability = 25,
            expectedCloseDate = "Nov 28, 2026",
            assignedRepId = "user_rep3",
            assignedRepName = "Elena Rostova",
            assignedRepInitials = "ER",
            contactName = "Chloe Bennett",
            contactEmail = "chloe@hyperscalesaaS.com",
            contactPhone = "+1 (555) 345-9012",
            contactRole = "Head of Customer Experience",
            priority = DealPriority.MEDIUM,
            notes = "Initial discovery call conducted. Expressed high interest in automated sentiment classification.",
            industry = "Software",
            companySize = "100-250",
            website = "hyperscalesaaS.com",
            daysInStage = 3
        ),
        LeadDeal(
            id = "deal_5",
            title = "Real-time Telemetry Analytics",
            company = "Vanguard Automotive",
            value = 310000.0,
            stage = PipelineStage.PROPOSAL,
            probability = 75,
            expectedCloseDate = "Nov 30, 2026",
            assignedRepId = "user_rep3",
            assignedRepName = "Elena Rostova",
            assignedRepInitials = "ER",
            contactName = "Vikram Patel",
            contactEmail = "v.patel@vanguardauto.de",
            contactPhone = "+49 30 901820",
            contactRole = "Director of Connected Fleet",
            priority = DealPriority.CRITICAL,
            notes = "Commercial proposal under legal review. Price terms agreed in principle.",
            industry = "Automotive",
            companySize = "10,000+",
            website = "vanguardauto.com",
            daysInStage = 6
        ),
        LeadDeal(
            id = "deal_6",
            title = "Core Banking Microservices Migration",
            company = "Nordic Horizon Bank",
            value = 420000.0,
            stage = PipelineStage.QUALIFIED,
            probability = 50,
            expectedCloseDate = "Jan 15, 2027",
            assignedRepId = "user_rep2",
            assignedRepName = "David Miller",
            assignedRepInitials = "DM",
            contactName = "Lars Lindqvist",
            contactEmail = "lars.l@nordichorizon.se",
            contactPhone = "+46 8 123 4567",
            contactRole = "Head of Digital Core",
            priority = DealPriority.HIGH,
            notes = "Scope validation workshop successful. Competitor evaluated but eliminated due to compliance limits.",
            industry = "Banking",
            companySize = "2,500-5,000",
            website = "nordichorizon.se",
            daysInStage = 11
        ),
        LeadDeal(
            id = "deal_7",
            title = "Smart Retail Checkout Integration",
            company = "OmniMart Superstores",
            value = 78000.0,
            stage = PipelineStage.NEW,
            probability = 10,
            expectedCloseDate = "Dec 20, 2026",
            assignedRepId = "user_rep1",
            assignedRepName = "Sarah Connor",
            assignedRepInitials = "SC",
            contactName = "Tara Washington",
            contactEmail = "twashington@omnimart.com",
            contactPhone = "+1 (555) 671-8822",
            contactRole = "VP Retail Operations",
            priority = DealPriority.LOW,
            notes = "Inbound lead from Web Gartner Summit. Follow-up email sent.",
            industry = "Retail",
            companySize = "5,000+",
            website = "omnimart.com",
            daysInStage = 2
        ),
        LeadDeal(
            id = "deal_8",
            title = "Predictive Maintenance IoT Hub",
            company = "Titanium Heavy Machinery",
            value = 160000.0,
            stage = PipelineStage.WON,
            probability = 100,
            expectedCloseDate = "Oct 12, 2026",
            assignedRepId = "user_rep2",
            assignedRepName = "David Miller",
            assignedRepInitials = "DM",
            contactName = "Hans Gruber",
            contactEmail = "hans.g@titanium-machinery.com",
            contactPhone = "+1 (555) 890-4411",
            contactRole = "VP Manufacturing Ops",
            priority = DealPriority.HIGH,
            notes = "Fully executed and PO delivered. Pilot deployment rolling out in 4 plants.",
            industry = "Manufacturing",
            companySize = "2,000-5,000",
            website = "titanium-machinery.com",
            daysInStage = 0
        ),
        LeadDeal(
            id = "deal_9",
            title = "HR Workflow Automation",
            company = "NexGen Media Group",
            value = 45000.0,
            stage = PipelineStage.LOST,
            probability = 0,
            expectedCloseDate = "Oct 05, 2026",
            assignedRepId = "user_rep3",
            assignedRepName = "Elena Rostova",
            assignedRepInitials = "ER",
            contactName = "Jordan Riley",
            contactEmail = "jordan.r@nexgenmedia.com",
            contactPhone = "+1 (555) 321-7788",
            contactRole = "Chief People Officer",
            priority = DealPriority.LOW,
            notes = "Lost to incumbent due to enterprise HRIS bundle pricing.",
            industry = "Media & Entertainment",
            companySize = "500-1,000",
            website = "nexgenmedia.com",
            daysInStage = 14
        ),
        LeadDeal(
            id = "deal_10",
            title = "Biotech Laboratory Cloud Repository",
            company = "GeneVault Therapeutics",
            value = 210000.0,
            stage = PipelineStage.NEW,
            probability = 10,
            expectedCloseDate = "Jan 30, 2027",
            assignedRepId = "user_rep1",
            assignedRepName = "Sarah Connor",
            assignedRepInitials = "SC",
            contactName = "Dr. Sophia Morales",
            contactEmail = "smorales@genevault.com",
            contactPhone = "+1 (555) 901-4433",
            contactRole = "Head of R&D Bioinformatics",
            priority = DealPriority.HIGH,
            notes = "Referred by venture board member. Discovery workshop scheduled.",
            industry = "Biotechnology",
            companySize = "150-300",
            website = "genevault.com",
            daysInStage = 1
        )
    )

    private fun getInitialContacts(): List<Contact> = listOf(
        Contact(
            id = "c_1",
            name = "Marcus Vance",
            company = "Apex Financial Technologies",
            jobTitle = "Chief Information Officer",
            email = "m.vance@apexfintech.com",
            phone = "+1 (555) 234-8901",
            linkedDealsCount = 1,
            totalValue = 185000.0,
            status = "Active",
            location = "New York, NY",
            tags = listOf("Decision Maker", "VIP", "Tech Lead")
        ),
        Contact(
            id = "c_2",
            name = "Elena Chen",
            company = "SwiftStream Global",
            jobTitle = "VP of Supply Chain",
            email = "elena.c@swiftstream.io",
            phone = "+1 (555) 492-1188",
            linkedDealsCount = 1,
            totalValue = 120000.0,
            status = "Active",
            location = "Chicago, IL",
            tags = listOf("Budget Owner", "Enterprise")
        ),
        Contact(
            id = "c_3",
            name = "Dr. Robert Sterling",
            company = "Guardian Health Systems",
            jobTitle = "Chief Medical Security Officer",
            email = "rsterling@guardianhealth.org",
            phone = "+1 (555) 789-3321",
            linkedDealsCount = 1,
            totalValue = 240000.0,
            status = "Customer",
            location = "Boston, MA",
            tags = listOf("Champion", "Healthcare", "C-Suite")
        ),
        Contact(
            id = "c_4",
            name = "Chloe Bennett",
            company = "HyperScale SaaS",
            jobTitle = "Head of Customer Experience",
            email = "chloe@hyperscalesaaS.com",
            phone = "+1 (555) 345-9012",
            linkedDealsCount = 1,
            totalValue = 95000.0,
            status = "Lead",
            location = "San Francisco, CA",
            tags = listOf("SaaS", "Evaluator")
        ),
        Contact(
            id = "c_5",
            name = "Vikram Patel",
            company = "Vanguard Automotive",
            jobTitle = "Director of Connected Fleet",
            email = "v.patel@vanguardauto.de",
            phone = "+49 30 901820",
            linkedDealsCount = 1,
            totalValue = 310000.0,
            status = "Active",
            location = "Munich, Germany",
            tags = listOf("International", "Enterprise", "Auto")
        ),
        Contact(
            id = "c_6",
            name = "Lars Lindqvist",
            company = "Nordic Horizon Bank",
            jobTitle = "Head of Digital Core",
            email = "lars.l@nordichorizon.se",
            phone = "+46 8 123 4567",
            linkedDealsCount = 1,
            totalValue = 420000.0,
            status = "Active",
            location = "Stockholm, Sweden",
            tags = listOf("Banking", "C-Suite", "Tier 1")
        ),
        Contact(
            id = "c_7",
            name = "Tara Washington",
            company = "OmniMart Superstores",
            jobTitle = "VP Retail Operations",
            email = "twashington@omnimart.com",
            phone = "+1 (555) 671-8822",
            linkedDealsCount = 1,
            totalValue = 78000.0,
            status = "Prospect",
            location = "Atlanta, GA",
            tags = listOf("Retail", "Prospect")
        ),
        Contact(
            id = "c_8",
            name = "Dr. Sophia Morales",
            company = "GeneVault Therapeutics",
            jobTitle = "Head of R&D Bioinformatics",
            email = "smorales@genevault.com",
            phone = "+1 (555) 901-4433",
            linkedDealsCount = 1,
            totalValue = 210000.0,
            status = "Lead",
            location = "Cambridge, MA",
            tags = listOf("Biotech", "Inbound")
        )
    )

    private fun getInitialTasks(): List<TaskItem> = listOf(
        TaskItem(
            id = "task_1",
            title = "Finalize Master Service Agreement (MSA)",
            dealId = "deal_1",
            dealTitle = "Global Cloud Infrastructure Upgrade",
            company = "Apex Financial Technologies",
            dueDate = "Today, 3:00 PM",
            isOverdue = false,
            isCompleted = false,
            priority = DealPriority.CRITICAL,
            type = TaskType.REVIEW,
            assignedRepName = "Sarah Connor",
            notes = "Include multi-region SLA clause per Marcus Vance's request."
        ),
        TaskItem(
            id = "task_2",
            title = "Confirm ERP technical requirements call",
            dealId = "deal_2",
            dealTitle = "Omnichannel Logistics Platform",
            company = "SwiftStream Global",
            dueDate = "Today, 5:30 PM",
            isOverdue = false,
            isCompleted = false,
            priority = DealPriority.HIGH,
            type = TaskType.CALL,
            assignedRepName = "David Miller",
            notes = "Align with solution architect on SAP connector scope."
        ),
        TaskItem(
            id = "task_3",
            title = "Send tailored API security whitepaper",
            dealId = "deal_4",
            dealTitle = "AI-Powered Customer Success Suite",
            company = "HyperScale SaaS",
            dueDate = "Yesterday",
            isOverdue = true,
            isCompleted = false,
            priority = DealPriority.HIGH,
            type = TaskType.EMAIL,
            assignedRepName = "Elena Rostova",
            notes = "Chloe requested SOC2 Type II report and GDPR compliance."
        ),
        TaskItem(
            id = "task_4",
            title = "Quarterly Business Review Prep",
            dealId = "deal_3",
            dealTitle = "Enterprise Cyber Defense Suite",
            company = "Guardian Health Systems",
            dueDate = "Tomorrow, 10:00 AM",
            isOverdue = false,
            isCompleted = false,
            priority = DealPriority.MEDIUM,
            type = TaskType.MEETING,
            assignedRepName = "Sarah Connor",
            notes = "Present 90-day onboarding milestones to executive committee."
        ),
        TaskItem(
            id = "task_5",
            title = "Discovery sync with R&D team",
            dealId = "deal_10",
            dealTitle = "Biotech Laboratory Cloud Repository",
            company = "GeneVault Therapeutics",
            dueDate = "Oct 24, 2026",
            isOverdue = false,
            isCompleted = false,
            priority = DealPriority.HIGH,
            type = TaskType.CALL,
            assignedRepName = "Sarah Connor",
            notes = "Gather throughput requirements for genomic sequencing workloads."
        ),
        TaskItem(
            id = "task_6",
            title = "Review competitor pricing matrix",
            dealId = "deal_5",
            dealTitle = "Real-time Telemetry Analytics",
            company = "Vanguard Automotive",
            dueDate = "Yesterday",
            isOverdue = true,
            isCompleted = false,
            priority = DealPriority.MEDIUM,
            type = TaskType.REVIEW,
            assignedRepName = "Elena Rostova",
            notes = "Check fleet volume tier pricing against regional vendor."
        ),
        TaskItem(
            id = "task_7",
            title = "Execute kickoff paperwork",
            dealId = "deal_8",
            dealTitle = "Predictive Maintenance IoT Hub",
            company = "Titanium Heavy Machinery",
            dueDate = "Oct 15, 2026",
            isOverdue = false,
            isCompleted = true,
            priority = DealPriority.HIGH,
            type = TaskType.REVIEW,
            assignedRepName = "David Miller",
            notes = "Signed and sent to billing."
        )
    )

    private fun getInitialActivities(): Map<String, List<ActivityLog>> = mapOf(
        "deal_1" to listOf(
            ActivityLog(
                id = "act_101",
                dealId = "deal_1",
                type = ActivityType.MEETING,
                title = "Executive Architecture Review",
                description = "Presented cloud deployment topologies with Marcus Vance and 4 Senior Directors. Feedback was overwhelmingly favorable.",
                timestamp = "Oct 20, 2026 - 2:30 PM",
                authorName = "Sarah Connor"
            ),
            ActivityLog(
                id = "act_102",
                dealId = "deal_1",
                type = ActivityType.CALL,
                title = "Procurement Terms Clarification",
                description = "Discussed payment terms and enterprise discount tiers for a 3-year commitment.",
                timestamp = "Oct 17, 2026 - 11:00 AM",
                authorName = "Sarah Connor"
            ),
            ActivityLog(
                id = "act_103",
                dealId = "deal_1",
                type = ActivityType.NOTE,
                title = "Security Clearance Pass",
                description = "Received written sign-off from Chief Security Architect on cloud isolation standards.",
                timestamp = "Oct 15, 2026 - 4:15 PM",
                authorName = "Sarah Connor"
            )
        ),
        "deal_2" to listOf(
            ActivityLog(
                id = "act_201",
                dealId = "deal_2",
                type = ActivityType.CALL,
                title = "ERP Integration Scope",
                description = "Elena Chen confirmed SAP integration is required before go-live.",
                timestamp = "Oct 19, 2026 - 10:15 AM",
                authorName = "David Miller"
            )
        ),
        "deal_3" to listOf(
            ActivityLog(
                id = "act_301",
                dealId = "deal_3",
                type = ActivityType.MEETING,
                title = "Contract Signature Session",
                description = "Dr. Sterling and Legal officially countersigned master agreement.",
                timestamp = "Oct 18, 2026 - 3:00 PM",
                authorName = "Sarah Connor"
            )
        )
    )
}

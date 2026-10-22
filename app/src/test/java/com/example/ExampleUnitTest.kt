package com.example

import com.example.data.CrmRepository
import com.example.model.PipelineStage
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testInitialDealsAndFormatting() {
        val repo = CrmRepository()
        val deals = repo.deals.value
        assertTrue("Repository should have initial seeded enterprise deals", deals.isNotEmpty())

        val formatted = CrmRepository.formatCurrency(185000.0)
        assertTrue("Formatted currency should contain dollar sign and commas", formatted.contains("$185,000"))
    }

    @Test
    fun testUpdateDealStage() {
        val repo = CrmRepository()
        val dealId = "deal_1"
        repo.updateDealStage(dealId, PipelineStage.WON, "Sarah Connor")

        val updated = repo.deals.value.find { it.id == dealId }
        assertNotNull(updated)
        assertEquals(PipelineStage.WON, updated?.stage)
        assertEquals(100, updated?.probability)

        val activities = repo.activities.value[dealId]
        assertNotNull(activities)
        assertTrue(activities!!.any { it.title.contains("Stage updated to Won") })
    }

    @Test
    fun testTeamRepsCalculations() {
        val repo = CrmRepository()
        val teamReps = repo.getTeamReps()
        assertTrue(teamReps.isNotEmpty())
        val sarah = teamReps.find { it.name == "Sarah Connor" }
        assertNotNull(sarah)
        assertTrue(sarah!!.wonValue > 0)
    }
}

package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class PipelineStage(
    val displayName: String,
    val defaultProbability: Int,
    val order: Int
) {
    NEW("New", 10, 0),
    CONTACTED("Contacted", 25, 1),
    QUALIFIED("Qualified", 50, 2),
    PROPOSAL("Proposal", 75, 3),
    WON("Won", 100, 4),
    LOST("Lost", 0, 5);

    fun textColor(): Color = when (this) {
        NEW -> StageNewText
        CONTACTED -> StageContactedText
        QUALIFIED -> StageQualifiedText
        PROPOSAL -> StageProposalText
        WON -> StageWonText
        LOST -> StageLostText
    }

    fun backgroundColor(): Color = when (this) {
        NEW -> StageNewBg
        CONTACTED -> StageContactedBg
        QUALIFIED -> StageQualifiedBg
        PROPOSAL -> StageProposalBg
        WON -> StageWonBg
        LOST -> StageLostBg
    }

    fun borderColor(): Color = when (this) {
        NEW -> StageNewBorder
        CONTACTED -> StageContactedBorder
        QUALIFIED -> StageQualifiedBorder
        PROPOSAL -> StageProposalBorder
        WON -> StageWonBorder
        LOST -> StageLostBorder
    }

    fun barColor(): Color = when (this) {
        NEW -> StageNewBar
        CONTACTED -> StageContactedBar
        QUALIFIED -> StageQualifiedBar
        PROPOSAL -> StageProposalBar
        WON -> StageWonBar
        LOST -> StageLostBar
    }
}

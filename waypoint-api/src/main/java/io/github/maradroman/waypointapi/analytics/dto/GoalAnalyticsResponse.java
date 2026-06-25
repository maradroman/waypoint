package io.github.maradroman.waypointapi.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Goal analytics response")
public record GoalAnalyticsResponse(
        @Schema(description = "Goal UUID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID goalId,
        @Schema(description = "Total amount deposited in cents", example = "1000000")
        int totalDeposited,
        @Schema(description = "Total amount allocated to milestones in cents", example = "750000")
        int totalAllocated,
        @Schema(description = "Current wallet balance in cents", example = "250000")
        int walletBalance,
        @Schema(description = "Total cost of all milestones in cents", example = "2000000")
        int totalMilestoneCost,
        @Schema(description = "Total balance allocated to milestones in cents", example = "750000")
        int totalMilestoneBalance,
        @Schema(description = "Number of completed milestones", example = "1")
        int completedMilestones,
        @Schema(description = "Total number of milestones", example = "5")
        int totalMilestones,
        @Schema(description = "Progress percentage (0-100)", example = "37")
        int progressPercent,
        @Schema(description = "Currently active (first enabled, not completed) milestone UUID")
        UUID activeMilestoneId,
        @Schema(description = "Currently active milestone title", example = "Save $5,000")
        String activeMilestoneTitle
) {}

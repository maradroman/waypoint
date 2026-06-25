package io.github.maradroman.waypointapi.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User summary response")
public record SummaryResponse(
        @Schema(description = "Total amount saved across all goals in cents", example = "1500000")
        int totalSaved,
        @Schema(description = "Total target cost across all milestones in cents", example = "5000000")
        int totalTargets,
        @Schema(description = "Number of active goals", example = "3")
        int activeGoals,
        @Schema(description = "Number of completed milestones across all goals", example = "7")
        int completedMilestones
) {}

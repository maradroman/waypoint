package io.github.maradroman.waypointapi.milestone.dto;

import io.github.maradroman.waypointapi.milestone.model.Milestone;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Milestone response")
public record MilestoneResponse(
        @Schema(description = "Milestone UUID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "Parent goal UUID", example = "550e8400-e29b-41d4-a716-446655440001")
        UUID goalId,

        @Schema(description = "Milestone title", example = "Save $5,000")
        String title,

        @Schema(description = "Target cost in cents", example = "500000")
        Integer cost,

        @Schema(description = "Milestone details", example = "First milestone toward the car")
        String details,

        @Schema(description = "Whether milestone is enabled", example = "true")
        Boolean enabled,

        @Schema(description = "Whether milestone is completed", example = "false")
        Boolean completed,

        @Schema(description = "When milestone was completed")
        Instant completedAt,

        @Schema(description = "Sort order within goal", example = "0")
        Integer sortOrder,

        @Schema(description = "Currently allocated balance in cents", example = "250000")
        Integer balance,

        @Schema(description = "Creation timestamp") Instant createdAt,
        @Schema(description = "Last update timestamp") Instant updatedAt) {
    public static MilestoneResponse from(Milestone milestone, Integer balance) {
        return new MilestoneResponse(
                milestone.getId(),
                milestone.getGoal().getId(),
                milestone.getTitle(),
                milestone.getCost(),
                milestone.getDetails(),
                milestone.getEnabled(),
                milestone.getCompleted(),
                milestone.getCompletedAt(),
                milestone.getSortOrder(),
                balance,
                milestone.getCreatedAt(),
                milestone.getUpdatedAt());
    }
}

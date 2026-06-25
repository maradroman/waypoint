package io.github.maradroman.waypointapi.goal.dto;

import io.github.maradroman.waypointapi.goal.model.Goal;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Goal response")
public record GoalResponse(
        @Schema(description = "Goal UUID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
        @Schema(description = "Goal title", example = "Save for a new car")
        String title,
        @Schema(description = "Goal description", example = "I want to save $20,000 for a down payment on a car")
        String description,
        @Schema(description = "Icon identifier", example = "car")
        String icon,
        @Schema(description = "Sort order for display", example = "0")
        Integer sortOrder,
        @Schema(description = "Creation timestamp")
        Instant createdAt,
        @Schema(description = "Last update timestamp")
        Instant updatedAt
) {
    public static GoalResponse from(Goal goal) {
        return new GoalResponse(
                goal.getId(),
                goal.getTitle(),
                goal.getDescription(),
                goal.getIcon(),
                goal.getSortOrder(),
                goal.getCreatedAt(),
                goal.getUpdatedAt()
        );
    }
}

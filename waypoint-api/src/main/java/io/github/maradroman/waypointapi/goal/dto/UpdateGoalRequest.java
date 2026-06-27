package io.github.maradroman.waypointapi.goal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Update goal request")
public record UpdateGoalRequest(
        @Size(max = 120) @Schema(description = "New title", example = "Save for a new car")
        String title,

        @Size(max = 10000)
        @Schema(description = "New description", example = "Updated description for my car savings goal")
        String description,

        @Size(max = 32) @Schema(description = "New icon identifier", example = "car")
        String icon) {}

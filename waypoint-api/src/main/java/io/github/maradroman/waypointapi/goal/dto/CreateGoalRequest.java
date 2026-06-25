package io.github.maradroman.waypointapi.goal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Create goal request")
public record CreateGoalRequest(
        @NotBlank @Size(max = 120)
        @Schema(description = "Goal title", example = "Save for a new car")
        String title,
        @Size(max = 10000)
        @Schema(description = "Optional description", example = "I want to save $20,000 for a down payment on a car")
        String description,
        @Size(max = 32)
        @Schema(description = "Icon identifier", example = "car")
        String icon
) {}

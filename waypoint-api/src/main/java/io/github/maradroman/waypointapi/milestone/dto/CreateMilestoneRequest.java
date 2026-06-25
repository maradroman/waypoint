package io.github.maradroman.waypointapi.milestone.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Schema(description = "Create milestone request")
public record CreateMilestoneRequest(
        @NotBlank @Size(max = 120)
        @Schema(description = "Milestone title", example = "Save $5,000")
        String title,
        @PositiveOrZero
        @Schema(description = "Target cost in cents", example = "500000")
        Integer cost,
        @Size(max = 10000)
        @Schema(description = "Optional details", example = "First milestone toward the car")
        String details,
        @Schema(description = "Whether milestone is enabled", example = "true")
        Boolean enabled
) {}

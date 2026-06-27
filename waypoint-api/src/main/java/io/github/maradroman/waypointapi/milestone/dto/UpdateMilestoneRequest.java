package io.github.maradroman.waypointapi.milestone.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Schema(description = "Update milestone request")
public record UpdateMilestoneRequest(
        @Size(max = 120) @Schema(description = "New title", example = "Save $10,000")
        String title,

        @PositiveOrZero @Schema(description = "New cost in cents", example = "1000000")
        Integer cost,

        @Size(max = 10000) @Schema(description = "New details", example = "Updated milestone details")
        String details,

        @Schema(description = "Whether milestone is enabled", example = "true")
        Boolean enabled) {}

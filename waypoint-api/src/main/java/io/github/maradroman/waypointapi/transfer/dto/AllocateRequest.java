package io.github.maradroman.waypointapi.transfer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

@Schema(description = "Allocate funds request")
public record AllocateRequest(
        @NotNull @Schema(description = "Target milestone UUID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID milestoneId,

        @Positive @Schema(description = "Amount to allocate in cents", example = "50000")
        int amount) {}

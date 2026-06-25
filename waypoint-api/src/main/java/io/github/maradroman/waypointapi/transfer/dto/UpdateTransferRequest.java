package io.github.maradroman.waypointapi.transfer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

@Schema(description = "Update transfer request")
public record UpdateTransferRequest(
        @Positive
        @Schema(description = "New amount in cents", example = "75000")
        int amount
) {}

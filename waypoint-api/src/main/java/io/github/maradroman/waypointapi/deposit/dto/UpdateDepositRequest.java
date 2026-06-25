package io.github.maradroman.waypointapi.deposit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

@Schema(description = "Update deposit request")
public record UpdateDepositRequest(
        @Positive
        @Schema(description = "New amount in cents", example = "150000")
        Integer amount
) {}

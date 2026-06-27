package io.github.maradroman.waypointapi.deposit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

@Schema(description = "Create deposit request")
public record CreateDepositRequest(
        @Positive @Schema(description = "Deposit amount in cents", example = "100000")
        Integer amount,

        @Schema(description = "Optional note", example = "January salary")
        String note) {}

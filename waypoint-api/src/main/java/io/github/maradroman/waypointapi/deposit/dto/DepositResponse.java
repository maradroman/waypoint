package io.github.maradroman.waypointapi.deposit.dto;

import io.github.maradroman.waypointapi.deposit.model.Deposit;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Deposit response")
public record DepositResponse(
        @Schema(description = "Deposit UUID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "Parent goal UUID", example = "550e8400-e29b-41d4-a716-446655440001")
        UUID goalId,

        @Schema(description = "Deposit amount in cents", example = "100000")
        Integer amount,

        @Schema(description = "Optional note", example = "January salary")
        String note,

        @Schema(description = "Transaction timestamp") Instant timestamp,
        @Schema(description = "Creation timestamp") Instant createdAt) {
    public static DepositResponse from(Deposit deposit) {
        return new DepositResponse(
                deposit.getId(),
                deposit.getGoal().getId(),
                deposit.getAmount(),
                deposit.getNote(),
                deposit.getTimestamp(),
                deposit.getCreatedAt());
    }
}

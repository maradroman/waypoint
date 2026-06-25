package io.github.maradroman.waypointapi.transfer.dto;

import io.github.maradroman.waypointapi.transfer.model.Transfer;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Transfer response")
public record TransferResponse(
        @Schema(description = "Transfer UUID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
        @Schema(description = "Parent goal UUID", example = "550e8400-e29b-41d4-a716-446655440001")
        UUID goalId,
        @Schema(description = "Target milestone UUID", example = "550e8400-e29b-41d4-a716-446655440002")
        UUID milestoneId,
        @Schema(description = "Amount in cents (positive for allocate, negative for withdraw)", example = "50000")
        Integer amount,
        @Schema(description = "Transfer type: allocate or withdraw", example = "allocate")
        String type,
        @Schema(description = "Optional comment", example = "Monthly allocation")
        String comment,
        @Schema(description = "Transaction timestamp")
        Instant timestamp,
        @Schema(description = "Creation timestamp")
        Instant createdAt
) {
    public static TransferResponse from(Transfer transfer) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getGoal().getId(),
                transfer.getMilestone().getId(),
                transfer.getAmount(),
                transfer.getType(),
                transfer.getComment(),
                transfer.getTimestamp(),
                transfer.getCreatedAt()
        );
    }
}

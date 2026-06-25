package io.github.maradroman.waypointapi.completion.dto;

import io.github.maradroman.waypointapi.completion.model.Completion;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Completion response")
public record CompletionResponse(
        @Schema(description = "Completion UUID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
        @Schema(description = "Parent goal UUID", example = "550e8400-e29b-41d4-a716-446655440001")
        UUID goalId,
        @Schema(description = "Completed milestone UUID", example = "550e8400-e29b-41d4-a716-446655440002")
        UUID milestoneId,
        @Schema(description = "Allocated balance at completion time in cents", example = "500000")
        Integer amount,
        @Schema(description = "Completion timestamp")
        Instant timestamp,
        @Schema(description = "Creation timestamp")
        Instant createdAt
) {
    public static CompletionResponse from(Completion completion) {
        return new CompletionResponse(
                completion.getId(),
                completion.getGoal().getId(),
                completion.getMilestone().getId(),
                completion.getAmount(),
                completion.getTimestamp(),
                completion.getCreatedAt()
        );
    }
}

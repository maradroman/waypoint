package io.github.maradroman.waypointapi.plannedfund.dto;

import io.github.maradroman.waypointapi.plannedfund.model.PlannedFund;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Planned fund response")
public record PlannedFundResponse(
        @Schema(description = "Planned fund UUID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "Parent goal UUID", example = "550e8400-e29b-41d4-a716-446655440001")
        UUID goalId,

        @Schema(description = "Date in YYYY-MM-DD format", example = "2026-07-15")
        String date,

        @Schema(description = "Planned amount in cents", example = "50000")
        Integer amount) {
    public static PlannedFundResponse from(PlannedFund plannedFund) {
        return new PlannedFundResponse(
                plannedFund.getId(),
                plannedFund.getGoal().getId(),
                plannedFund.getDate().toString(),
                plannedFund.getAmount());
    }
}

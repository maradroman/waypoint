package io.github.maradroman.waypointapi.plannedfund.dto;

import io.github.maradroman.waypointapi.plannedfund.validation.FutureOrPresentDate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

@Schema(description = "Upsert planned fund request")
public record UpsertPlannedFundRequest(
        @NotBlank
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date must be in YYYY-MM-DD format")
        @FutureOrPresentDate
        @Schema(description = "Date in YYYY-MM-DD format (must be today or in the future)", example = "2026-07-15")
        String date,
        @Positive
        @Schema(description = "Planned amount in cents", example = "50000")
        Integer amount
) {}

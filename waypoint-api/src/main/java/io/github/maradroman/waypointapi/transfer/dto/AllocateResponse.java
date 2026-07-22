package io.github.maradroman.waypointapi.transfer.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Allocation result")
public record AllocateResponse(
        @Schema(description = "Amount actually applied", example = "50000")
        int applied,

        @Schema(description = "Amount originally requested", example = "100000")
        int requested) {}

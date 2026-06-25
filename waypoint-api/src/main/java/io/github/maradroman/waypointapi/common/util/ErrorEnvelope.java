package io.github.maradroman.waypointapi.common.util;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonPropertyOrder({"error"})
@Schema(description = "Standard API error response envelope")
public record ErrorEnvelope(
        @Schema(description = "Error details")
        ErrorDetails error
) {
    public static ErrorEnvelope of(String code, String message, Object details) {
        return new ErrorEnvelope(new ErrorDetails(code, message, details));
    }

    @JsonPropertyOrder({"code", "message", "details"})
    @Schema(description = "Error details")
    public record ErrorDetails(
            @Schema(description = "Error code", example = "VALIDATION_ERROR")
            String code,
            @Schema(description = "Human-readable error message", example = "Request validation failed")
            String message,
            @Schema(description = "Additional error details")
            Object details
    ) {}
}

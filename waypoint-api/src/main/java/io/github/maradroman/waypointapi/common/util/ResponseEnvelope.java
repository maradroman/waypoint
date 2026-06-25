package io.github.maradroman.waypointapi.common.util;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@JsonPropertyOrder({"data", "meta"})
@Schema(description = "Standard API success response envelope")
public record ResponseEnvelope<T>(
        @Schema(description = "Response payload")
        T data,
        @Schema(description = "Response metadata")
        Meta meta
) {
    public static <T> ResponseEnvelope<T> of(T data) {
        return new ResponseEnvelope<>(data, Meta.now());
    }

    @JsonPropertyOrder({"requestId", "timestamp"})
    @Schema(description = "Response metadata")
    public record Meta(
            @Schema(description = "Unique request identifier", example = "550e8400-e29b-41d4-a716-446655440000")
            UUID requestId,
            @Schema(description = "Response timestamp")
            Instant timestamp
    ) {
        static Meta now() {
            return new Meta(UUID.randomUUID(), Instant.now());
        }
    }
}

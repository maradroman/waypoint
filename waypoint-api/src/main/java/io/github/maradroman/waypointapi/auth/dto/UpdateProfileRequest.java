package io.github.maradroman.waypointapi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Profile update request")
public record UpdateProfileRequest(
        @Size(max = 120)
        @Schema(description = "New display name", example = "John Doe")
        String displayName,
        @Size(min = 2, max = 5)
        @Schema(description = "Locale code (BCP 47)", example = "en")
        String locale,
        @Size(min = 2, max = 3)
        @Schema(description = "Currency code (ISO 4217)", example = "USD")
        String currency,
        @Size(min = 4, max = 5)
        @Schema(description = "Theme preference", example = "light", allowableValues = {"light", "dark"})
        String theme
) {}

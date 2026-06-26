package io.github.maradroman.waypointapi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication response containing JWT tokens and user profile")
public record AuthResponse(
        @Schema(description = "JWT access token (15 min expiry)", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,
        @Schema(description = "JWT refresh token (30 day expiry)", example = "eyJhbGciOiJIUzI1NiJ9...")
        String refreshToken,
        @Schema(description = "Authenticated user profile")
        UserProfile user
) {
    @Schema(description = "User profile information")
    public record UserProfile(
            @Schema(description = "User UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            String id,
            @Schema(description = "Email address", example = "user@example.com")
            String email,
            @Schema(description = "Display name", example = "John Doe")
            String displayName,
            @Schema(description = "Locale code", example = "en")
            String locale,
            @Schema(description = "Currency code", example = "USD")
            String currency,
            @Schema(description = "User role", example = "USER", allowableValues = {"USER", "ADMIN"})
            String role
    ) {}
}

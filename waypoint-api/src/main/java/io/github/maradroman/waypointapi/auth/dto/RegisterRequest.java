package io.github.maradroman.waypointapi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Registration request")
public record RegisterRequest(
        @Email @NotBlank @Schema(description = "Email address", example = "user@example.com")
        String email,

        @NotBlank
        @Size(min = 6, max = 128)
        @Schema(description = "Password (min 6 characters)", example = "securePassword123")
        String password,

        @Size(max = 120) @Schema(description = "Display name", example = "John Doe")
        String displayName) {}

package io.github.maradroman.waypointapi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Login credentials")
public record LoginRequest(
        @Email @NotBlank @Schema(description = "Email address", example = "user@example.com")
        String email,

        @NotBlank @Schema(description = "Password", example = "securePassword123")
        String password) {}

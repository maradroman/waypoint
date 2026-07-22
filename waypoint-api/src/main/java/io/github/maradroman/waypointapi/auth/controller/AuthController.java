package io.github.maradroman.waypointapi.auth.controller;

import io.github.maradroman.waypointapi.auth.dto.AuthResponse;
import io.github.maradroman.waypointapi.auth.dto.LoginRequest;
import io.github.maradroman.waypointapi.auth.dto.RefreshTokenRequest;
import io.github.maradroman.waypointapi.auth.dto.RegisterRequest;
import io.github.maradroman.waypointapi.auth.dto.UpdateProfileRequest;
import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.auth.service.AuthService;
import io.github.maradroman.waypointapi.common.security.CurrentUser;
import io.github.maradroman.waypointapi.common.util.ResponseEnvelope;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login, refresh tokens, and manage profile")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account and returns JWT tokens")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ResponseEntity<ResponseEnvelope<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseEnvelope.of(authService.register(request)));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate with email and password, returns JWT tokens")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<ResponseEnvelope<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ResponseEnvelope.of(authService.login(request)));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh tokens", description = "Exchange a refresh token for a new access/refresh token pair")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tokens refreshed"),
        @ApiResponse(responseCode = "400", description = "Invalid or expired refresh token")
    })
    public ResponseEntity<ResponseEnvelope<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ResponseEnvelope.of(authService.refresh(request)));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Invalidate all refresh tokens for the current user")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Logged out successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> logout(@CurrentUser User user) {
        authService.logout(user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Returns the authenticated user's profile")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile retrieved"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ResponseEnvelope<AuthResponse.UserProfile>> me(@CurrentUser User user) {
        return ResponseEntity.ok(ResponseEnvelope.of(authService.getProfile(user)));
    }

    @PatchMapping("/me")
    @Operation(summary = "Update profile", description = "Update display name, locale, or currency")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile updated"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ResponseEnvelope<AuthResponse.UserProfile>> updateMe(
            @CurrentUser User user, @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ResponseEnvelope.of(authService.updateProfile(user, request)));
    }
}

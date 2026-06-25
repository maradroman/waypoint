package io.github.maradroman.waypointapi.auth.service;

import io.github.maradroman.waypointapi.auth.dto.AuthResponse;
import io.github.maradroman.waypointapi.auth.dto.LoginRequest;
import io.github.maradroman.waypointapi.auth.dto.RefreshTokenRequest;
import io.github.maradroman.waypointapi.auth.dto.RegisterRequest;
import io.github.maradroman.waypointapi.auth.dto.UpdateProfileRequest;
import io.github.maradroman.waypointapi.auth.model.RefreshToken;
import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.auth.repository.RefreshTokenRepository;
import io.github.maradroman.waypointapi.auth.repository.UserRepository;
import io.github.maradroman.waypointapi.common.exception.BadRequestException;
import io.github.maradroman.waypointapi.common.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("EMAIL_EXISTS", "Email already registered");
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .displayName(request.displayName())
                .build();
        user = userRepository.save(user);

        return generateAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        return generateAuthResponse(user);
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new BadRequestException("INVALID_REFRESH_TOKEN", "Invalid refresh token"));

        if (storedToken.isExpired()) {
            refreshTokenRepository.delete(storedToken);
            throw new BadRequestException("REFRESH_TOKEN_EXPIRED", "Refresh token has expired");
        }

        User user = storedToken.getUser();
        refreshTokenRepository.delete(storedToken);

        return generateAuthResponse(user);
    }

    @Transactional
    public void logout(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    public AuthResponse.UserProfile getProfile(User user) {
        return toProfile(user);
    }

    public AuthResponse.UserProfile updateProfile(User user, UpdateProfileRequest request) {
        if (request.displayName() != null) {
            user.setDisplayName(request.displayName());
        }
        if (request.locale() != null) {
            user.setLocale(request.locale());
        }
        if (request.currency() != null) {
            user.setCurrency(request.currency());
        }
        user = userRepository.save(user);
        return toProfile(user);
    }

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user.getId());
        String rawRefreshToken = jwtService.generateRefreshToken();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(rawRefreshToken)
                .expiresAt(Instant.now().plusMillis(jwtService.getRefreshTokenExpiration()))
                .build();
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(accessToken, rawRefreshToken, toProfile(user));
    }

    private AuthResponse.UserProfile toProfile(User user) {
        return new AuthResponse.UserProfile(
                user.getId().toString(),
                user.getEmail(),
                user.getDisplayName(),
                user.getLocale(),
                user.getCurrency()
        );
    }
}

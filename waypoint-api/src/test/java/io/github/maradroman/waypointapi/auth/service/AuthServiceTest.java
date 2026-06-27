package io.github.maradroman.waypointapi.auth.service;

import io.github.maradroman.waypointapi.auth.dto.AuthResponse;
import io.github.maradroman.waypointapi.auth.dto.UpdateProfileRequest;
import io.github.maradroman.waypointapi.auth.model.RefreshToken;
import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.auth.repository.RefreshTokenRepository;
import io.github.maradroman.waypointapi.auth.repository.UserRepository;
import io.github.maradroman.waypointapi.common.exception.BadRequestException;
import io.github.maradroman.waypointapi.common.exception.DuplicateResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static io.github.maradroman.waypointapi.testdata.TestDataAuthDto.*;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.*;
import static io.github.maradroman.waypointapi.testdata.TestDataRefreshTokenEntity.buildExpiredRefreshToken;
import static io.github.maradroman.waypointapi.testdata.TestDataRefreshTokenEntity.buildRefreshToken;
import static io.github.maradroman.waypointapi.testdata.TestDataUserEntity.buildUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    @InjectMocks private AuthService authService;

    private User user;

    @Captor private ArgumentCaptor<User> userCaptor;
    @Captor private ArgumentCaptor<RefreshToken> refreshTokenCaptor;

    @BeforeEach
    void setUp() {
        user = buildUser();
    }

    @Nested
    @DisplayName("Register")
    class Register {

        @Test
        @DisplayName("registers new user and returns auth response")
        void register_registersNewUserAndReturnsAuthResponseTest() {
            var request = registerRequest();

            when(userRepository.existsByEmail(request.email())).thenReturn(false);
            when(passwordEncoder.encode(request.password())).thenReturn("encoded");
            when(userRepository.save(userCaptor.capture())).thenReturn(user);
            when(jwtService.generateAccessToken(user.getId(), user.getRole())).thenReturn("access-token");
            when(jwtService.generateRefreshToken()).thenReturn("refresh-token");
            when(jwtService.getRefreshTokenExpiration()).thenReturn(2592000000L);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

            var actualResult = authService.register(request);

            assertThat(userCaptor.getValue())
                .extracting(User::getEmail, User::getPasswordHash, User::getDisplayName)
                .containsExactly(request.email(), "encoded", request.displayName());
            assertThat(actualResult)
                .extracting(AuthResponse::accessToken, AuthResponse::refreshToken)
                .containsExactly("access-token", "refresh-token");
            assertThat(actualResult.user())
                .extracting(AuthResponse.UserProfile::id, AuthResponse.UserProfile::email, AuthResponse.UserProfile::displayName, AuthResponse.UserProfile::role)
                .containsExactly(USER_ID.toString(), USER_EMAIL, USER_DISPLAY_NAME, USER_ROLE);
        }

        @Test
        @DisplayName("throws DuplicateResourceException when email exists")
        void register_throwsDuplicateResourceException_whenEmailExistsTest() {
            var request = registerRequest();

            when(userRepository.existsByEmail(request.email())).thenReturn(true);

            assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasFieldOrPropertyWithValue("code", "EMAIL_EXISTS");
        }
    }

    @Nested
    @DisplayName("Login")
    class Login {

        @Test
        @DisplayName("logs in with valid credentials")
        void login_logsInWithValidCredentialsTest() {
            var request = loginRequest();

            when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(true);
            when(jwtService.generateAccessToken(user.getId(), user.getRole())).thenReturn("access-token");
            when(jwtService.generateRefreshToken()).thenReturn("refresh-token");
            when(jwtService.getRefreshTokenExpiration()).thenReturn(2592000000L);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

            var actualResult = authService.login(request);

            assertThat(actualResult)
                .extracting(AuthResponse::accessToken, AuthResponse::refreshToken)
                .containsExactly("access-token", "refresh-token");
        }

        @Test
        @DisplayName("throws BadCredentialsException when email not found")
        void login_throwsBadCredentialsException_whenEmailNotFoundTest() {
            var request = loginRequest();

            when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
        }

        @Test
        @DisplayName("throws BadCredentialsException when password wrong")
        void login_throwsBadCredentialsException_whenPasswordWrongTest() {
            var request = loginRequest();

            when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(false);

            assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
        }
    }

    @Nested
    @DisplayName("Refresh")
    class Refresh {

        @Test
        @DisplayName("refreshes tokens with valid token")
        void refresh_refreshesTokensWithValidTokenTest() {
            var request = refreshTokenRequest();
            var refreshToken = buildRefreshToken(user);

            when(refreshTokenRepository.findByToken(request.refreshToken())).thenReturn(Optional.of(refreshToken));
            when(refreshTokenRepository.deleteByToken(request.refreshToken())).thenReturn(1);
            when(jwtService.generateAccessToken(user.getId(), user.getRole())).thenReturn("new-access-token");
            when(jwtService.generateRefreshToken()).thenReturn("new-refresh-token");
            when(jwtService.getRefreshTokenExpiration()).thenReturn(2592000000L);
            when(refreshTokenRepository.save(refreshTokenCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

            var actualResult = authService.refresh(request);

            verify(refreshTokenRepository).deleteByToken(request.refreshToken());
            assertThat(actualResult)
                .extracting(AuthResponse::accessToken, AuthResponse::refreshToken)
                .containsExactly("new-access-token", "new-refresh-token");
            assertThat(refreshTokenCaptor.getValue().getUser()).isEqualTo(user);
        }

        @Test
        @DisplayName("throws BadRequestException when token not found")
        void refresh_throwsBadRequestException_whenTokenNotFoundTest() {
            var request = refreshTokenRequest();

            when(refreshTokenRepository.findByToken(request.refreshToken())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", "INVALID_REFRESH_TOKEN");
        }

        @Test
        @DisplayName("throws BadRequestException when token expired")
        void refresh_throwsBadRequestException_whenTokenExpiredTest() {
            var request = refreshTokenRequest(REFRESH_TOKEN_VALUE_2);
            var expiredToken = buildExpiredRefreshToken(user);

            when(refreshTokenRepository.findByToken(request.refreshToken())).thenReturn(Optional.of(expiredToken));
            when(refreshTokenRepository.deleteByToken(request.refreshToken())).thenReturn(1);

            assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", "REFRESH_TOKEN_EXPIRED");
            verify(refreshTokenRepository).deleteByToken(request.refreshToken());
        }
    }

    @Nested
    @DisplayName("Logout")
    class Logout {

        @Test
        @DisplayName("deletes all refresh tokens for user")
        void logout_deletesAllRefreshTokensForUserTest() {
            authService.logout(user);

            verify(refreshTokenRepository).deleteByUser(user);
        }
    }

    @Nested
    @DisplayName("GetProfile")
    class GetProfile {

        @Test
        @DisplayName("returns user profile")
        void getProfile_returnsUserProfileTest() {
            var actualResult = authService.getProfile(user);

            assertThat(actualResult)
                .extracting(AuthResponse.UserProfile::id, AuthResponse.UserProfile::email, AuthResponse.UserProfile::displayName, AuthResponse.UserProfile::locale, AuthResponse.UserProfile::currency)
                .containsExactly(USER_ID.toString(), USER_EMAIL, USER_DISPLAY_NAME, USER_LOCALE, USER_CURRENCY);
        }
    }

    @Nested
    @DisplayName("UpdateProfile")
    class UpdateProfile {

        @Test
        @DisplayName("updates displayName, locale, currency selectively")
        void updateProfile_updatesDisplayNameLocaleCurrencySelectivelyTest() {
            var request = new UpdateProfileRequest("New Name", null, null, null);

            when(userRepository.save(user)).thenReturn(user);

            var actualResult = authService.updateProfile(user, request);

            assertThat(actualResult)
                .extracting(AuthResponse.UserProfile::displayName, AuthResponse.UserProfile::locale, AuthResponse.UserProfile::currency)
                .containsExactly("New Name", USER_LOCALE, USER_CURRENCY);
            assertThat(user.getDisplayName()).isEqualTo("New Name");
            assertThat(user.getLocale()).isEqualTo(USER_LOCALE);
            assertThat(user.getCurrency()).isEqualTo(USER_CURRENCY);
        }

        @Test
        @DisplayName("updates locale selectively")
        void updateProfile_updatesLocaleSelectivelyTest() {
            var request = new UpdateProfileRequest(null, "fr", null, null);

            when(userRepository.save(user)).thenReturn(user);

            var actualResult = authService.updateProfile(user, request);

            assertThat(actualResult.locale()).isEqualTo("fr");
            assertThat(actualResult.displayName()).isEqualTo(USER_DISPLAY_NAME);
            assertThat(actualResult.currency()).isEqualTo(USER_CURRENCY);
            assertThat(user.getLocale()).isEqualTo("fr");
        }

        @Test
        @DisplayName("updates currency selectively")
        void updateProfile_updatesCurrencySelectivelyTest() {
            var request = new UpdateProfileRequest(null, null, "EUR", null);

            when(userRepository.save(user)).thenReturn(user);

            var actualResult = authService.updateProfile(user, request);

            assertThat(actualResult.currency()).isEqualTo("EUR");
            assertThat(user.getCurrency()).isEqualTo("EUR");
        }
    }
}

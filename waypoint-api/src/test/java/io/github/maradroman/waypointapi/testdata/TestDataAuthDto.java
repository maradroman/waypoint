package io.github.maradroman.waypointapi.testdata;

import io.github.maradroman.waypointapi.auth.dto.AuthResponse;
import io.github.maradroman.waypointapi.auth.dto.LoginRequest;
import io.github.maradroman.waypointapi.auth.dto.RefreshTokenRequest;
import io.github.maradroman.waypointapi.auth.dto.RegisterRequest;
import io.github.maradroman.waypointapi.auth.dto.UpdateProfileRequest;
import lombok.experimental.UtilityClass;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.*;

@UtilityClass
public class TestDataAuthDto {

    public static RegisterRequest registerRequest() {
        return new RegisterRequest(USER_EMAIL, "password123", USER_DISPLAY_NAME);
    }

    public static LoginRequest loginRequest() {
        return new LoginRequest(USER_EMAIL, "password123");
    }

    public static RefreshTokenRequest refreshTokenRequest() {
        return new RefreshTokenRequest(REFRESH_TOKEN_VALUE);
    }

    public static RefreshTokenRequest refreshTokenRequest(String token) {
        return new RefreshTokenRequest(token);
    }

    public static UpdateProfileRequest updateProfileRequest(String displayName) {
        return new UpdateProfileRequest(displayName, USER_LOCALE, USER_CURRENCY, USER_THEME);
    }

    public static AuthResponse authResponse() {
        return new AuthResponse("access-token", "refresh-token",
                new AuthResponse.UserProfile(USER_ID.toString(), USER_EMAIL, USER_DISPLAY_NAME, USER_LOCALE, USER_CURRENCY, USER_THEME, USER_ROLE));
    }
}

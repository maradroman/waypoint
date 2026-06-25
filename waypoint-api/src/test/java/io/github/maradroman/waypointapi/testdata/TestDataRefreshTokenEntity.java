package io.github.maradroman.waypointapi.testdata;

import io.github.maradroman.waypointapi.auth.model.RefreshToken;
import io.github.maradroman.waypointapi.auth.model.User;
import lombok.experimental.UtilityClass;

import java.time.Instant;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.*;

@UtilityClass
public class TestDataRefreshTokenEntity {

    public static RefreshToken buildRefreshToken(User user) {
        return RefreshToken.builder()
                .user(user)
                .token(REFRESH_TOKEN_VALUE)
                .expiresAt(Instant.now().plusSeconds(2592000))
                .build();
    }

    public static RefreshToken buildExpiredRefreshToken(User user) {
        return RefreshToken.builder()
                .user(user)
                .token(REFRESH_TOKEN_VALUE_2)
                .expiresAt(Instant.now().minusSeconds(3600))
                .build();
    }
}

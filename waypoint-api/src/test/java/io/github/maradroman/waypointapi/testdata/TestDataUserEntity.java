package io.github.maradroman.waypointapi.testdata;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.*;

import io.github.maradroman.waypointapi.auth.model.User;
import java.util.UUID;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TestDataUserEntity {

    public static User buildUser() {
        return User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .passwordHash(USER_PASSWORD_HASH)
                .displayName(USER_DISPLAY_NAME)
                .locale(USER_LOCALE)
                .currency(USER_CURRENCY)
                .role(USER_ROLE)
                .build();
    }

    public static User buildUser(UUID id) {
        return User.builder()
                .id(id)
                .email(id == USER_ID ? USER_EMAIL : USER_EMAIL_2)
                .passwordHash(USER_PASSWORD_HASH)
                .displayName(USER_DISPLAY_NAME)
                .locale(USER_LOCALE)
                .currency(USER_CURRENCY)
                .role(USER_ROLE)
                .build();
    }

    public static User buildUserWithEmail(String email) {
        return User.builder()
                .id(USER_ID)
                .email(email)
                .passwordHash(USER_PASSWORD_HASH)
                .displayName(USER_DISPLAY_NAME)
                .locale(USER_LOCALE)
                .currency(USER_CURRENCY)
                .role(USER_ROLE)
                .build();
    }

    public static User buildAdmin() {
        return User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .passwordHash(USER_PASSWORD_HASH)
                .displayName(USER_DISPLAY_NAME)
                .locale(USER_LOCALE)
                .currency(USER_CURRENCY)
                .role(USER_ROLE_ADMIN)
                .build();
    }
}

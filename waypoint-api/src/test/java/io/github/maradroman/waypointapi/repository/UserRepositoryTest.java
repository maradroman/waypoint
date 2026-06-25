package io.github.maradroman.waypointapi.repository;

import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.auth.repository.UserRepository;
import io.github.maradroman.waypointapi.testdata.TestDataJpa;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.USER_DISPLAY_NAME;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.USER_EMAIL;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.USER_EMAIL_2;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryTest extends TestDataJpa {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("findByEmail returns empty when not exists")
    void findByEmail_returnsEmpty_whenNotExistsTest() {
        var actualResult = userRepository.findByEmail(USER_EMAIL_2);

        assertThat(actualResult).isEmpty();
    }

    @Test
    @DisplayName("findByEmail returns user when exists")
    void findByEmail_returnsUser_whenExistsTest() {
        persistUser(USER_ID);
        flushAndClear();

        var actualResult = userRepository.findByEmail(USER_EMAIL);

        assertThat(actualResult)
                .isPresent()
                .get()
                .extracting(User::getId, User::getEmail, User::getDisplayName)
                .containsExactly(USER_ID, USER_EMAIL, USER_DISPLAY_NAME);
    }

    @Test
    @DisplayName("existsByEmail returns false when not exists")
    void existsByEmail_returnsFalse_whenNotExistsTest() {
        var actualResult = userRepository.existsByEmail(USER_EMAIL_2);

        assertThat(actualResult).isFalse();
    }

    @Test
    @DisplayName("existsByEmail returns true when exists")
    void existsByEmail_returnsTrue_whenExistsTest() {
        persistUser(USER_ID);
        flushAndClear();

        var actualResult = userRepository.existsByEmail(USER_EMAIL);

        assertThat(actualResult).isTrue();
    }
}

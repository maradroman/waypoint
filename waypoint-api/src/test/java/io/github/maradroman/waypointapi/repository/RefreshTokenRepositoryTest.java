package io.github.maradroman.waypointapi.repository;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.REFRESH_TOKEN_VALUE;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.REFRESH_TOKEN_VALUE_2;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.maradroman.waypointapi.auth.model.RefreshToken;
import io.github.maradroman.waypointapi.auth.repository.RefreshTokenRepository;
import io.github.maradroman.waypointapi.testdata.TestDataJpa;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

class RefreshTokenRepositoryTest extends TestDataJpa {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    @DisplayName("findByToken returns empty when not exists")
    void findByToken_returnsEmpty_whenNotExistsTest() {
        var actualResult = refreshTokenRepository.findByToken("non-existent-token");

        assertThat(actualResult).isEmpty();
    }

    @Test
    @DisplayName("findByToken returns token when exists")
    void findByToken_returnsToken_whenExistsTest() {
        var user = persistUser(USER_ID);
        var rtId = UUID.randomUUID();
        jdbcTemplate.update(
                """
                INSERT INTO refresh_tokens (id, user_id, token, expires_at, created_at)
                VALUES (?, ?, ?, ?, ?)
                """,
                rtId,
                user.getId(),
                REFRESH_TOKEN_VALUE,
                java.time.Instant.now().plusSeconds(2592000),
                java.time.Instant.now());
        flushAndClear();

        var actualResult = refreshTokenRepository.findByToken(REFRESH_TOKEN_VALUE);

        assertThat(actualResult)
                .isPresent()
                .get()
                .extracting(RefreshToken::getToken)
                .isEqualTo(REFRESH_TOKEN_VALUE);
    }

    @Test
    @Transactional
    @DisplayName("deleteByUser removes all tokens for user")
    void deleteByUser_removesAllTokensForUserTest() {
        var user = persistUser(USER_ID);
        var now = java.time.Instant.now();
        jdbcTemplate.update("""
                INSERT INTO refresh_tokens (id, user_id, token, expires_at, created_at)
                VALUES (?, ?, ?, ?, ?)
                """, UUID.randomUUID(), user.getId(), REFRESH_TOKEN_VALUE, now.plusSeconds(2592000), now);
        jdbcTemplate.update("""
                INSERT INTO refresh_tokens (id, user_id, token, expires_at, created_at)
                VALUES (?, ?, ?, ?, ?)
                """, UUID.randomUUID(), user.getId(), REFRESH_TOKEN_VALUE_2, now.minusSeconds(3600), now);
        em.flush();
        em.clear();

        jdbcTemplate.update("DELETE FROM refresh_tokens WHERE user_id = ?", user.getId());
        em.clear();

        var actualResult = refreshTokenRepository.findByToken(REFRESH_TOKEN_VALUE);
        assertThat(actualResult).isEmpty();

        var actualResult2 = refreshTokenRepository.findByToken(REFRESH_TOKEN_VALUE_2);
        assertThat(actualResult2).isEmpty();
    }
}

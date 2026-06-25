package io.github.maradroman.waypointapi.auth.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String JWT_SECRET = "my-super-secret-key-for-testing-purposes-only-1234567890";
    private static final long ACCESS_TOKEN_EXPIRATION = 900000;
    private static final long REFRESH_TOKEN_EXPIRATION = 2592000000L;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(JWT_SECRET, ACCESS_TOKEN_EXPIRATION, REFRESH_TOKEN_EXPIRATION);
    }

    @Test
    @DisplayName("generateAccessToken creates valid JWT")
    void generateAccessToken_createsValidJwtTest() {
        var actualResult = jwtService.generateAccessToken(USER_ID);

        assertThat(actualResult).isNotBlank();
        var extractedUserId = jwtService.validateAndExtractUserId(actualResult);
        assertThat(extractedUserId).isEqualTo(USER_ID);
    }

    @Test
    @DisplayName("generateRefreshToken creates valid JWT")
    void generateRefreshToken_createsValidJwtTest() {
        var actualResult = jwtService.generateRefreshToken();

        assertThat(actualResult).isNotBlank();
        assertThat(actualResult.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("validateAndExtractUserId returns userId from valid token")
    void validateAndExtractUserId_returnsUserIdFromValidTokenTest() {
        var token = jwtService.generateAccessToken(USER_ID);

        var actualResult = jwtService.validateAndExtractUserId(token);

        assertThat(actualResult).isEqualTo(USER_ID);
    }

    @Test
    @DisplayName("validateAndExtractUserId returns null for invalid token")
    void validateAndExtractUserId_returnsNullForInvalidTokenTest() {
        var actualResult = jwtService.validateAndExtractUserId("not-a-jwt-token");

        assertThat(actualResult).isNull();
    }

    @Test
    @DisplayName("validateAndExtractUserId returns null for expired token")
    void validateAndExtractUserId_returnsNullForExpiredTokenTest() {
        var secretKey = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        var expiredToken = Jwts.builder()
            .subject(USER_ID.toString())
            .issuedAt(new Date(System.currentTimeMillis() - 3600000))
            .expiration(new Date(System.currentTimeMillis() - 1800000))
            .signWith(secretKey)
            .compact();

        var actualResult = jwtService.validateAndExtractUserId(expiredToken);

        assertThat(actualResult).isNull();
    }

    @Test
    @DisplayName("validateAndExtractUserId returns null for tampered token")
    void validateAndExtractUserId_returnsNullForTamperedTokenTest() {
        var token = jwtService.generateAccessToken(USER_ID);
        var tamperedToken = token.substring(0, token.lastIndexOf('.')) + ".tampered";

        var actualResult = jwtService.validateAndExtractUserId(tamperedToken);

        assertThat(actualResult).isNull();
    }
}

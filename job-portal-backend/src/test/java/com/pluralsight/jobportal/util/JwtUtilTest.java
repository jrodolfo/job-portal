package com.pluralsight.jobportal.util;

import com.pluralsight.jobportal.exception.ResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey", "MY_SECRET_KEY_123456789012345678901234567890");
    }

    @Test
    void shouldGenerateAndExtractStandardJwtSubject() {
        String token = jwtUtil.generateToken("user@example.com");

        String email = jwtUtil.extractEmail(token);

        assertEquals("user@example.com", email);
    }

    @Test
    void shouldValidateStandardJwtForMatchingEmail() {
        String token = jwtUtil.generateToken("user@example.com");

        boolean valid = jwtUtil.validateToken(token, "user@example.com");

        assertTrue(valid);
    }

    @Test
    void shouldRejectStandardJwtForDifferentEmail() {
        String token = jwtUtil.generateToken("user@example.com");

        boolean valid = jwtUtil.validateToken(token, "other@example.com");

        assertFalse(valid);
    }

    @Test
    void extractEmailShouldThrowForBlankToken() {
        assertThrows(IllegalArgumentException.class, () -> jwtUtil.extractEmail("  "));
    }

    @Test
    void extractEmailShouldWrapParsingErrorForMalformedToken() {
        assertThrows(ResourceException.class, () -> jwtUtil.extractEmail("abc.def.ghi"));
    }

    @Test
    void shouldValidateGoogleTokenWhenEmailMatchesAndTokenNotExpired() {
        long futureEpochSeconds = (System.currentTimeMillis() / 1000) + 3600;
        String token = buildGoogleLikeToken("google.user@example.com", futureEpochSeconds);

        boolean valid = jwtUtil.validateToken(token, "google.user@example.com");

        assertTrue(valid);
    }

    @Test
    void shouldRejectGoogleTokenWhenExpired() {
        long pastEpochSeconds = (System.currentTimeMillis() / 1000) - 3600;
        String token = buildGoogleLikeToken("google.user@example.com", pastEpochSeconds);

        boolean valid = jwtUtil.validateToken(token, "google.user@example.com");

        assertFalse(valid);
    }

    @Test
    void shouldExtractEmailFromGoogleLikeToken() {
        long futureEpochSeconds = (System.currentTimeMillis() / 1000) + 3600;
        String token = buildGoogleLikeToken("google.user@example.com", futureEpochSeconds);

        String email = jwtUtil.extractEmail(token);

        assertEquals("google.user@example.com", email);
    }

    private String buildGoogleLikeToken(String email, long expEpochSeconds) {
        String header = "{\"alg\":\"RS256\",\"typ\":\"JWT\"}";
        String payload = "{\"iss\":\"https://accounts.google.com\",\"email\":\"" + email + "\",\"exp\":" + expEpochSeconds + "}";
        String encodedHeader = Base64.getUrlEncoder().withoutPadding().encodeToString(header.getBytes(StandardCharsets.UTF_8));
        String encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return encodedHeader + "." + encodedPayload + ".signature";
    }
}

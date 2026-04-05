package com.microcommerce.user.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for JwtTokenProvider
 * Tests unitarios para JwtTokenProvider
 */
@DisplayName("JwtTokenProvider Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    // Base64-encoded test secret (same as application.yml default)
    private static final String TEST_SECRET = "bWljcm9jb21tZXJjZS1zZWNyZXQta2V5LWZvci1kZXZlbG9wbWVudC1vbmx5LWNoYW5nZS1pbi1wcm9kdWN0aW9u";
    private static final long TEST_EXPIRATION = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(TEST_SECRET, TEST_EXPIRATION);
    }

    @Test
    @DisplayName("generateToken - debe generar token valido")
    void generateToken_ShouldGenerateValidToken() {
        // When
        String token = jwtTokenProvider.generateToken(1L, "john@example.com", "CUSTOMER");

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts: header.payload.signature
    }

    @Test
    @DisplayName("getEmailFromToken - debe extraer email correctamente")
    void getEmailFromToken_ShouldExtractEmailCorrectly() {
        // Given
        String token = jwtTokenProvider.generateToken(1L, "john@example.com", "CUSTOMER");

        // When
        String email = jwtTokenProvider.getEmailFromToken(token);

        // Then
        assertThat(email).isEqualTo("john@example.com");
    }

    @Test
    @DisplayName("getUserIdFromToken - debe extraer userId correctamente")
    void getUserIdFromToken_ShouldExtractUserIdCorrectly() {
        // Given
        String token = jwtTokenProvider.generateToken(42L, "john@example.com", "CUSTOMER");

        // When
        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        // Then
        assertThat(userId).isEqualTo(42L);
    }

    @Test
    @DisplayName("getRoleFromToken - debe extraer rol correctamente")
    void getRoleFromToken_ShouldExtractRoleCorrectly() {
        // Given
        String token = jwtTokenProvider.generateToken(1L, "admin@example.com", "ADMIN");

        // When
        String role = jwtTokenProvider.getRoleFromToken(token);

        // Then
        assertThat(role).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("validateToken - token valido - debe retornar true")
    void validateToken_ValidToken_ShouldReturnTrue() {
        // Given
        String token = jwtTokenProvider.generateToken(1L, "john@example.com", "CUSTOMER");

        // When
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("validateToken - token malformado - debe retornar false")
    void validateToken_MalformedToken_ShouldReturnFalse() {
        // When
        boolean isValid = jwtTokenProvider.validateToken("malformed.token.here");

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("validateToken - token vacio - debe retornar false")
    void validateToken_EmptyToken_ShouldReturnFalse() {
        // When
        boolean isValid = jwtTokenProvider.validateToken("");

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("validateToken - token expirado - debe retornar false")
    void validateToken_ExpiredToken_ShouldReturnFalse() {
        // Given - Create provider with 0ms expiration for immediate expiry
        JwtTokenProvider expiredProvider = new JwtTokenProvider(TEST_SECRET, 0L);
        String token = expiredProvider.generateToken(1L, "john@example.com", "CUSTOMER");

        // When
        boolean isValid = expiredProvider.validateToken(token);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("validateToken - token con firma incorrecta - debe retornar false")
    void validateToken_WrongSignature_ShouldReturnFalse() {
        // Given - Create token with different secret
        String differentSecret = "YW5vdGhlci1zZWNyZXQta2V5LWZvci10ZXN0aW5nLXB1cnBvc2VzLW9ubHktZG8tbm90LXVzZS1pbi1wcm9k";
        JwtTokenProvider otherProvider = new JwtTokenProvider(differentSecret, TEST_EXPIRATION);
        String token = otherProvider.generateToken(1L, "john@example.com", "CUSTOMER");

        // When
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("generateToken - diferentes usuarios - deben generar tokens diferentes")
    void generateToken_DifferentUsers_ShouldGenerateDifferentTokens() {
        // When
        String token1 = jwtTokenProvider.generateToken(1L, "user1@example.com", "CUSTOMER");
        String token2 = jwtTokenProvider.generateToken(2L, "user2@example.com", "ADMIN");

        // Then
        assertThat(token1).isNotEqualTo(token2);
    }
}

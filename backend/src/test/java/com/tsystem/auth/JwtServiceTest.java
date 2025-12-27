package com.tsystem.auth;

import com.tsystem.model.user.SystemRole;
import com.tsystem.model.user.User;
import com.tsystem.repository.UserRepository;
import com.tsystem.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    UserRepository userRepository;

    JwtService jwtService;
    User testUser;

    @BeforeEach
    void setup() throws Exception {
        jwtService = new JwtService(userRepository);

        // ===== RSA KEYS =====
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        String privateKey = Base64.getEncoder()
                .encodeToString(keyPair.getPrivate().getEncoded());
        String publicKey = Base64.getEncoder()
                .encodeToString(keyPair.getPublic().getEncoded());

        jwtService.setPrivateKey(privateKey);
        jwtService.setPublicKey(publicKey);

        // ===== USER =====
        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("test@example.com")
                .email("test@example.com")
                .name("Test")
                .surname("User")
                .role(SystemRole.ADMIN)
                .tokenVersion(1)
                .build();
    }

    // =======================
    // TOKEN GENERATION
    // =======================

    @Test
    @DisplayName("generateToken - generates valid JWT")
    void generateToken_ReturnsValidToken() {
        String token = jwtService.generateToken(testUser);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
    }

    @Test
    @DisplayName("generateToken - token contains username as subject")
    void generateToken_ContainsCorrectUsername() {
        String token = jwtService.generateToken(testUser);

        String username = jwtService.extractUsername(token);
        assertEquals("test@example.com", username);
    }

    @Test
    @DisplayName("generateToken - different users receive different tokens")
    void generateToken_DifferentUsers_DifferentTokens() {
        User otherUser = User.builder()
                .id(UUID.randomUUID())
                .username("other@example.com")
                .role(SystemRole.USER)
                .tokenVersion(1)
                .build();

        String token1 = jwtService.generateToken(testUser);
        String token2 = jwtService.generateToken(otherUser);

        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("generateToken - throws exception for non-User instance")
    void generateToken_NonUserObject_ThrowsException() {
        UserDetails springUser =
                new org.springframework.security.core.userdetails.User(
                        "test", "pass", Collections.emptyList());

        assertThrows(IllegalArgumentException.class,
                () -> jwtService.generateToken(springUser));
    }

    // =======================
    // TOKEN VALIDATION
    // =======================

    @Test
    @DisplayName("isTokenValid - valid token returns true")
    void isTokenValid_ValidToken_ReturnsTrue() {
        when(userRepository.findById(testUser.getId()))
                .thenReturn(Optional.of(testUser));

        String token = jwtService.generateToken(testUser);

        assertTrue(jwtService.isTokenValid(token, testUser));
    }

    @Test
    @DisplayName("isTokenValid - token invalid for different user")
    void isTokenValid_DifferentUser_ReturnsFalse() {
        when(userRepository.findById(testUser.getId()))
                .thenReturn(Optional.of(testUser));

        String token = jwtService.generateToken(testUser);

        User otherUser = User.builder()
                .id(UUID.randomUUID())
                .username("other@example.com")
                .tokenVersion(1)
                .build();

        assertFalse(jwtService.isTokenValid(token, otherUser));
    }

    @Test
    @DisplayName("isTokenValid - throws exception for malformed token")
    void isTokenValid_InvalidToken_ThrowsException() {
        assertThrows(Exception.class,
                () -> jwtService.isTokenValid("invalid.token.here", testUser));
    }

    // =======================
    // CLAIM EXTRACTION
    // =======================

    @Test
    @DisplayName("extractUsername - extracts correct username")
    void extractUsername_ReturnsCorrectUsername() {
        String token = jwtService.generateToken(testUser);

        assertEquals("test@example.com",
                jwtService.extractUsername(token));
    }

    @Test
    @DisplayName("extractUsername - throws exception for invalid token")
    void extractUsername_InvalidToken_ThrowsException() {
        assertThrows(Exception.class,
                () -> jwtService.extractUsername("invalid.token"));
    }

    // =======================
    // TOKEN DETAILS
    // =======================

    @Test
    @DisplayName("getTokenDetails - returns token details string")
    void getTokenDetails_ReturnsDetails() {
        String token = jwtService.generateToken(testUser);

        String details = jwtService.getTokenDetails(token);

        assertNotNull(details);
        assertTrue(details.contains("test@example.com"));
        assertTrue(details.contains("ADMIN"));
    }
}



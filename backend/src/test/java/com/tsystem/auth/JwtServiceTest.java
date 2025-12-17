package com.tsystem.auth;

import com.tsystem.model.user.SystemRole;
import com.tsystem.model.user.User;
import com.tsystem.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    JwtService jwtService;
    User testUser;

    @BeforeEach
    void setup() {
        jwtService = new JwtService();
        byte[] keyBytes = new byte[64];
        for (int i = 0; i < keyBytes.length; i++) {
            keyBytes[i] = (byte) i;
        }
        jwtService.setSecretKey(Base64.getEncoder().encodeToString(keyBytes));

        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("test@example.com")
                .email("test@example.com")
                .name("Test")
                .surname("User")
                .role(SystemRole.ADMIN)
                .build();
    }

    @Test @DisplayName("generateToken - generates valid token")
    void generateToken_ReturnsValidToken() {
        String token = jwtService.generateToken(testUser);
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
    }

    @Test @DisplayName("generateToken - token contains valid username")
    void generateToken_ContainsCorrectUsername() {
        String token = jwtService.generateToken(testUser);
        assertEquals("test@example.com", jwtService.extractUsername(token));
    }

    @Test @DisplayName("generateToken - different users get different tokens")
    void generateToken_DifferentUsers_DifferentTokens() {
        User user2 = User.builder().id(UUID.randomUUID()).username("user2@example.com").name("U2").surname("T").role(SystemRole.USER).build();
        String token1 = jwtService.generateToken(testUser);
        String token2 = jwtService.generateToken(user2);
        assertNotEquals(token1, token2);
    }

    @Test @DisplayName("generateToken - throws exception for non-user object")
    void generateToken_NonUserObject_ThrowsException() {
        org.springframework.security.core.userdetails.User springUser =
                new org.springframework.security.core.userdetails.User("test", "pass", java.util.Collections.emptyList());
        assertThrows(IllegalArgumentException.class, () -> jwtService.generateToken(springUser));
    }

    @Test @DisplayName("isTokenValid - valid token returns true")
    void isTokenValid_ValidToken_ReturnsTrue() {
        String token = jwtService.generateToken(testUser);
        assertTrue(jwtService.isTokenValid(token, testUser));
    }

    @Test @DisplayName("isTokenValid - token is invalid for different user")
    void isTokenValid_DifferentUser_ReturnsFalse() {
        String token = jwtService.generateToken(testUser);
        User differentUser = User.builder().id(UUID.randomUUID()).username("different@example.com").name("D").surname("U").role(SystemRole.USER).build();
        assertFalse(jwtService.isTokenValid(token, differentUser));
    }

    @Test @DisplayName("isTokenValid - invalid token throws exception")
    void isTokenValid_InvalidToken_ThrowsException() {
        assertThrows(Exception.class, () -> jwtService.isTokenValid("invalid.token.here", testUser));
    }

    @Test @DisplayName("extractUsername - extracts valid username")
    void extractUsername_ReturnsCorrectUsername() {
        String token = jwtService.generateToken(testUser);
        assertEquals("test@example.com", jwtService.extractUsername(token));
    }

    @Test @DisplayName("extractUsername - throws exception for invaild token")
    void extractUsername_InvalidToken_ThrowsException() {
        assertThrows(Exception.class, () -> jwtService.extractUsername("invalid.token"));
    }

    @Test @DisplayName("getTokenDetails - returns token details")
    void getTokenDetails_ReturnsDetails() {
        String token = jwtService.generateToken(testUser);
        String details = jwtService.getTokenDetails(token);
        assertNotNull(details);
        assertTrue(details.contains("test@example.com"));
    }

    @Test @DisplayName("getTokenDetails - contains role")
    void getTokenDetails_ContainsRole() {
        String token = jwtService.generateToken(testUser);
        String details = jwtService.getTokenDetails(token);
        assertTrue(details.contains("ADMIN"));
    }
}



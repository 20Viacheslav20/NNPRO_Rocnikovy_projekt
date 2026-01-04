package com.tsystem.auth;

import com.tsystem.model.user.SystemRole;
import com.tsystem.model.user.User;
import com.tsystem.repository.UserRepository;
import com.tsystem.service.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
    UUID userId;

    @BeforeEach
    void setup() throws Exception {
        jwtService = new JwtService(userRepository);

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        String privateKey = Base64.getEncoder()
                .encodeToString(keyPair.getPrivate().getEncoded());
        String publicKey = Base64.getEncoder()
                .encodeToString(keyPair.getPublic().getEncoded());

        jwtService.setPrivateKey(privateKey);
        jwtService.setPublicKey(publicKey);

        userId = UUID.randomUUID();
        testUser = User.builder()
                .id(userId)
                .username("test@example.com")
                .email("test@example.com")
                .name("Test")
                .surname("User")
                .role(SystemRole.ADMIN)
                .tokenVersion(1)
                .blocked(false)
                .build();
    }

    @Nested
    @DisplayName("Token Generation Tests")
    class TokenGenerationTests {

        @Test
        @DisplayName("generateToken - generates valid JWT")
        void generateToken_ReturnsValidToken() {
            String token = jwtService.generateToken(testUser);

            assertNotNull(token);
            assertFalse(token.isEmpty());
            assertTrue(token.contains("."));
            assertEquals(3, token.split("\\.").length);
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
                    .name("Other")
                    .surname("User")
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

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> jwtService.generateToken(springUser));
            assertEquals("UserDetails must be instance of User", ex.getMessage());
        }

        @Test
        @DisplayName("generateToken with extra claims")
        void generateToken_WithExtraClaims() {
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("customClaim", "customValue");

            String token = jwtService.generateToken(extraClaims, testUser);

            assertNotNull(token);
            String details = jwtService.getTokenDetails(token);
            assertTrue(details.contains("customClaim"));
        }
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class TokenValidationTests {

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

        @Test
        @DisplayName("isTokenValid - blocked user returns false")
        void isTokenValid_BlockedUser_ReturnsFalse() {
            String token = jwtService.generateToken(testUser);

            User blockedUser = User.builder()
                    .id(userId)
                    .username("test@example.com")
                    .tokenVersion(1)
                    .blocked(true)
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(blockedUser));

            assertFalse(jwtService.isTokenValid(token, testUser));
        }

        @Test
        @DisplayName("isTokenValid - mismatched token version returns false")
        void isTokenValid_MismatchedTokenVersion_ReturnsFalse() {
            String token = jwtService.generateToken(testUser);

            User userWithNewVersion = User.builder()
                    .id(userId)
                    .username("test@example.com")
                    .tokenVersion(2) // Different version
                    .blocked(false)
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(userWithNewVersion));

            assertFalse(jwtService.isTokenValid(token, testUser));
        }

        @Test
        @DisplayName("isTokenValid - user not found throws exception")
        void isTokenValid_UserNotFound_ThrowsException() {
            String token = jwtService.generateToken(testUser);

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> jwtService.isTokenValid(token, testUser));
        }
    }

    @Nested
    @DisplayName("Claim Extraction Tests")
    class ClaimExtractionTests {

        @Test
        @DisplayName("extractUsername - extracts correct username")
        void extractUsername_ReturnsCorrectUsername() {
            String token = jwtService.generateToken(testUser);

            assertEquals("test@example.com", jwtService.extractUsername(token));
        }

        @Test
        @DisplayName("extractUsername - throws exception for invalid token")
        void extractUsername_InvalidToken_ThrowsException() {
            assertThrows(Exception.class, () -> jwtService.extractUsername("invalid.token"));
        }

        @Test
        @DisplayName("extractClaim - extracts custom claim")
        void extractClaim_ReturnsCorrectClaim() {
            String token = jwtService.generateToken(testUser);

            String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
            assertEquals("ADMIN", role);
        }

        @Test
        @DisplayName("extractClaim - extracts userId claim")
        void extractClaim_ExtractsUserId() {
            String token = jwtService.generateToken(testUser);

            String userIdClaim = jwtService.extractClaim(token, claims -> claims.get("userId", String.class));
            assertEquals(userId.toString(), userIdClaim);
        }

        @Test
        @DisplayName("extractClaim - extracts name and surname")
        void extractClaim_ExtractsNameAndSurname() {
            String token = jwtService.generateToken(testUser);

            String name = jwtService.extractClaim(token, claims -> claims.get("name", String.class));
            String surname = jwtService.extractClaim(token, claims -> claims.get("surname", String.class));

            assertEquals("Test", name);
            assertEquals("User", surname);
        }
    }

    @Nested
    @DisplayName("Token Details Tests")
    class TokenDetailsTests {

        @Test
        @DisplayName("getTokenDetails - returns token details string")
        void getTokenDetails_ReturnsDetails() {
            String token = jwtService.generateToken(testUser);

            String details = jwtService.getTokenDetails(token);

            assertNotNull(details);
            assertTrue(details.contains("test@example.com"));
            assertTrue(details.contains("ADMIN"));
            assertTrue(details.contains("Token details:"));
            assertTrue(details.contains("Subject (username):"));
            assertTrue(details.contains("UserID:"));
            assertTrue(details.contains("Issued at:"));
            assertTrue(details.contains("Expiration:"));
        }

        @Test
        @DisplayName("getTokenDetails - contains all expected claims")
        void getTokenDetails_ContainsAllClaims() {
            String token = jwtService.generateToken(testUser);

            String details = jwtService.getTokenDetails(token);

            assertTrue(details.contains(userId.toString()));
            assertTrue(details.contains("Test"));
            assertTrue(details.contains("User"));
        }
    }
}
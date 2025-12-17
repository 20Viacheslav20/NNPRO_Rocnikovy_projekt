package com.tsystem.auth;

import com.tsystem.model.dto.ChangePassword;
import com.tsystem.model.dto.RequestPasswordReset;
import com.tsystem.model.dto.ResetPassword;
import com.tsystem.model.dto.request.LoginRequest;
import com.tsystem.model.dto.request.RegisterRequest;
import com.tsystem.model.dto.response.TokenResponse;
import com.tsystem.model.user.PasswordResetToken;
import com.tsystem.model.user.SystemRole;
import com.tsystem.model.user.User;
import com.tsystem.repository.PasswordResetTokenRepository;
import com.tsystem.repository.UserRepository;
import com.tsystem.service.AuthService;
import com.tsystem.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock AuthenticationManager authenticationManager;
    @Mock PasswordResetTokenRepository passwordResetTokenRepository;

    @InjectMocks AuthService authService;

    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = User.builder()
                .id(userId)
                .username("test@example.com")
                .email("test@example.com")
                .name("Test")
                .surname("User")
                .password("hashedPassword")
                .role(SystemRole.USER)
                .build();
    }

    @Nested
    @DisplayName("Register Tests")
    class RegisterTests {

        @Test
        @DisplayName("New user was successfully registered")
        void register_Success() {
            RegisterRequest req = RegisterRequest.builder()
                    .email("new@example.com").name("New").surname("User").password("password123").build();

            when(userRepository.existsByEmail(req.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(any())).thenReturn("hashedPassword");
            when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

            TokenResponse response = authService.register(req);

            assertEquals("jwt-token", response.getToken());
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Registration with existing email throws exception")
        void register_EmailExists_ThrowsException() {
            RegisterRequest req = RegisterRequest.builder()
                    .email("existing@example.com").name("Test").surname("User").password("password").build();

            when(userRepository.existsByEmail(req.getEmail())).thenReturn(true);

            assertThrows(IllegalArgumentException.class, () -> authService.register(req));
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("During registration user with ADMIN role has been created")
        void register_CreatesAdminUser() {
            RegisterRequest req = RegisterRequest.builder()
                    .email("admin@example.com").name("Admin").surname("User").password("password").build();

            when(userRepository.existsByEmail(any())).thenReturn(false);
            when(passwordEncoder.encode(any())).thenReturn("hash");
            when(jwtService.generateToken(any(User.class))).thenReturn("token");

            authService.register(req);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertEquals(SystemRole.ADMIN, userCaptor.getValue().getRole());
        }
    }

    @Nested
    @DisplayName("Authenticate Tests")
    class AuthenticateTests {

        @Test
        @DisplayName("Successful authentication by username")
        void authenticate_ByUsername_Success() {
            LoginRequest req = new LoginRequest("test@example.com", "password123");

            when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(testUser));
            when(jwtService.generateToken(testUser)).thenReturn("jwt-token");

            TokenResponse response = authService.authenticate(req);

            assertEquals("jwt-token", response.getToken());
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }

        @Test
        @DisplayName("Successful authentication by email")
        void authenticate_ByEmail_Success() {
            LoginRequest req = new LoginRequest("test@example.com", "password123");

            when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(jwtService.generateToken(testUser)).thenReturn("jwt-token");

            TokenResponse response = authService.authenticate(req);

            assertEquals("jwt-token", response.getToken());
        }

        @Test
        @DisplayName("Authentication with invalid login throws exception")
        void authenticate_InvalidLogin_ThrowsException() {
            LoginRequest req = new LoginRequest("nonexistent@example.com", "password");

            when(userRepository.findByUsername("nonexistent@example.com")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> authService.authenticate(req));
        }
    }

    @Nested
    @DisplayName("Request Password Reset Tests")
    class RequestPasswordResetTests {

        @Test
        @DisplayName("Request for password reset for existing user")
        void requestPasswordReset_UserExists_CreatesToken() {
            RequestPasswordReset req = RequestPasswordReset.builder().login("test@example.com").build();

            when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.encode(any())).thenReturn("hashedCode");

            authService.requestPasswordReset(req);

            verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        }

        @Test
        @DisplayName("Request for password reset for non-existing user does not throw exception")
        void requestPasswordReset_UserNotExists_NoException() {
            RequestPasswordReset req = RequestPasswordReset.builder().login("nonexistent@example.com").build();

            when(userRepository.findByUsername("nonexistent@example.com")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

            assertDoesNotThrow(() -> authService.requestPasswordReset(req));
            verify(passwordResetTokenRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Reset Password Tests")
    class ResetPasswordTests {

        @Test
        @DisplayName("Successful password request")
        void resetPassword_Success() {
            UUID tokenId = UUID.randomUUID();
            String code = "12345678";
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .id(tokenId).user(testUser).token("hashedCode")
                    .expiresAt(OffsetDateTime.now().plusMinutes(5)).build();

            ResetPassword req = ResetPassword.builder()
                    .code(tokenId + "." + code).newPassword("newPassword123").build();

            when(passwordResetTokenRepository.findById(tokenId)).thenReturn(Optional.of(resetToken));
            when(passwordEncoder.matches(code, "hashedCode")).thenReturn(true);
            when(passwordEncoder.encode("newPassword123")).thenReturn("newHashedPassword");

            authService.resetPassword(req);

            verify(userRepository).save(testUser);
            verify(passwordResetTokenRepository).delete(resetToken);
        }

        @Test
        @DisplayName("Reset password with inbalid token format")
        void resetPassword_InvalidTokenFormat_ThrowsException() {
            ResetPassword req = ResetPassword.builder().code("invalidformat").newPassword("newPassword").build();
            assertThrows(IllegalArgumentException.class, () -> authService.resetPassword(req));
        }

        @Test
        @DisplayName("Reset password with expired token")
        void resetPassword_ExpiredToken_ThrowsException() {
            UUID tokenId = UUID.randomUUID();
            PasswordResetToken expiredToken = PasswordResetToken.builder()
                    .id(tokenId).user(testUser).token("hashedCode")
                    .expiresAt(OffsetDateTime.now().minusMinutes(5)).build();

            ResetPassword req = ResetPassword.builder().code(tokenId + ".12345678").newPassword("newPassword").build();

            when(passwordResetTokenRepository.findById(tokenId)).thenReturn(Optional.of(expiredToken));

            assertThrows(IllegalStateException.class, () -> authService.resetPassword(req));
        }
    }

    @Nested
    @DisplayName("Change Password Tests")
    class ChangePasswordTests {

        @Test
        @DisplayName("Successful password change")
        void changePassword_Success() {
            ChangePassword req = ChangePassword.builder().oldPassword("oldPassword").newPassword("newPassword123").build();

            when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("oldPassword", "hashedPassword")).thenReturn(true);
            when(passwordEncoder.encode("newPassword123")).thenReturn("newHashedPassword");

            authService.changePassword(req, "test@example.com");

            verify(userRepository).save(testUser);
            verify(passwordResetTokenRepository).deleteAllByUserId(userId);
        }

        @Test
        @DisplayName("Changing of password with wrong old password")
        void changePassword_WrongOldPassword_ThrowsException() {
            ChangePassword req = ChangePassword.builder().oldPassword("wrongPassword").newPassword("newPassword").build();

            when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

            assertThrows(IllegalArgumentException.class, () -> authService.changePassword(req, "test@example.com"));
            verify(userRepository, never()).save(any());
        }
    }
}

package com.tsystem.auth;

import com.tsystem.configuration.JwtAuthenticationFilter;
import com.tsystem.controller.AuthController;
import com.tsystem.exception.UnauthorizedException;
import com.tsystem.model.dto.response.TokenResponse;
import com.tsystem.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Nested
    @DisplayName("POST /api/auth/register")
    class RegisterTests {

        @Test
        @DisplayName("successful registration returns token")
        void register_Success() throws Exception {
            when(authService.register(any())).thenReturn(new TokenResponse("jwt-token"));

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "email": "test@example.com",
                                        "name": "Test",
                                        "surname": "User",
                                        "password": "password123"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("jwt-token"));

            verify(authService).register(any());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginTests {

        @Test
        @DisplayName("successful login returns token")
        void login_Success() throws Exception {
            when(authService.authenticate(any())).thenReturn(new TokenResponse("jwt-token"));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "login": "test@example.com",
                                        "password": "password123"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("jwt-token"));

            verify(authService).authenticate(any());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/request-password-reset")
    class RequestPasswordResetTests {

        @Test
        @DisplayName("successful request returns 204")
        void requestPasswordReset_Success() throws Exception {
            doNothing().when(authService).requestPasswordReset(any());

            mockMvc.perform(post("/api/auth/request-password-reset")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "login": "test@example.com"
                                    }
                                    """))
                    .andExpect(status().isNoContent());

            verify(authService).requestPasswordReset(any());
        }

        @Test
        @DisplayName("request for non-existent user still returns 204 (no reveal)")
        void requestPasswordReset_NonExistentUser_Returns204() throws Exception {
            doNothing().when(authService).requestPasswordReset(any());

            mockMvc.perform(post("/api/auth/request-password-reset")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "login": "nonexistent@example.com"
                                    }
                                    """))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/reset-password")
    class ResetPasswordTests {

        @Test
        @DisplayName("successful reset returns 204")
        void resetPassword_Success() throws Exception {
            doNothing().when(authService).resetPassword(any());

            mockMvc.perform(post("/api/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "code": "uuid.12345678",
                                        "newPassword": "newPassword123"
                                    }
                                    """))
                    .andExpect(status().isNoContent());

            verify(authService).resetPassword(any());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/change-password")
    class ChangePasswordTests {

        @Test
        @DisplayName("without authentication throws UnauthorizedException")
        void changePassword_WithoutAuth_ThrowsUnauthorized() {
            Exception thrown = assertThrows(
                    jakarta.servlet.ServletException.class,
                    () -> mockMvc.perform(post("/api/auth/change-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "oldPassword": "oldPassword",
                                        "newPassword": "newPassword123"
                                    }
                                    """))
            );

            assertInstanceOf(UnauthorizedException.class, thrown.getCause(),
                    "Expected UnauthorizedException as cause");
        }

        @Test
        @DisplayName("with authentication returns 204")
        @WithMockUser(username = "test@example.com")
        void changePassword_WithAuth_Success() throws Exception {
            doNothing().when(authService).changePassword(any(), eq("test@example.com"));

            mockMvc.perform(post("/api/auth/change-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "oldPassword": "oldPassword",
                                        "newPassword": "newPassword123"
                                    }
                                    """))
                    .andExpect(status().isNoContent());

            verify(authService).changePassword(any(), eq("test@example.com"));
        }
    }
}
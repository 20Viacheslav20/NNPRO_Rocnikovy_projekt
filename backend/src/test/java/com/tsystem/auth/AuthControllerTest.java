package com.tsystem.auth;

import com.tsystem.configuration.JwtAuthenticationFilter;
import com.tsystem.controller.AuthController;
import com.tsystem.exception.UnauthorizedException;
import com.tsystem.model.dto.response.TokenResponse;
import com.tsystem.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
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

    @Autowired MockMvc mockMvc;
    @MockitoBean AuthService authService;

    // === Register Tests ===
    @Test
    @DisplayName("POST /api/auth/register - successful registration")
    void register_Success() throws Exception {
        when(authService.register(any())).thenReturn(new TokenResponse("jwt-token"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"name\":\"Test\",\"surname\":\"User\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    // === Login Tests ===
    @Test
    @DisplayName("POST /api/auth/login - successful login")
    void login_Success() throws Exception {
        when(authService.authenticate(any())).thenReturn(new TokenResponse("jwt-token"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"test@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    // === Request Password Reset Tests ===
    @Test
    @DisplayName("POST /api/auth/request-password-reset - successful request")
    void requestPasswordReset_Success() throws Exception {
        doNothing().when(authService).requestPasswordReset(any());

        mockMvc.perform(post("/api/auth/request-password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"test@example.com\"}"))
                .andExpect(status().isNoContent());
    }

    // === Reset Password Tests ===
    @Test
    @DisplayName("POST /api/auth/reset-password - successful reset")
    void resetPassword_Success() throws Exception {
        doNothing().when(authService).resetPassword(any());

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"uuid.12345678\",\"newPassword\":\"newPassword123\"}"))
                .andExpect(status().isNoContent());
    }

    // === Change Password Tests ===
    // Note: changePassword requires @AuthenticationPrincipal and throws UnauthorizedException if null
    // UnauthorizedException is not handled in ErrorHandling, so it gets wrapped in ServletException
    @Test
    @DisplayName("POST /api/auth/change-password - without authentication throws UnauthorizedException")
    void changePassword_WithoutAuth_ThrowsUnauthorized() {
        Exception thrown = org.junit.jupiter.api.Assertions.assertThrows(
                jakarta.servlet.ServletException.class,
                () -> mockMvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\":\"oldPassword\",\"newPassword\":\"newPassword123\"}"))
        );

        assert thrown.getCause() instanceof UnauthorizedException :
                "Expected UnauthorizedException as cause but got " + thrown.getCause().getClass().getSimpleName();
    }
}
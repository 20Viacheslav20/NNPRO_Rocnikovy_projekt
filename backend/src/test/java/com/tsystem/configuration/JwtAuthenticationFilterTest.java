package com.tsystem.configuration;

import com.tsystem.model.user.SystemRole;
import com.tsystem.model.user.User;
import com.tsystem.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private User testUser;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService, userDetailsService);

        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("test@example.com")
                .email("test@example.com")
                .name("Test")
                .surname("User")
                .role(SystemRole.ADMIN)
                .password("hashedPassword")
                .build();
    }

    @Nested
    @DisplayName("Authorization Header Tests")
    class AuthorizationHeaderTests {

        @Test
        @DisplayName("Без Authorization header продовжує без автентифікації")
        void noAuthHeader_ContinuesWithoutAuth() throws ServletException, IOException {
            when(request.getHeader("Authorization")).thenReturn(null);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }

        @Test
        @DisplayName("Authorization header without Bearer prefix continues without authentication")
        void nonBearerHeader_ContinuesWithoutAuth() throws ServletException, IOException {
            when(request.getHeader("Authorization")).thenReturn("Basic some token");

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }
    }

    @Nested
    @DisplayName("Valid Token Tests")
    class ValidTokenTests {

        @Test
        @DisplayName("Valid token sets authentication")
        void validToken_SetsAuthentication() throws ServletException, IOException {
            String token = "valid.jwt.token";

            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtService.extractUsername(token)).thenReturn("test@example.com");
            when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(testUser);
            when(jwtService.isTokenValid(token, testUser)).thenReturn(true);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertNotNull(SecurityContextHolder.getContext().getAuthentication());
            assertEquals("test@example.com",
                    ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername());
        }

        @Test
        @DisplayName("Token with correct username is extracted")
        void validToken_ExtractsUsername() throws ServletException, IOException {
            String token = "valid.jwt.token";

            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtService.extractUsername(token)).thenReturn("test@example.com");
            when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(testUser);
            when(jwtService.isTokenValid(token, testUser)).thenReturn(true);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(jwtService).extractUsername(token);
        }
    }

    @Nested
    @DisplayName("Invalid Token Tests")
    class InvalidTokenTests {

        @Test
        @DisplayName("Invalid token does not set authentication")
        void invalidToken_NoAuthentication() throws ServletException, IOException {
            String token = "invalid.jwt.token";

            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtService.extractUsername(token)).thenReturn("test@example.com");
            when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(testUser);
            when(jwtService.isTokenValid(token, testUser)).thenReturn(false);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }

        @Test
        @DisplayName("Null username does not set authentication")
        void nullUsername_NoAuthentication() throws ServletException, IOException {
            String token = "some.jwt.token";

            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtService.extractUsername(token)).thenReturn(null);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(userDetailsService, never()).loadUserByUsername(any());
        }
    }

    @Nested
    @DisplayName("Already Authenticated Tests")
    class AlreadyAuthenticatedTests {

        @Test
        @DisplayName("If it is authentified it does not reset")
        void alreadyAuthenticated_SkipsTokenValidation() throws ServletException, IOException {
            String token = "valid.jwt.token";

            SecurityContextHolder.getContext().setAuthentication(
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            testUser, null, testUser.getAuthorities()));

            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtService.extractUsername(token)).thenReturn("test@example.com");

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(userDetailsService, never()).loadUserByUsername(any());
        }
    }
}

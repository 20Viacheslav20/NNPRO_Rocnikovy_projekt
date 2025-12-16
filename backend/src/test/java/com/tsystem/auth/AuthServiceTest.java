package com.tsystem.auth;

import com.tsystem.model.dto.request.LoginRequest;
import com.tsystem.model.dto.request.RegisterRequest;
import com.tsystem.model.dto.response.TokenResponse;
import com.tsystem.model.user.User;
import com.tsystem.repository.PasswordResetTokenRepository;
import com.tsystem.repository.UserRepository;
import com.tsystem.service.AuthService;
import com.tsystem.service.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    JwtService jwtService;

    @InjectMocks
    AuthService authService;

    @Test
    void register_success() {
        RegisterRequest req = RegisterRequest.builder()
                .email("a@test.com")
                .name("A")
                .surname("B")
                .password("password")
                .build();

        when(userRepository.existsByEmail(req.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt");

        TokenResponse res = authService.register(req);

        assertEquals("jwt", res.getToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void authenticate_invalid_login() {
        when(userRepository.findByUsername("bad")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("bad")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> authService.authenticate(new LoginRequest("bad", "123")));
    }
}
package com.tsystem.auth;

import com.tsystem.model.*;
import com.tsystem.model.user.SystemRole;
import com.tsystem.model.user.User;
import com.tsystem.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    JwtService jwtService;

    @BeforeEach
    void setup() {
        jwtService = new JwtService();
        byte[] keyBytes = new byte[64];
        for (int i = 0; i < keyBytes.length; i++) {
            keyBytes[i] = (byte) i;
        }

        String base64Key = Base64.getEncoder().encodeToString(keyBytes);
        jwtService.setSecretKey(base64Key);
    }

    @Test
    void generate_and_validate_token() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("u@test.com")
                .name("U")
                .surname("S")
                .role(SystemRole.ADMIN)
                .build();

        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token, user));
        assertEquals("u@test.com", jwtService.extractUsername(token));
    }
}



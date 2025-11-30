package com.tsystem.service;


import com.tsystem.model.dto.request.LoginRequest;
import com.tsystem.model.dto.request.RegisterRequest;
import com.tsystem.model.dto.response.TokenResponse;
import com.tsystem.model.user.SystemRole;
import com.tsystem.model.user.User;

import com.tsystem.model.dto.*;
import com.tsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public TokenResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User with same username or email already exists");
        }

        var user = User.builder()
                .username(request.getEmail())
                .email(request.getEmail())
                .name(request.getName())         // важно, если NOT NULL
                .surname(request.getSurname())   // важно, если NOT NULL
                .password(passwordEncoder.encode(request.getPassword()))
                .role(SystemRole.SYSTEM_USER)
                .build();

        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);

        return new TokenResponse(jwtToken);
    }


    /**
     * Authentication. login = username OR email.
     * AuthenticationManager expects a username, so when logging in by email,
     * we first find the user and substitute their username.
     */
    public TokenResponse authenticate(LoginRequest request) {
        var user = userRepository.findByUsername(request.getLogin())
                .orElseGet(() -> userRepository.findByEmail(request.getLogin())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid credentials")));

        // проверка пароля и создание SecurityContext — через AuthenticationManager
        //
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword())
        );

        String jwtToken = jwtService.generateToken((UserDetails) user);
        return new TokenResponse(jwtToken);
    }

    /**
     * Password reset request (login = username/email). Always 204, no reveal.
     */
    public void requestPasswordReset(RequestPasswordReset req) {
        Optional<User> opt = userRepository.findByUsername(req.getLogin())
                .or(() -> userRepository.findByEmail(req.getLogin()));

        opt.ifPresent(u -> {
            UUID tokenId = UUID.randomUUID();
            String code = generateNumericCode(8);                 // 8 цифр
            String codeHash = passwordEncoder.encode(code);       // в БД хэш

            u.setResetTokenId(tokenId);
            u.setResetCode(codeHash);
            u.setResetCodeExp(OffsetDateTime.now().plusMinutes(10)); // TTL 10 мин

            userRepository.save(u);

            // In DEV, it's convenient to log the full token (uuid.code). In production, send it by email.
            System.out.println("DEV reset token for " + u.getUsername() + " -> " + tokenId + "." + code);
        });

    }

    /**
     * Password reset by token (<uuid>.<code>)
     */
    public void resetPassword(ResetPassword req) {
        String[] parts = req.getCode().split("\\.", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("invalid reset token");
        }
        UUID tokenId = UUID.fromString(parts[0]);
        String code = parts[1];

        User u = userRepository.findByResetTokenId(tokenId)
                .orElseThrow(() -> new IllegalArgumentException("invalid reset token"));

        if (u.getResetCodeExp() == null || OffsetDateTime.now().isAfter(u.getResetCodeExp())) {
            throw new IllegalStateException("reset code expired");
        }
        if (u.getResetCode() == null || !passwordEncoder.matches(code, u.getResetCode())) {
            throw new IllegalArgumentException("invalid reset code");
        }

        u.setPassword(passwordEncoder.encode(req.getNewPassword()));
        u.setPasswordChangedAt(OffsetDateTime.now());

        // invalidate the one-time token
        u.setResetTokenId(null);
        u.setResetCode(null);
        u.setResetCodeExp(null);

        userRepository.save(u);
    }

    /** 3) Change the password for the authorized user (check the old one, set a new one) */
    public void changePassword(ChangePassword req, String currentUsername) {
        User me = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        if (!passwordEncoder.matches(req.getOldPassword(), me.getPassword())) {
            throw new IllegalArgumentException("old password mismatch");
        }

        me.setPassword(passwordEncoder.encode(req.getNewPassword()));
        me.setPasswordChangedAt(OffsetDateTime.now());

        // at the same time, we invalidate the potential active reset token
        me.setResetTokenId(null);
        me.setResetCode(null);
        me.setResetCodeExp(null);

        userRepository.save(me);
    }

    private static String generateNumericCode(int len) {
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(rnd.nextInt(10));
        return sb.toString();
    }
}
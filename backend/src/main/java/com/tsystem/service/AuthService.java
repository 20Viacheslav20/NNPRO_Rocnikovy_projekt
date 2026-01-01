package com.tsystem.service;


import com.tsystem.model.dto.request.LoginRequest;
import com.tsystem.model.dto.request.RegisterRequest;
import com.tsystem.model.dto.response.TokenResponse;
import com.tsystem.model.user.PasswordResetToken;
import com.tsystem.model.user.SystemRole;
import com.tsystem.model.user.User;

import com.tsystem.model.dto.*;
import com.tsystem.repository.PasswordResetTokenRepository;
import com.tsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public TokenResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User with same username or email already exists");
        }

        var user = User.builder()
                .username(request.getEmail())
                .email(request.getEmail())
                .name(request.getName())
                .surname(request.getSurname())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(SystemRole.ADMIN)
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

        if (user.isBlocked())
            throw new IllegalArgumentException("Invalid credentials");

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword())
        );

        String jwtToken = jwtService.generateToken((UserDetails) user);
        return new TokenResponse(jwtToken);
    }

    /**
     * Password reset request (login = username/email). Always 204, no reveal.
     */
    @Transactional
    public void requestPasswordReset(RequestPasswordReset req) {

        Optional<User> opt = userRepository.findByUsername(req.getLogin())
                .or(() -> userRepository.findByEmail(req.getLogin()));

        opt.ifPresent(user -> {

            UUID tokenId = UUID.randomUUID();
            String code = generateNumericCode(8);
            String codeHash = passwordEncoder.encode(code);

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .id(tokenId)
                    .user(user)
                    .token(codeHash)
                    .expiresAt(OffsetDateTime.now().plusMinutes(10))
                    .build();

            passwordResetTokenRepository.save(resetToken);

            System.out.println(
                    "DEV reset token for " + user.getUsername()
                            + " -> " + tokenId + "." + code
            );
        });
    }


    /**
     * Password reset by token (<uuid>.<code>)
     */
    @Transactional
    public void resetPassword(ResetPassword req) {

        // Parse incoming token in the format: {uuid}.{code}
        String[] parts = req.getCode().split("\\.", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("invalid reset token format");
        }

        UUID tokenId = UUID.fromString(parts[0]);
        String code = parts[1];

        // Load reset token record from the database
        PasswordResetToken token = passwordResetTokenRepository.findById(tokenId)
                .orElseThrow(() -> new IllegalArgumentException("invalid reset token"));

        // Verify expiration time
        if (OffsetDateTime.now().isAfter(token.getExpiresAt())) {
            // Remove expired token for safety
            passwordResetTokenRepository.delete(token);
            throw new IllegalStateException("reset token expired");
        }

        // Verify provided code (matches bcrypt hash stored in DB)
        if (!passwordEncoder.matches(code, token.getToken())) {
            throw new IllegalArgumentException("invalid reset code");
        }

        // Update user password
        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        user.setPasswordChangedAt(OffsetDateTime.now());
        userRepository.save(user);

        // Remove used token (one-time token)
        passwordResetTokenRepository.delete(token);
    }


    /** Change the password for the authorized user (check the old one, set a new one) */
    @Transactional
    public void changePassword(ChangePassword req, String currentUsername) {

        // Load the currently authenticated user
        User me = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        // Validate old password
        if (!passwordEncoder.matches(req.getOldPassword(), me.getPassword())) {
            throw new IllegalArgumentException("old password mismatch");
        }

        // Update password
        me.setPassword(passwordEncoder.encode(req.getNewPassword()));
        me.setPasswordChangedAt(OffsetDateTime.now());
        userRepository.save(me);

        // Invalidate all existing password reset tokens for this user
        // (for security: changing password should disable all active reset tokens)
        passwordResetTokenRepository.deleteAllByUserId(me.getId());
    }


    private static String generateNumericCode(int len) {
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(rnd.nextInt(10));
        return sb.toString();
    }
}
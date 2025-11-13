package com.tsystem.controller;

import com.tsystem.exception.UnauthorizedException;
import com.tsystem.model.dto.*;
import com.tsystem.model.dto.request.LoginRequest;
import com.tsystem.model.dto.request.RegisterRequest;
import com.tsystem.model.dto.response.TokenResponse;
import com.tsystem.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> authenticate(
            @RequestBody LoginRequest loginRequest
    ) {
        return ResponseEntity.ok(authService.authenticate(loginRequest));
    }

    @PostMapping("/request-password-reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void requestReset(@RequestBody RequestPasswordReset req) {
        authService.requestPasswordReset(req);
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@RequestBody ResetPassword req) {
        authService.resetPassword(req);
    }

    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@RequestBody ChangePassword req,
                               @AuthenticationPrincipal UserDetails principal) {
        if (principal == null) throw new UnauthorizedException("Unauthorized");
        authService.changePassword(req, principal.getUsername());
    }

}

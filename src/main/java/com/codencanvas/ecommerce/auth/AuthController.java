package com.codencanvas.ecommerce.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codencanvas.ecommerce.auth.dto.AuthResponse;
import com.codencanvas.ecommerce.auth.dto.LoginRequest;
import com.codencanvas.ecommerce.auth.dto.RegisterRequest;
import com.codencanvas.ecommerce.user.UserPrincipal;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;
    private final RegisterService registerService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(
            @Valid @RequestBody RegisterRequest request) {

        registerService.register(request);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(
            @RequestParam String token) {
        emailVerificationService.verifyEmail(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @RequestHeader("X-Refresh-Token") String refreshToken) {

        return ResponseEntity.ok(authService.refresh(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader("X-Refresh-Token") String refreshToken) {

        String accessToken = authHeader.substring(7);
        authService.logout(accessToken, refreshToken);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        authService.logoutAll(principal.getUserId());
        return ResponseEntity.noContent().build();
    }
}
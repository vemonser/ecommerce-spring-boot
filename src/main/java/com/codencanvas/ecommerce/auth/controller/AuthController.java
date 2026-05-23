package com.codencanvas.ecommerce.auth.controller;

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
import com.codencanvas.ecommerce.auth.dto.ChangePasswordRequest;
import com.codencanvas.ecommerce.auth.dto.ForgotPasswordRequest;
import com.codencanvas.ecommerce.auth.dto.LoginRequest;
import com.codencanvas.ecommerce.auth.dto.RegisterRequest;
import com.codencanvas.ecommerce.auth.dto.ResetPasswordRequest;
import com.codencanvas.ecommerce.auth.service.AuthService;
import com.codencanvas.ecommerce.auth.service.EmailVerificationService;
import com.codencanvas.ecommerce.auth.service.PasswordResetService;
import com.codencanvas.ecommerce.auth.service.RegisterService;
import com.codencanvas.ecommerce.common.dto.ApiResponse;
import com.codencanvas.ecommerce.common.util.ResponseUtil;
import com.codencanvas.ecommerce.user.security.UserPrincipal;

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
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(
            @Valid @RequestBody RegisterRequest request) {
        registerService.register(request);
        return ResponseUtil.accepted();
    }

    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @RequestParam String token) {
        emailVerificationService.verifyEmail(token);
        return ResponseUtil.ok("Email verified successfully", null);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @RequestParam String email) {
        emailVerificationService.resendVerificationEmail(email);
        return ResponseUtil.accepted();
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseUtil.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @RequestHeader("X-Refresh-Token") String refreshToken) {
        return ResponseUtil.ok(authService.refresh(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader("X-Refresh-Token") String refreshToken) {
        String accessToken = authHeader.substring(7);
        authService.logout(accessToken, refreshToken);
        return ResponseUtil.noContent();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAll(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        authService.logoutAll(principal.getUserId());
        return ResponseUtil.noContent();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.sendResetEmail(request.getEmail());
        return ResponseUtil.accepted();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseUtil.ok("Password reset successfully", null);
    }

  
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        passwordResetService.changePassword(principal.getUser(), request);
        return ResponseUtil.ok("Password changed successfully", null);
    }

}
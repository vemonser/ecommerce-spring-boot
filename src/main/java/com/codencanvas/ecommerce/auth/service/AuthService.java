package com.codencanvas.ecommerce.auth.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.codencanvas.ecommerce.auth.dto.AuthResponse;
import com.codencanvas.ecommerce.auth.dto.LoginRequest;
import com.codencanvas.ecommerce.auth.repository.RefreshTokenRepository;
import com.codencanvas.ecommerce.auth.token.RefreshToken;
import com.codencanvas.ecommerce.common.exception.AccountLockoutException;
import com.codencanvas.ecommerce.common.exception.InvalidTokenException;
import com.codencanvas.ecommerce.security.service.JwtService;
import com.codencanvas.ecommerce.user.model.User;
import com.codencanvas.ecommerce.user.repository.UserRepository;
import com.codencanvas.ecommerce.user.security.UserPrincipal;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${application.security.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${application.security.lockout.duration-minutes:30}")
    private int lockoutDurationMinutes;

    @Value("${application.security.lockout.max-attempts:5}")
    private int maxFailedAttempts;

    // ==========================================
    // Login
    // ==========================================

    @Transactional
    public AuthResponse login(LoginRequest request) {

        User user = userRepository
                .findByEmailOrUsername(request.getIdentifier())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (user.isLocked()) {
            long remainingMinutes = java.time.Duration.between(
                    LocalDateTime.now(), user.getLockedUntil()).toMinutes();
            throw new AccountLockoutException(
                    "Account is locked. Please try again in " + remainingMinutes + " minutes.",
                    remainingMinutes);
        }

     if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            handleFailedLogin(user);
            throw new BadCredentialsException("Invalid credentials");
        }


        if (!user.isEnabled()) {
            throw new DisabledException("Please verify your email first");
        }

        user.resetFailedLogin();
        userRepository.save(user);

        return buildAuthResponse(user);
    }

    private void handleFailedLogin(User user) {
        user.incrementFailedLogin();
        userRepository.save(user);

        if (user.isLocked()) {
            log.warn("Account locked for user {} after {} failed attempts",
                    user.getEmail(), user.getFailedLoginAttempts());
        } else {
            int remaining = maxFailedAttempts - user.getFailedLoginAttempts();
            log.warn("Failed login attempt {} for user {}. {} attempts remaining before lockout",
                    user.getFailedLoginAttempts(), user.getEmail(), remaining);
        }
    }

    // ==========================================
    // Refresh
    // ==========================================

    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {

        String tokenHash = jwtService.hashToken(rawRefreshToken);

        RefreshToken storedToken = refreshTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (!storedToken.isValid()) {
            throw new InvalidTokenException("Refresh token expired or revoked");
        }

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        return buildAuthResponse(storedToken.getUser());
    }

    // ==========================================
    // Logout
    // ==========================================

    @Transactional
    public void logout(String rawAccessToken, String rawRefreshToken) {

        jwtService.blacklistToken(rawAccessToken);

        String tokenHash = jwtService.hashToken(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    @Transactional
    public void logoutAll(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    // ==========================================
    // Helper
    // ==========================================

    public AuthResponse buildAuthResponse(User user) {

        UserPrincipal principal = new UserPrincipal(user);
        String accessToken = jwtService.generateAccessToken(principal);
        String rawRefreshToken = jwtService.generateRawRefreshToken();
        String tokenHash = jwtService.hashToken(rawRefreshToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(tokenHash)
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(
                        refreshTokenExpiration / 1000))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .expiresIn(900000L)
                .build();
    }

    @Value("${application.security.jwt.access-token-expiration}")
    private long accessTokenExpiration;

}
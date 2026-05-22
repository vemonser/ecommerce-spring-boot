package com.codencanvas.ecommerce.auth;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.codencanvas.ecommerce.auth.dto.AuthResponse;
import com.codencanvas.ecommerce.auth.dto.LoginRequest;
import com.codencanvas.ecommerce.exception.InvalidTokenException;
import com.codencanvas.ecommerce.security.service.JwtService;
import com.codencanvas.ecommerce.user.User;
import com.codencanvas.ecommerce.user.UserPrincipal;
import com.codencanvas.ecommerce.user.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

// auth/AuthService.java
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${application.security.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    // ==========================================
    // Login
    // ==========================================

    @Transactional
    public AuthResponse login(LoginRequest request) {

        User user = userRepository
                .findByEmailOrUsername(request.getIdentifier())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        if (!user.isEnabled()) {
            throw new DisabledException("Please verify your email first");
        }

        return buildAuthResponse(user);
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

    private AuthResponse buildAuthResponse(User user) {

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
}
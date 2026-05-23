package com.codencanvas.ecommerce.auth.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.codencanvas.ecommerce.auth.repository.EmailVerificationTokenRepository;
import com.codencanvas.ecommerce.auth.token.EmailVerificationToken;
import com.codencanvas.ecommerce.common.exception.InvalidTokenException;
import com.codencanvas.ecommerce.infrastructure.email.EmailService;
import com.codencanvas.ecommerce.user.model.User;
import com.codencanvas.ecommerce.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationTokenRepository verificationTokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${application.frontend-url}")
    private String frontendUrl;

    @Value("${application.security.verification.token-expiry-hours:24}")
    private int tokenExpiryHours;

    @Transactional
    public void sendVerificationEmail(User user) {

        verificationTokenRepository.deleteByUserId(user.getId());

        String rawToken = UUID.randomUUID().toString();

        EmailVerificationToken token = EmailVerificationToken.builder()
                .token(rawToken)
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(tokenExpiryHours))
                .used(false)
                .build();

        verificationTokenRepository.save(token);

        String verificationLink = frontendUrl + "/verify-email?token=" + rawToken;

        emailService.sendVerificationEmail(
                user.getEmail(),
                user.getFullName(),
                verificationLink);

        log.info("Verification email sent to {}", user.getEmail());

    }

    // ==========================================
    // Resend
    // ==========================================
    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidTokenException("No account found with this email"));

        if (user.isEnabled()) {
            throw new InvalidTokenException("Account is already verified");
        }

        sendVerificationEmail(user);
    }

    // ==========================================
    // Verify
    // ==========================================
    @Transactional
    public void verifyEmail(String rawToken) {
        EmailVerificationToken token = verificationTokenRepository
                .findByToken(rawToken)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired verification token"));

        if (!token.isValid()) {
            throw new InvalidTokenException("Token expired or already used");
        }

        User user = token.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        token.setUsed(true);
        verificationTokenRepository.save(token);

        // Cleanup: امسح كل tokens القديمة للـ user ده
        verificationTokenRepository.deleteByUserId(user.getId());

        log.info("Email verified successfully for {}", user.getEmail());

    }
}

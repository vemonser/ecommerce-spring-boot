package com.codencanvas.ecommerce.auth;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.codencanvas.ecommerce.exception.InvalidTokenException;
import com.codencanvas.ecommerce.user.User;
import com.codencanvas.ecommerce.user.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationTokenRepository verificationTokenRepository;
    private final UserRepository userRepository;
    private final emailService emailService;

    @Value("${application.frontend-url}")
    private String frontendUrl;

    @Transactional
    public void sendVerificationEmail(User user) {

        String rawToken = UUID.randomUUID().toString();

        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .token(rawToken)
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();

        verificationTokenRepository.save(verificationToken);

        String verificationLink = frontendUrl +
                "/verify-email?token=" + rawToken;

        emailService.sendVerificationEmail(
                user.getEmail(),
                user.getFullName(),
                verificationLink);
    }

    @Transactional
    public void verifyEmail(String rawToken) {

        EmailVerificationToken token = verificationTokenRepository
                .findByToken(rawToken)
                .orElseThrow(() -> new InvalidTokenException(
                        "Invalid verification token"));

        if (!token.isValid()) {
            throw new InvalidTokenException("Token expired or already used");
        }

        User user = token.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        token.setUsed(true);
        verificationTokenRepository.save(token);
    }
}

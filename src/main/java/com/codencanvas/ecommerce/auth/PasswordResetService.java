package com.codencanvas.ecommerce.auth;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.codencanvas.ecommerce.auth.dto.ChangePasswordRequest;
import com.codencanvas.ecommerce.auth.dto.ResetPasswordRequest;
import com.codencanvas.ecommerce.exception.InvalidTokenException;
import com.codencanvas.ecommerce.exception.WeakPasswordException;
import com.codencanvas.ecommerce.infrastructure.email.EmailService;
import com.codencanvas.ecommerce.user.User;
import com.codencanvas.ecommerce.user.UserRepository;
import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository resetTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${application.frontend-url}")
    private String frontendUrl;

    @Value("${application.security.reset-token-expiry-hours:1}")
    private int tokenExpiryHours;

    // ==========================================
    // 1. Forgot Password — بعت الإيميل
    // ==========================================
    @Transactional
    public void sendResetEmail(String email) {
        User user = userRepository.findByEmail(email).orElse(null);

        // مهم جداً: نرجّع نفس الـ message سواء الإيميل موجود أو لأ
        // عشان محدش يستخدم الـ API عشان يعرف إيه الإيميلات المسجلة
        if (user == null) {
            log.warn("Password reset requested for non-existent email: {}", email);
            return; // <-- ساكت. مش نرمي exception.
        }

        // امسح أي tokens قديمة
        resetTokenRepository.deleteByUserId(user.getId());

        String rawToken = UUID.randomUUID().toString();

        PasswordResetToken token = PasswordResetToken.builder()
                .token(rawToken)
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(tokenExpiryHours))
                .used(false)
                .build();

        resetTokenRepository.save(token);

        String resetLink = frontendUrl + "/reset-password?token=" + rawToken;

        emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetLink);

        log.info("Password reset email sent to {}", email);
    }

    // ==========================================
    // 2. Reset Password — غيّر الباسورد
    // ==========================================
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // 1. التحقق من الـ token
        PasswordResetToken token = resetTokenRepository
                .findByToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));

        if (!token.isValid()) {
            throw new InvalidTokenException("Reset token expired or already used");
        }

        // 2. التحقق من تطابق الباسورد
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new WeakPasswordException("Passwords do not match");
        }

        // 3. قوّة الباسورد
        validatePasswordStrength(request.getNewPassword());

        // 4. غيّر الباسورد
        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // 5. عطّل الـ token
        token.setUsed(true);
        resetTokenRepository.save(token);

        // 6. امسح كل refresh tokens القديمة (force logout from all devices)
        // لو عندك RefreshTokenRepository injectه هنا واعمل deleteByUserId

        log.info("Password reset successful for {}", user.getEmail());
    }

    // ==========================================
    // 3. Change Password (وهو logged in)
    // ==========================================
    @Transactional
    public void changePassword(User user, ChangePasswordRequest request) {
        // 1. التحقق من الباسورد الحالي
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        // 2. التأكد إن الجديد مش زي القديم
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new WeakPasswordException("New password cannot be the same as the old password");
        }

        // 3. التحقق من التطابق
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new WeakPasswordException("New passwords do not match");
        }

        // 4. قوّة الباسورد
        validatePasswordStrength(request.getNewPassword());

        // 5. حدّث الباسورد
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // 6. بعت إيميل إشعار
        emailService.sendPasswordChangedNotification(user.getEmail(), user.getFullName());

        log.info("Password changed successfully for user {}", user.getEmail());
    }

    private void validatePasswordStrength(String password) {
        Zxcvbn zxcvbn = new Zxcvbn();
        Strength strength = zxcvbn.measure(password);

        if (strength.getScore() < 2) {
            String suggestion = strength.getFeedback()
                    .getSuggestions()
                    .stream()
                    .findFirst()
                    .orElse("Please choose a stronger password");

            throw new WeakPasswordException(suggestion);
        }
    }

}

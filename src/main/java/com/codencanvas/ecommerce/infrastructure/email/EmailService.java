package com.codencanvas.ecommerce.infrastructure.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine; // <-- Thymeleaf

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${application.frontend-url}")
    private String frontendUrl;

    @Async
    public void sendVerificationEmail(String to, String fullName, String verificationLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Thymeleaf context
            Context context = new Context();
            context.setVariable("fullName", fullName);
            context.setVariable("verificationLink", verificationLink);
            context.setVariable("expiryHours", "24");
            context.setVariable("logoUrl", frontendUrl + "/assets/logo.png");
            context.setVariable("supportEmail", fromEmail);

            // Render template
            String htmlContent = templateEngine.process("email/verification", context);

            helper.setFrom(fromEmail, "CodeNCanvas");
            helper.setTo(to);
            helper.setSubject("Verify Your Email Address");
            helper.setText(htmlContent, true); // <-- true = HTML

            mailSender.send(message);
            log.info("HTML verification email sent to {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send verification email to {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Async
    public void sendPasswordResetEmail(String to, String fullName, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            context.setVariable("fullName", fullName);
            context.setVariable("resetLink", resetLink);
            context.setVariable("expiryHours", String.valueOf(1)); // 1 hour
            context.setVariable("supportEmail", fromEmail);

            String htmlContent = templateEngine.process("email/password-reset", context);

            helper.setFrom(fromEmail, "CodeNCanvas");
            helper.setTo(to);
            helper.setSubject("Reset Your Password");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Password reset email sent to {}", to);

        } catch (Exception e) {
            log.error("Failed to send password reset email to {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

        @Async
    public void sendPasswordChangedNotification(String to, String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            context.setVariable("fullName", fullName);
            context.setVariable("changedAt", java.time.LocalDateTime.now().toString());
            context.setVariable("supportEmail", fromEmail);

            String htmlContent = templateEngine.process("email/password-changed", context);

            helper.setFrom(fromEmail, "CodeNCanvas Security");
            helper.setTo(to);
            helper.setSubject("Your Password Was Changed");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Password change notification sent to {}", to);

        } catch (Exception e) {
            log.error("Failed to send password change notification to {}", to, e);
            // ماترميش exception هنا — الـ user غيّر باسورد successfully، الإيميل بس ماتبعتش
        }
    }
}

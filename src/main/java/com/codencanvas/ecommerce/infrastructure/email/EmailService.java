package com.codencanvas.ecommerce.infrastructure.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async 
    public void sendVerificationEmail(String to, String fullName, String verificationLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Verify Your Email - CodeNCanvas");
        message.setText(String.format(
            "Hi %s,\n\nPlease verify your email by clicking the link below:\n%s\n\n" +
            "This link expires in 24 hours.\n\nIf you didn't create an account, ignore this email.",
            fullName, verificationLink
        ));
        
        mailSender.send(message);
    }
}

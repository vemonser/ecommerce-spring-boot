package com.codencanvas.ecommerce.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.codencanvas.ecommerce.auth.dto.RegisterRequest;
import com.codencanvas.ecommerce.common.exception.EmailAlreadyExistsException;
import com.codencanvas.ecommerce.common.exception.UsernameAlreadyExistsException;
import com.codencanvas.ecommerce.common.exception.WeakPasswordException;
import com.codencanvas.ecommerce.user.model.Role;
import com.codencanvas.ecommerce.user.model.User;
import com.codencanvas.ecommerce.user.repository.UserRepository;
import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegisterService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    @Transactional
    public void register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already in use");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException("Username already in use");
        }

        validatePasswordStrength(request.getPassword());

        User user = User.builder()
                .fullName(request.getFullName())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.CUSTOMER)
                .enabled(false)
                .build();

        userRepository.save(user);

        emailVerificationService.sendVerificationEmail(user);
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

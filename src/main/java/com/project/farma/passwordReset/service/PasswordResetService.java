package com.project.farma.passwordReset.service;

import com.project.farma.common.event.service.EmailService;
import com.project.farma.passwordReset.model.PasswordResetToken;
import com.project.farma.passwordReset.repository.PasswordResetTokenRepository;
import com.project.farma.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.frontend.url:https://www.farma.com.ng}")
    private String frontendUrl;

    @Transactional
    public void createPasswordResetTokenForUser(String email) {
        log.info("TRACKING: 1. Forgot Password requested for email: '{}'", email);

        userRepository.findByEmail(email).ifPresentOrElse(user -> {
            log.info("TRACKING: 2. User FOUND in database! Generating secure token...");
            tokenRepository.deleteByEmail(email);

            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(token)
                    .email(email)
                    .expiryDate(LocalDateTime.now().plusMinutes(15))
                    .build();

            tokenRepository.save(resetToken);

            String resetUrl = frontendUrl + "/auth/reset-password?token=" + token;
            log.info("TRACKING: 3. Token saved. Handing over to EmailService with URL: {}", resetUrl);

            emailService.sendPasswordResetEmail(email, resetUrl);

        }, () -> {
            log.error("TRACKING: ❌ FAILED! User NOT FOUND in database for email: '{}'. Request aborted silently.", email);
        });
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token).orElse(null);

        if (resetToken == null || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }

        userRepository.findByEmail(resetToken.getEmail()).ifPresent(user -> {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        });

        tokenRepository.delete(resetToken);
        return true;
    }
}
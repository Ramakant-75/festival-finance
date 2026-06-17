package com.example.societyfest.service;

import com.example.societyfest.entity.PasswordResetToken;
import com.example.societyfest.entity.User;
import com.example.societyfest.repository.PasswordResetTokenRepository;
import com.example.societyfest.repository.UserRepository;
import com.example.societyfest.service.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ForgotPasswordService {

    private final UserRepository userRepo;
    private final PasswordResetTokenRepository tokenRepo;
    private final PasswordEncoder encoder;
    private final EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public void sendResetLink(String mailId) {

        Optional<User> userOpt =
                userRepo.findByMailId(mailId);

        if (userOpt.isEmpty()) {
            return;
        }

        User user = userOpt.get();

        tokenRepo.deleteByUser(user);

        String token =
                UUID.randomUUID().toString();

        PasswordResetToken resetToken =
                PasswordResetToken.builder()
                        .token(token)
                        .expiryTime(
                                LocalDateTime.now()
                                        .plusMinutes(15))
                        .used(false)
                        .user(user)
                        .build();

        tokenRepo.save(resetToken);

        String link =
                frontendUrl
                        + "/reset-password?token="
                        + token;

        emailService.sendResetMail(
                user.getMailId(),
                link
        );
    }

    public void resetPassword(
            String token,
            String newPassword) {

        PasswordResetToken resetToken =
                tokenRepo.findByToken(token)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid token"));

        if (resetToken.isUsed()) {
            throw new RuntimeException(
                    "Token already used");
        }

        if (resetToken.getExpiryTime()
                .isBefore(LocalDateTime.now())) {

            throw new RuntimeException(
                    "Token expired");
        }

        User user =
                resetToken.getUser();

        user.setPassword(
                encoder.encode(newPassword));

        userRepo.save(user);

        resetToken.setUsed(true);

        tokenRepo.save(resetToken);
    }
}
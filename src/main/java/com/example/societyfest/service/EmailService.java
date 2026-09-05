package com.example.societyfest.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendResetMail(
            String to,
            String resetLink) {

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(to);
        message.setSubject("Reset Password");

        message.setText(
                "Click below link to reset password:\n"
                        + resetLink
        );

        mailSender.send(message);
    }
}
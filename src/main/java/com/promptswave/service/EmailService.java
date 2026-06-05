package com.promptswave.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String to, String token) {
        String url = baseUrl + "/api/auth/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Verify your PromptVault Account");
        message.setText(
            "Welcome to PromptVault!\n\n" +
            "Please click the link below to verify your email address:\n\n" +
            url + "\n\n" +
            "This link expires in 24 hours.\n\n" +
            "If you did not create an account, you can safely ignore this email."
        );
        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String to, String token) {
        String url = frontendUrl + "/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Reset your PromptVault Password");
        message.setText(
            "You requested a password reset for your PromptVault account.\n\n" +
            "Click the link below to set a new password:\n\n" +
            url + "\n\n" +
            "This link expires in 1 hour.\n\n" +
            "If you did not request this, you can safely ignore this email."
        );
        mailSender.send(message);
    }

    public void sendEmailChangeVerification(String to, String token) {
        String url = baseUrl + "/api/auth/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Verify your new email - PromptVault");
        message.setText(
            "You requested an email address change on PromptVault.\n\n" +
            "Please verify your new email address by clicking the link below:\n\n" +
            url + "\n\n" +
            "This link expires in 24 hours."
        );
        mailSender.send(message);
    }
}

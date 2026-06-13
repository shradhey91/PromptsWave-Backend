package com.promptswave.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.promptswave.entity.ContactMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String to, String token) {
        String url = baseUrl + "/api/auth/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject("Verify your PromptsWave Account");
        message.setText(
                "Welcome to PromptsWave!\n\n" +
                        "Please click the link below to verify your email address:\n\n" +
                        url + "\n\n" +
                        "This link expires in 24 hours.\n\n" +
                        "If you did not create an account, you can safely ignore this email.");
        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String to, String token) {
        String url = frontendUrl + "/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject("Reset your PromptsWave Password");
        message.setText(
                "You requested a password reset for your PromptsWave account.\n\n" +
                        "Click the link below to set a new password:\n\n" +
                        url + "\n\n" +
                        "This link expires in 1 hour.\n\n" +
                        "If you did not request this, you can safely ignore this email.");
        mailSender.send(message);
    }

    public void sendEmailChangeVerification(String to, String token) {
        String url = baseUrl + "/api/auth/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject("Verify your new email - PromptsWave");
        message.setText(
                "You requested an email address change on PromptsWave.\n\n" +
                        "Please verify your new email address by clicking the link below:\n\n" +
                        url + "\n\n" +
                        "This link expires in 24 hours.");
        mailSender.send(message);
    }

    public void sendContactNotification(String to, ContactMessage msg) {
        String fullName = msg.getFirstName()
                + (msg.getLastName() == null || msg.getLastName().isBlank() ? "" : " " + msg.getLastName());

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(fromAddress);
        mail.setTo(to);
        mail.setReplyTo(msg.getEmail());
        mail.setSubject("[Contact] " + msg.getSubject());
        mail.setText(
                "New contact form submission on PromptsWave:\n\n" +
                        "Name:    " + fullName + "\n" +
                        "Email:   " + msg.getEmail() + "\n" +
                        "Subject: " + msg.getSubject() + "\n\n" +
                        "Message:\n" + msg.getMessage() + "\n");
        mailSender.send(mail);
    }
}
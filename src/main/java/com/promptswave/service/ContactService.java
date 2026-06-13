package com.promptswave.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promptswave.dto.request.ContactRequest;
import com.promptswave.entity.ContactMessage;
import com.promptswave.repository.ContactMessageRepo;

@Service
public class ContactService {

    private final ContactMessageRepo contactMessageRepo;
    private final EmailService emailService;

    @Value("${app.contact.recipient:support@promptswave.com}")
    private String recipient;

    public ContactService(ContactMessageRepo contactMessageRepo, EmailService emailService) {
        this.contactMessageRepo = contactMessageRepo;
        this.emailService = emailService;
    }

    @Transactional
    public String submit(ContactRequest request) {
        ContactMessage saved = contactMessageRepo.save(ContactMessage.builder()
                .firstName(request.firstName().trim())
                .lastName(request.lastName() == null ? null : request.lastName().trim())
                .email(request.email().trim())
                .subject(request.subject().trim())
                .message(request.message().trim())
                .build());

        
        try {
            emailService.sendContactNotification(recipient, saved);
        } catch (Exception ignored) {
           
        }

        return "Thanks for reaching out! We'll get back to you within 24 hours.";
    }
}
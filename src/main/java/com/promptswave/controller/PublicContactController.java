package com.promptswave.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.promptswave.dto.request.ContactRequest;
import com.promptswave.service.ContactService;

import java.util.Map;

@RestController
@RequestMapping("/api/public/contact")
@Tag(name = "Contact", description = "Public contact form submissions")
public class PublicContactController {

    private final ContactService contactService;

    public PublicContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    @Operation(summary = "Submit a contact / 'Send us a message' form")
    public ResponseEntity<Map<String, String>> submit(@Valid @RequestBody ContactRequest request) {
        String result = contactService.submit(request);
        return ResponseEntity.ok(Map.of("message", result));
    }
}
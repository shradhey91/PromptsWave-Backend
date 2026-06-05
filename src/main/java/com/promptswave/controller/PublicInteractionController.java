package com.promptswave.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.promptswave.service.UserInteractionService;

import java.util.Map;

@RestController
@RequestMapping("/api/public/interactions")
@Tag(name = "Interactions (Public)", description = "Copy tracking for guest users")
public class PublicInteractionController {

    private final UserInteractionService interactionService;

    public PublicInteractionController(UserInteractionService interactionService) {
        this.interactionService = interactionService;
    }

    @PostMapping("/copy/{promptId}")
    @Operation(summary = "Record a copy event for a guest user (no auth required)")
    public ResponseEntity<Map<String, String>> guestCopy(@PathVariable Long promptId) {
        interactionService.recordCopy(null, promptId);
        return ResponseEntity.ok(Map.of("message", "Copied successfully"));
    }
}
package com.promptswave.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.promptswave.dto.response.AiEntityResponse;
import com.promptswave.service.AiEntityService;

import java.util.List;

@RestController
@RequestMapping("/api/public/ai-entities")
@Tag(name = "AI Entities (Public)", description = "List of all AI tools available on the platform")
public class PublicAiEntityController {

    private final AiEntityService aiEntityService;

    public PublicAiEntityController(AiEntityService aiEntityService) {
        this.aiEntityService = aiEntityService;
    }

    @GetMapping
    @Operation(summary = "Get all active AI entities (for filter dropdowns)")
    public ResponseEntity<List<AiEntityResponse>> getAllActive() {
        return ResponseEntity.ok(aiEntityService.getAllActive());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get AI entity by ID")
    public ResponseEntity<AiEntityResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(aiEntityService.getById(id));
    }
}
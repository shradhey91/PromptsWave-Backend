package com.promptswave.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.promptswave.dto.request.CreateAiEntityRequest;
import com.promptswave.dto.response.AiEntityResponse;
import com.promptswave.service.AiEntityService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/ai-entities")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "AI Entities (Admin)", description = "Admin: manage AI tools list")
public class AdminAiEntityController {

    private final AiEntityService aiEntityService;

    public AdminAiEntityController(AiEntityService aiEntityService) {
        this.aiEntityService = aiEntityService;
    }

    @GetMapping
    @Operation(summary = "List all AI entities including inactive ones")
    public ResponseEntity<List<AiEntityResponse>> getAll() {
        return ResponseEntity.ok(aiEntityService.getAllForAdmin());
    }

    @PostMapping
    @Operation(summary = "Add a new AI entity")
    public ResponseEntity<AiEntityResponse> create(@Valid @RequestBody CreateAiEntityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(aiEntityService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an AI entity")
    public ResponseEntity<AiEntityResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateAiEntityRequest request) {
        return ResponseEntity.ok(aiEntityService.update(id, request));
    }

    @PatchMapping("/{id}/toggle-active")
    @Operation(summary = "Toggle active/inactive status of an AI entity")
    public ResponseEntity<Map<String, String>> toggleActive(@PathVariable Long id) {
        aiEntityService.toggleActive(id);
        return ResponseEntity.ok(Map.of("message", "AI entity status toggled"));
    }
}

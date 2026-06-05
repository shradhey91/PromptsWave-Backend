package com.promptswave.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.promptswave.dto.request.CreatePromptRequest;
import com.promptswave.dto.request.UpdatePromptRequest;
import com.promptswave.dto.response.PagedResponse;
import com.promptswave.dto.response.PromptResponse;
import com.promptswave.security.CustomUserDetails;
import com.promptswave.service.PromptService;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/prompts")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Prompts (Admin)", description = "Admin: create, edit, publish, delete prompts")
public class AdminPromptController {

    private final PromptService promptService;

    public AdminPromptController(PromptService promptService) {
        this.promptService = promptService;
    }

    @GetMapping
    @Operation(summary = "List all prompts (published + drafts) with optional search/filter")
    public ResponseEntity<PagedResponse<PromptResponse>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                promptService.getAllPromptsForAdmin(search, categoryId, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single prompt by ID (admin view)")
    public ResponseEntity<PromptResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(promptService.getPromptByIdForAdmin(id));
    }

    @PostMapping
    @Operation(summary = "Create a new prompt (as draft or published)")
    public ResponseEntity<PromptResponse> create(
            @Valid @RequestBody CreatePromptRequest request,
            Authentication authentication) {
        Long adminId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(promptService.createPrompt(request, adminId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing prompt")
    public ResponseEntity<PromptResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePromptRequest request) {
        return ResponseEntity.ok(promptService.updatePrompt(id, request));
    }

    @PatchMapping("/{id}/toggle-publish")
    @Operation(summary = "Publish or unpublish a prompt")
    public ResponseEntity<PromptResponse> togglePublish(@PathVariable Long id) {
        return ResponseEntity.ok(promptService.togglePublish(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Permanently delete a prompt")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        promptService.deletePrompt(id);
        return ResponseEntity.ok(Map.of("message", "Prompt deleted successfully"));
    }
}

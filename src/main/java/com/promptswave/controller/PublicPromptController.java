package com.promptswave.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.promptswave.dto.response.PagedResponse;
import com.promptswave.dto.response.PromptResponse;
import com.promptswave.dto.response.PromptSummaryResponse;
import com.promptswave.service.PromptService;

@RestController
@RequestMapping("/api/public/prompts")
@Tag(name = "Prompts (Public)", description = "Browse, search, and filter AI prompts")
public class PublicPromptController {

    private final PromptService promptService;

    public PublicPromptController(PromptService promptService) {
        this.promptService = promptService;
    }

    @GetMapping
    @Operation(summary = "Browse all published prompts with optional filters and sorting")
    public ResponseEntity<PagedResponse<PromptSummaryResponse>> browsePrompts(

            @Parameter(description = "Sort order: newest | mostLiked | mostUsed | trending") @RequestParam(defaultValue = "newest") String sort,

            @Parameter(description = "Filter by category ID (includes subcategories)") @RequestParam(required = false) Long categoryId,

            @Parameter(description = "Filter by recommended AI entity ID") @RequestParam(required = false) Long aiEntityId,

            @Parameter(description = "Search keyword") @RequestParam(required = false) String search,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                promptService.browsePrompts(sort, categoryId, aiEntityId, search, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get full details of a single published prompt")
    public ResponseEntity<PromptResponse> getPrompt(@PathVariable Long id) {
        return ResponseEntity.ok(promptService.getPublishedPromptById(id));
    }

    @GetMapping("/hero")
    @Operation(summary = "Prompts for the homepage hero section "
            + "(admin-pinned first, then most liked/copied)")
    public ResponseEntity<java.util.List<PromptSummaryResponse>> heroPrompts(
            @Parameter(description = "How many prompts to show in the hero") @RequestParam(defaultValue = "6") int limit,
            @Parameter(description = "Auto-fill ranking: liked | copied") @RequestParam(defaultValue = "liked") String sortBy) {
        return ResponseEntity.ok(promptService.getHeroPrompts(limit, sortBy));
    }
}
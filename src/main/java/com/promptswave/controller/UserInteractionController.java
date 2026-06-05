package com.promptswave.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.promptswave.dto.response.CopyHistoryResponse;
import com.promptswave.dto.response.InteractionStatusResponse;
import com.promptswave.dto.response.PagedResponse;
import com.promptswave.dto.response.PromptSummaryResponse;
import com.promptswave.security.CustomUserDetails;
import com.promptswave.service.UserInteractionService;

import java.util.Map;

@RestController
@RequestMapping("/api/user/interactions")
@Tag(name = "User Interactions", description = "Like, save, and copy prompts — user library management")
public class UserInteractionController {

    private final UserInteractionService interactionService;

    public UserInteractionController(UserInteractionService interactionService) {
        this.interactionService = interactionService;
    }

    @GetMapping("/status/{promptId}")
    @Operation(summary = "Get current like/save status and counters for a prompt")
    public ResponseEntity<InteractionStatusResponse> getStatus(
            @PathVariable Long promptId,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(interactionService.getStatus(userId, promptId));
    }

    // LIKE / UNLIKE

    @PostMapping("/like/{promptId}")
    @Operation(summary = "Toggle like on a prompt (like if not liked, unlike if already liked)")
    public ResponseEntity<InteractionStatusResponse> toggleLike(
            @PathVariable Long promptId,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(interactionService.toggleLike(userId, promptId));
    }

    // SAVE / UNSAVE

    @PostMapping("/save/{promptId}")
    @Operation(summary = "Toggle save on a prompt (save if not saved, unsave if already saved)")
    public ResponseEntity<InteractionStatusResponse> toggleSave(
            @PathVariable Long promptId,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(interactionService.toggleSave(userId, promptId));
    }

    // COPY
    // Authenticated users — copy is tracked against their account

    @PostMapping("/copy/{promptId}")
    @Operation(summary = "Record a copy event for a prompt (authenticated user)")
    public ResponseEntity<Map<String, String>> copy(
            @PathVariable Long promptId,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        interactionService.recordCopy(userId, promptId);
        return ResponseEntity.ok(Map.of("message", "Copied successfully"));
    }

    // USER LIBRARY

    @GetMapping("/liked")
    @Operation(summary = "Get all prompts liked by the current user")
    public ResponseEntity<PagedResponse<PromptSummaryResponse>> getLikedPrompts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(interactionService.getLikedPrompts(userId, page, size));
    }

    @GetMapping("/saved")
    @Operation(summary = "Get all prompts saved by the current user")
    public ResponseEntity<PagedResponse<PromptSummaryResponse>> getSavedPrompts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(interactionService.getSavedPrompts(userId, page, size));
    }

    @GetMapping("/history")
    @Operation(summary = "Get the current user's prompt copy history")
    public ResponseEntity<PagedResponse<CopyHistoryResponse>> getCopyHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(interactionService.getCopyHistory(userId, page, size));
    }

    // HELPER

    private Long getUserId(Authentication authentication) {
        return ((CustomUserDetails) authentication.getPrincipal()).getId();
    }
}

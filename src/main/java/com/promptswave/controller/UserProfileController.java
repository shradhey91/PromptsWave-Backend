package com.promptswave.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.promptswave.dto.request.UpdateProfileRequest;
import com.promptswave.dto.response.UserProfileResponse;
import com.promptswave.security.CustomUserDetails;
import com.promptswave.service.UserProfileService;

import java.util.Map;

@RestController
@RequestMapping("/api/users/me")
@Tag(name = "User Profile", description = "Manage the logged-in user's profile")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping
    @Operation(summary = "Get current user's profile")
    public ResponseEntity<UserProfileResponse> getProfile(Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        return ResponseEntity.ok(userProfileService.getProfile(userId));
    }

    @PutMapping
    @Operation(summary = "Update name, profile icon, and country")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        return ResponseEntity.ok(userProfileService.updateProfile(
                userId,
                request.name(),
                request.profileIconUrl(),
                request.country()));
    }

    @DeleteMapping
    @Operation(summary = "Deactivate and soft-delete the current user's account")
    public ResponseEntity<Map<String, String>> deleteAccount(Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        userProfileService.deleteAccount(userId, null);
        return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
    }
}

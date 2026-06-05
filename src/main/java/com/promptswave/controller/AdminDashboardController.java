package com.promptswave.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.promptswave.dto.response.AdminUserResponse;
import com.promptswave.dto.response.DashboardStatsResponse;
import com.promptswave.dto.response.PagedResponse;
import com.promptswave.dto.response.TopPromptResponse;
import com.promptswave.service.AdminDashboardService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Dashboard", description = "Platform analytics, top prompts, user management")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    public AdminDashboardController(AdminDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // DASHBOARD STATS

    @GetMapping("/dashboard/stats")
    @Operation(summary = "Get platform-wide stats: users, prompts, likes, copies")
    public ResponseEntity<DashboardStatsResponse> getStats() {
        return ResponseEntity.ok(dashboardService.getDashboardStats());
    }

    // TOP PROMPTS

    @GetMapping("/dashboard/top-liked")
    @Operation(summary = "Get top N most liked prompts")
    public ResponseEntity<List<TopPromptResponse>> getTopLiked(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(dashboardService.getTopLikedPrompts(limit));
    }

    @GetMapping("/dashboard/top-copied")
    @Operation(summary = "Get top N most copied/used prompts")
    public ResponseEntity<List<TopPromptResponse>> getTopCopied(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(dashboardService.getTopCopiedPrompts(limit));
    }

    // USER MANAGEMENT

    @GetMapping("/users")
    @Operation(summary = "List all users with optional search, paginated")
    public ResponseEntity<PagedResponse<AdminUserResponse>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(dashboardService.getAllUsers(search, page, size));
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get a single user's full details and activity stats")
    public ResponseEntity<AdminUserResponse> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(dashboardService.getUserById(userId));
    }

    @PatchMapping("/users/{userId}/suspend")
    @Operation(summary = "Suspend a user account (blocks login)")
    public ResponseEntity<AdminUserResponse> suspendUser(@PathVariable Long userId) {
        return ResponseEntity.ok(dashboardService.suspendUser(userId));
    }

    @PatchMapping("/users/{userId}/reactivate")
    @Operation(summary = "Reactivate a suspended user account")
    public ResponseEntity<AdminUserResponse> reactivateUser(@PathVariable Long userId) {
        return ResponseEntity.ok(dashboardService.reactivateUser(userId));
    }

    @DeleteMapping("/users/{userId}")
    @Operation(summary = "Permanently delete a user account")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long userId) {
        dashboardService.deleteUser(userId);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }
}
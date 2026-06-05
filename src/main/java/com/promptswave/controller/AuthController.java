package com.promptswave.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.promptswave.dto.request.ChangeEmailRequest;
import com.promptswave.dto.request.ChangePasswordRequest;
import com.promptswave.dto.request.ForgotPasswordRequest;
import com.promptswave.dto.request.LoginRequest;
import com.promptswave.dto.request.RegisterRequest;
import com.promptswave.dto.request.ResetPasswordRequest;
import com.promptswave.dto.response.AuthResponse;
import com.promptswave.security.CustomUserDetails;
import com.promptswave.service.AuthService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User registration, login, email verification and password management")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        String result = authService.register(request);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive JWT tokens")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request.email(), request.password());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and invalidate the current access token")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authService.logout(authHeader.substring(7));
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Get a new access token using a refresh token")
    public ResponseEntity<Map<String, String>> refresh(
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Missing refresh token"));
        }
        Map<String, String> tokens = authService.refresh(authHeader.substring(7));
        return ResponseEntity.ok(tokens);
    }

    @GetMapping("/verify")
    @Operation(summary = "Verify email address via token sent to email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam("token") String token) {
        String result = authService.verifyEmail(token);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend the email verification link")
    public ResponseEntity<Map<String, String>> resendVerification(
            @RequestBody Map<String, String> body) {
        String result = authService.resendVerificationEmail(body.get("email"));
        return ResponseEntity.ok(Map.of("message", result));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request a password reset link via email")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        String result = authService.forgotPassword(request.email());
        return ResponseEntity.ok(Map.of("message", result));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using the token from email")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        String result = authService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(Map.of("message", result));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password for the logged-in user")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        String result = authService.changePassword(userId, request.currentPassword(), request.newPassword());
        return ResponseEntity.ok(Map.of("message", result));
    }

    @PostMapping("/change-email")
    @Operation(summary = "Change email address for the logged-in user")
    public ResponseEntity<Map<String, String>> changeEmail(
            @Valid @RequestBody ChangeEmailRequest request,
            Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        String result = authService.changeEmail(userId, request.newEmail(), request.password());
        return ResponseEntity.ok(Map.of("message", result));
    }
}

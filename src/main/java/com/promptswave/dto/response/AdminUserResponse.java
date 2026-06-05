package com.promptswave.dto.response;

import java.time.LocalDateTime;

public record AdminUserResponse(
    Long id,
    String name,
    String email,
    String country,
    String referralSource,
    String role,
    Boolean isEmailVerified,
    Boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime lastLoginAt,
    long likedPromptsCount,
    long savedPromptsCount,
    long copyEventsCount
) {}
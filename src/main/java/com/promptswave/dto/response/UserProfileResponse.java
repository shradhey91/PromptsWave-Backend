package com.promptswave.dto.response;

import java.time.LocalDateTime;

public record UserProfileResponse(
        Long id,
        String name,
        String email,
        String profileIconUrl,
        String country,
        String referralSource,
        String role,
        Boolean isEmailVerified,
        LocalDateTime createdAt,
        LocalDateTime lastLoginAt) {
}

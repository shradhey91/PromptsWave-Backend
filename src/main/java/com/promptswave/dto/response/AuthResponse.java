package com.promptswave.dto.response;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserProfileResponse user) {
}

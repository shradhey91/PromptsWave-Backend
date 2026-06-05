package com.promptswave.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(
        @NotBlank(message = "Name is required") String name,
        String profileIconUrl,
        String country) {
}

package com.promptswave.dto.response;

public record AiEntityResponse(
        Long id,
        String name,
        String slug,
        String iconUrl,
        String description,
        String websiteUrl,
        Boolean isActive) {
}

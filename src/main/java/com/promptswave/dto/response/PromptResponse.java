package com.promptswave.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record PromptResponse(
        Long id,
        String title,
        String promptText,
        String description,
        String imageUrl,
        CategoryResponse category,
        List<RankedAiEntityResponse> recommendedAiEntities,
        Integer timesCopied,
        Integer likesCount,
        Boolean isPublished,
        String uploadedBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
    public record RankedAiEntityResponse(
            Long id,
            String name,
            String slug,
            String iconUrl,
            Integer rank) {
    }
}

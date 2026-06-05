package com.promptswave.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record PromptSummaryResponse(
        Long id,
        String title,
        String description,
        String imageUrl,
        String categoryName,
        String categorySlug,
        List<String> recommendedAiNames,
        Integer timesCopied,
        Integer likesCount,
        LocalDateTime createdAt) {
}

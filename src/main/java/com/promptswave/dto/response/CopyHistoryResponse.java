package com.promptswave.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record CopyHistoryResponse(
    Long promptId,
    String promptTitle,
    String promptDescription,
    String imageUrl,
    String categoryName,
    String categorySlug,
    List<String> recommendedAiNames,
    LocalDateTime copiedAt
) {}
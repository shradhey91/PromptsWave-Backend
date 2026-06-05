package com.promptswave.dto.response;

public record TopPromptResponse(
    Long id,
    String title,
    String categoryName,
    String imageUrl,
    int likesCount,
    int timesCopied
) {}
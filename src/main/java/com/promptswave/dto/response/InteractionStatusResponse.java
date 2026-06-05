package com.promptswave.dto.response;

public record InteractionStatusResponse(
    Long promptId,
    boolean liked,
    boolean saved,
    int totalLikes,
    int totalCopies
) {}
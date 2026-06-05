package com.promptswave.dto.response;

public record DashboardStatsResponse(
    long totalUsers,
    long totalPrompts,
    long publishedPrompts,
    long draftPrompts,
    long totalCategories,
    long totalAiEntities,
    long totalLikes,
    long totalCopies
) {}
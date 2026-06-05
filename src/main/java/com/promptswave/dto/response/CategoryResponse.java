package com.promptswave.dto.response;

public record CategoryResponse(
        Long id,
        String name,
        String slug,
        String description,
        String iconUrl,
        Integer level,
        Integer sortOrder,
        Boolean isActive,
        Long parentId,
        String parentName,
        long promptCount) {
}

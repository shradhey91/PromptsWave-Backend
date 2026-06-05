package com.promptswave.dto.response;

import java.util.List;

public record CategoryTreeResponse(
        Long id,
        String name,
        String slug,
        String description,
        String iconUrl,
        Integer sortOrder,
        long promptCount,
        List<CategoryResponse> subcategories) {
}
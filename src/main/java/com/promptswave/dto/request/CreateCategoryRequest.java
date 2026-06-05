package com.promptswave.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateCategoryRequest(

        @NotBlank(message = "Category name is required") String name,

        @NotBlank(message = "Slug is required") String slug,

        String description,

        String iconUrl,

        Long parentId,

        Integer sortOrder) {
}

package com.promptswave.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateCategoryRequest(

        @NotBlank(message = "Category name is required") String name,

        String description,

        String iconUrl,

        Integer sortOrder,

        Boolean isActive) {
}

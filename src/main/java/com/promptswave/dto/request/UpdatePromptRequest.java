package com.promptswave.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdatePromptRequest(

        @NotBlank(message = "Title is required") String title,

        @NotBlank(message = "Prompt text is required") String promptText,

        String description,

        @NotNull(message = "Category is required") Long categoryId,

        String imageUrl,

        @NotEmpty(message = "At least one recommended AI is required") List<Long> recommendedAiEntityIds,

        Boolean isPublished) {
}

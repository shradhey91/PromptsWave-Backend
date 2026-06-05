package com.promptswave.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateAiEntityRequest(

        @NotBlank(message = "AI name is required") String name,

        @NotBlank(message = "Slug is required") String slug,

        String iconUrl,

        String description,

        String websiteUrl) {
}

package com.promptswave.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(

        @NotBlank(message = "Google authorization code is required") String code,

        String referralSource) {
}
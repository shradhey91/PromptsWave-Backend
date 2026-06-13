package com.promptswave.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactRequest(

        @NotBlank(message = "First name is required") @Size(max = 100, message = "First name is too long") String firstName,

        @Size(max = 100, message = "Last name is too long") String lastName,

        @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,

        @NotBlank(message = "Subject is required") @Size(max = 200, message = "Subject is too long") String subject,

        @NotBlank(message = "Message is required") @Size(max = 5000, message = "Message is too long") String message) {
}
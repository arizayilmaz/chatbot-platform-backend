package com.aryil.chatbot.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "User registration request")
public record RegisterRequest(
                @Schema(description = "User email address", example = "user@example.com") @Email(message = "Please provide a valid email address") @NotBlank(message = "Email is required") String email,

                @Schema(description = "User password", example = "securePassword123") @NotBlank(message = "Password is required") @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters") String password) {
}

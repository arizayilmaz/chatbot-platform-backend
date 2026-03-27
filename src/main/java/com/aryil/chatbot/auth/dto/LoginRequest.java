package com.aryil.chatbot.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "User login request")
public record LoginRequest(
                @Schema(description = "User email address", example = "user@example.com") @Email(message = "Please provide a valid email address") @NotBlank(message = "Email is required") String email,

                @Schema(description = "User password", example = "securePassword123") @NotBlank(message = "Password is required") String password) {
}

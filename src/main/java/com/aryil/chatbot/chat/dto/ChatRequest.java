package com.aryil.chatbot.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

@Schema(description = "Chat message request")
public record ChatRequest(
                @Schema(description = "Existing conversation ID (null to start new conversation)", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890", nullable = true) UUID conversationId,

                @Schema(description = "User message content", example = "Hello, how can you help me today?") @NotBlank(message = "Message is required") String message) {
}

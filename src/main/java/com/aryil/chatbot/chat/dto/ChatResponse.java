package com.aryil.chatbot.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Chat response from assistant")
public record ChatResponse(
                @Schema(description = "Conversation ID", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890") UUID conversationId,

                @Schema(description = "Assistant reply message", example = "Hello! I'm here to help. What would you like to know?") String reply,

                @Schema(description = "Whether the message was blocked by content filter", example = "false") boolean blocked,

                @Schema(description = "Reason for blocking (if blocked)", example = "null", nullable = true) String reason) {
}

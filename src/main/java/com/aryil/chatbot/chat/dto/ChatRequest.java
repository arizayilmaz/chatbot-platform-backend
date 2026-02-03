package com.aryil.chatbot.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ChatRequest(
        UUID conversationId,
        @NotBlank String message
) {}

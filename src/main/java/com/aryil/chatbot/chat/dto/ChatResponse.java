package com.aryil.chatbot.chat.dto;

import java.util.UUID;

public record ChatResponse(
        UUID conversationId,
        String reply,
        boolean blocked,
        String reason
) {}

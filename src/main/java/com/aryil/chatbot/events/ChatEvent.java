package com.aryil.chatbot.events;

import java.time.Instant;
import java.util.UUID;

public record ChatEvent(
        String type,
        UUID userId,
        UUID conversationId,
        UUID messageId,
        boolean blocked,
        String reason,
        Instant at
) {}

package com.aryil.chatbot.chat.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MessageDto(
        UUID id,
        String role,
        String content,
        OffsetDateTime createdAt,
        boolean blocked,
        String blockedReason
) {}

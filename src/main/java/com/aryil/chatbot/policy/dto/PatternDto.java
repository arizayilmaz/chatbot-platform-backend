package com.aryil.chatbot.policy.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PatternDto(
        UUID id,
        String pattern,
        String type,
        boolean enabled,
        String severity,
        String notes,
        OffsetDateTime createdAt
) {}

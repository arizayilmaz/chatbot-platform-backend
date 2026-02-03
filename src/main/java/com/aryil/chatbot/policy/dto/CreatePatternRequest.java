package com.aryil.chatbot.policy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreatePatternRequest(
        @NotBlank String pattern,
        @NotBlank @Pattern(regexp = "KEYWORD|REGEX") String type,
        @NotBlank @Pattern(regexp = "LOW|MEDIUM|HIGH") String severity,
        String notes
) {}

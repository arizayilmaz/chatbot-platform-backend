package com.aryil.chatbot.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standardized error response")
public record ApiError(
        @Schema(description = "Timestamp of the error", example = "2026-02-11T16:30:00Z") Instant timestamp,

        @Schema(description = "Request path that caused the error", example = "/api/auth/login") String path,

        @Schema(description = "Error code for programmatic handling", example = "INVALID_CREDENTIALS", allowableValues = {
                "VALIDATION_ERROR", "INVALID_CREDENTIALS", "ACCESS_DENIED",
                "EMAIL_EXISTS", "TOKEN_EXPIRED", "TOKEN_INVALID",
                "CONVERSATION_NOT_FOUND", "PATTERN_DUPLICATE", "CONTENT_BLOCKED" }) String code,

        @Schema(description = "Human-readable error message", example = "Invalid email or password") String message,

        @Schema(description = "Additional error details", example = "{\"field\": \"email\", \"rejected\": \"invalid-email\"}") Map<String, Object> details) {
    public static ApiError of(String path, String code, String message) {
        return new ApiError(Instant.now(), path, code, message, null);
    }

    public static ApiError of(String path, String code, String message, Map<String, Object> details) {
        return new ApiError(Instant.now(), path, code, message, details);
    }
}

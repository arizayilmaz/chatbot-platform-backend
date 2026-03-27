package com.aryil.chatbot.policy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Request to update an existing banned pattern (all fields optional)")
public record UpdatePatternRequest(
        @Schema(description = "Pattern to match (keyword or regex)", example = "badword", nullable = true) String pattern,

        @Schema(description = "Pattern type", example = "KEYWORD", allowableValues = {
                "KEYWORD",
                "REGEX" }, nullable = true) @Pattern(regexp = "KEYWORD|REGEX", message = "Type must be KEYWORD or REGEX") String type,

        @Schema(description = "Severity level", example = "HIGH", allowableValues = { "LOW", "MEDIUM",
                "HIGH" }, nullable = true) @Pattern(regexp = "LOW|MEDIUM|HIGH", message = "Severity must be LOW, MEDIUM, or HIGH") String severity,

        @Schema(description = "Pattern enabled status", example = "true", nullable = true) Boolean enabled,

        @Schema(description = "Additional notes", example = "Updated pattern description", nullable = true) String notes) {
}

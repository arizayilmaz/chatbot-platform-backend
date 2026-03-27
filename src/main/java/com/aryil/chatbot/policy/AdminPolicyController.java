package com.aryil.chatbot.policy;

import com.aryil.chatbot.common.dto.ApiError;
import com.aryil.chatbot.policy.dto.CreatePatternRequest;
import com.aryil.chatbot.policy.dto.PatternDto;
import com.aryil.chatbot.policy.dto.UpdatePatternRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Admin policy and pattern management")
public class AdminPolicyController {

    private final BannedPatternService service;

    public AdminPolicyController(BannedPatternService service) {
        this.service = service;
    }

    @GetMapping("/patterns")
    @Operation(summary = "List all banned patterns", description = "Retrieve all content filtering patterns", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Patterns retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Admin access required", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public List<PatternDto> list() {
        return service.list();
    }

    @PostMapping("/patterns")
    @Operation(summary = "Create banned pattern", description = "Add a new content filtering pattern", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pattern created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Admin access required", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Pattern already exists", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public PatternDto create(@Valid @RequestBody CreatePatternRequest req) {
        return service.create(req);
    }

    @PatchMapping("/patterns/{id}")
    @Operation(summary = "Update banned pattern", description = "Partially update a content filtering pattern", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pattern updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Admin access required", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Pattern not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Pattern already exists", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public PatternDto update(@PathVariable UUID id, @Valid @RequestBody UpdatePatternRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/patterns/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete banned pattern", description = "Remove a content filtering pattern", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pattern deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Admin access required", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Pattern not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    @PutMapping("/patterns/{id}/toggle")
    @Operation(summary = "Toggle pattern enabled status", description = "Enable or disable a content filtering pattern", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pattern status updated"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Admin access required", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Pattern not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public PatternDto toggle(@PathVariable UUID id, @RequestParam boolean enabled) {
        return service.setEnabled(id, enabled);
    }
}

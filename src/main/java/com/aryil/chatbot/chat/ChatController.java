package com.aryil.chatbot.chat;

import com.aryil.chatbot.chat.dto.ChatRequest;
import com.aryil.chatbot.chat.dto.ChatResponse;
import com.aryil.chatbot.chat.dto.MessageDto;
import com.aryil.chatbot.common.dto.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@Tag(name = "Chat", description = "Chat conversations and messaging")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    @Operation(summary = "Send chat message", description = "Send a message to the chatbot and receive a response. Creates a new conversation if conversationId is null.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chat response received", content = @Content(schema = @Schema(implementation = ChatResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or content blocked", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Access denied to conversation", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ChatResponse chat(@Valid @RequestBody ChatRequest request, Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return chatService.chat(userId, request);
    }

    @GetMapping("/conversations/{id}/messages")
    @Operation(summary = "Get conversation messages", description = "Retrieve all messages from a specific conversation", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Messages retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Access denied to conversation", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public List<MessageDto> messages(@PathVariable("id") UUID conversationId, Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return chatService.getMessages(userId, conversationId);
    }

}

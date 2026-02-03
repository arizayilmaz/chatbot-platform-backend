package com.aryil.chatbot.chat;

import com.aryil.chatbot.chat.dto.ChatRequest;
import com.aryil.chatbot.chat.dto.ChatResponse;
import com.aryil.chatbot.chat.dto.MessageDto;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request, Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal(); // JwtAuthFilter principal = userId
        return chatService.chat(userId, request);
    }

    @GetMapping("/conversations/{id}/messages")
    public List<MessageDto> messages(@PathVariable("id") UUID conversationId, Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return chatService.getMessages(userId, conversationId);
    }

}

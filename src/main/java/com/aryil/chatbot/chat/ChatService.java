package com.aryil.chatbot.chat;

import com.aryil.chatbot.chat.dto.ChatRequest;
import com.aryil.chatbot.chat.dto.ChatResponse;
import com.aryil.chatbot.chat.dto.MessageDto;
import com.aryil.chatbot.common.exception.ConversationAccessException;
import com.aryil.chatbot.events.OutboxEvent;
import com.aryil.chatbot.events.OutboxEventRepository;
import com.aryil.chatbot.guard.ContentGuardService;
import com.aryil.chatbot.llm.OllamaClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ContentGuardService guardService;
    private final OllamaClient ollamaClient;
    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public ChatService(ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            ContentGuardService guardService,
            OllamaClient ollamaClient,
            OutboxEventRepository outboxRepository,
            ObjectMapper objectMapper) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.guardService = guardService;
        this.ollamaClient = ollamaClient;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ChatResponse chat(UUID userId, ChatRequest req) {
        Conversation conv = getOrCreateConversation(userId, req.conversationId());

        // Input guard
        var guard = guardService.checkUserMessage(req.message());

        Message userMsg = Message.builder()
                .conversationId(conv.getId())
                .role("USER")
                .content(req.message())
                .blocked(!guard.allowed())
                .blockedReason(guard.reason())
                .build();
        messageRepository.save(userMsg);

        publishEvent("MESSAGE_RECEIVED", userId, conv.getId(), userMsg.getId(),
                userMsg.isBlocked(), userMsg.getBlockedReason());

        if (!guard.allowed()) {
            publishEvent("MESSAGE_BLOCKED", userId, conv.getId(), userMsg.getId(),
                    true, userMsg.getBlockedReason());

            return new ChatResponse(
                    conv.getId(),
                    "Bu mesaj içerik kuralları nedeniyle işlenemedi. Lütfen yeniden ifade et.",
                    true,
                    guard.reason());
        }

        // LLM
        var context = buildContextMessages(conv.getId(), req.message());
        String reply = ollamaClient.generate(systemPrompt(), context);

        // Output guard
        var outGuard = guardService.checkAssistantMessage(reply);
        if (!outGuard.allowed()) {
            reply = "Bu isteğe yardımcı olamam. İstersen güvenli bir şekilde yeniden ifade edebilirsin.";
        }

        Message assistantMsg = Message.builder()
                .conversationId(conv.getId())
                .role("ASSISTANT")
                .content(reply)
                .blocked(!outGuard.allowed())
                .blockedReason(outGuard.reason())
                .build();
        messageRepository.save(assistantMsg);

        publishEvent("REPLY_GENERATED", userId, conv.getId(), assistantMsg.getId(),
                assistantMsg.isBlocked(), assistantMsg.getBlockedReason());

        // Return the blocked response if necessary
        return new ChatResponse(conv.getId(), reply, assistantMsg.isBlocked(), assistantMsg.getBlockedReason());
    }

    private void publishEvent(String eventType, UUID userId, UUID conversationId, UUID messageId, boolean blocked,
            String blockedReason) {
        try {
            var eventData = java.util.Map.of(
                    "eventType", eventType,
                    "userId", userId.toString(),
                    "conversationId", conversationId.toString(),
                    "messageId", messageId.toString(),
                    "blocked", blocked,
                    "blockedReason", blockedReason != null ? blockedReason : "",
                    "timestamp", java.time.Instant.now().toString());

            String payloadJson = objectMapper.writeValueAsString(eventData);

            OutboxEvent event = OutboxEvent.builder()
                    .aggregateType("CONVERSATION")
                    .aggregateId(conversationId)
                    .eventType(eventType)
                    .payloadJson(payloadJson)
                    .build();

            outboxRepository.save(event);
        } catch (JsonProcessingException e) {
            System.out.println("⚠️ Failed to serialize event: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<MessageDto> getMessages(UUID userId, UUID conversationId) {
        if (!conversationRepository.existsByIdAndUserId(conversationId, userId)) {
            throw new ConversationAccessException("Not your conversation");
        }
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .map(m -> new MessageDto(
                        m.getId(), m.getRole(), m.getContent(), m.getCreatedAt(), m.isBlocked(), m.getBlockedReason()))
                .toList();
    }

    private Conversation getOrCreateConversation(UUID userId, UUID conversationId) {
        if (conversationId != null) {
            if (!conversationRepository.existsByIdAndUserId(conversationId, userId)) {
                throw new ConversationAccessException("Not your conversation");
            }
            return conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new IllegalArgumentException("Conversation not found: " + conversationId));
        }

        Conversation c = new Conversation();
        c.setUserId(userId);
        c.setTitle("New chat");
        return conversationRepository.save(c);
    }

    private List<java.util.Map<String, String>> buildContextMessages(UUID conversationId, String newUserMessage) {
        var history = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);

        int start = Math.max(0, history.size() - 10);

        var msgs = new java.util.ArrayList<java.util.Map<String, String>>();
        for (int i = start; i < history.size(); i++) {
            Message m = history.get(i);
            if (m.isBlocked())
                continue;

            String role = "USER".equalsIgnoreCase(m.getRole()) ? "user" : "assistant";
            msgs.add(java.util.Map.of("role", role, "content", m.getContent()));
        }

        return msgs;
    }

    private String systemPrompt() {
        return """
                Sen çok amaçlı bir asistansın.
                Yasak içerik üretme veya tekrarlama.
                Yasak bir içerik istenirse kısa bir şekilde reddet ve güvenli alternatif öner.
                Cevapların net ve yardımcı olsun.
                """;
    }
}

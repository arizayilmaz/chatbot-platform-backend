package com.aryil.chatbot.chat;

import com.aryil.chatbot.chat.dto.ChatRequest;
import com.aryil.chatbot.chat.dto.ChatResponse;
import com.aryil.chatbot.chat.dto.MessageDto;
import com.aryil.chatbot.events.ChatEvent;
import com.aryil.chatbot.events.EventPublisher;
import com.aryil.chatbot.guard.ContentGuardService;
import com.aryil.chatbot.llm.OllamaClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ContentGuardService guardService;
    private final OllamaClient ollamaClient;
    private final EventPublisher eventPublisher;

    public ChatService(ConversationRepository conversationRepository,
                       MessageRepository messageRepository,
                       ContentGuardService guardService,
                       OllamaClient ollamaClient,
                       EventPublisher eventPublisher) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.guardService = guardService;
        this.ollamaClient = ollamaClient;
        this.eventPublisher = eventPublisher;
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

        safePublish("chat.message.received",
                new ChatEvent(
                        "MESSAGE_RECEIVED",
                        userId,
                        conv.getId(),
                        userMsg.getId(),
                        userMsg.isBlocked(),
                        userMsg.getBlockedReason(),
                        Instant.now()
                )
        );

        if (!guard.allowed()) {
            safePublish("chat.message.blocked",
                    new ChatEvent(
                            "MESSAGE_BLOCKED",
                            userId,
                            conv.getId(),
                            userMsg.getId(),
                            true,
                            userMsg.getBlockedReason(),
                            Instant.now()
                    )
            );

            return new ChatResponse(
                    conv.getId(),
                    "Bu mesaj içerik kuralları nedeniyle işlenemedi. Lütfen yeniden ifade et.",
                    true,
                    guard.reason()
            );
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

        safePublish("chat.reply.generated",
                new ChatEvent(
                        "REPLY_GENERATED",
                        userId,
                        conv.getId(),
                        assistantMsg.getId(),
                        assistantMsg.isBlocked(),
                        assistantMsg.getBlockedReason(),
                        Instant.now()
                )
        );

        return new ChatResponse(conv.getId(), reply, false, null);
    }

    private void safePublish(String routingKey, ChatEvent event) {
        try {
            eventPublisher.publish(routingKey, event);
        } catch (Exception e) {
            // Event sistemi asla chat akışını bozmasın
            System.out.println("⚠️ event publish failed: " + routingKey + " err=" + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<MessageDto> getMessages(UUID userId, UUID conversationId) {
        if (!conversationRepository.existsByIdAndUserId(conversationId, userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Not your conversation");
        }
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .map(m -> new MessageDto(
                        m.getId(), m.getRole(), m.getContent(), m.getCreatedAt(), m.isBlocked(), m.getBlockedReason()
                ))
                .toList();
    }

    private Conversation getOrCreateConversation(UUID userId, UUID conversationId) {
        if (conversationId != null) {
            if (!conversationRepository.existsByIdAndUserId(conversationId, userId)) {
                throw new org.springframework.security.access.AccessDeniedException("Not your conversation");
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
            if (m.isBlocked()) continue;

            String role = "USER".equalsIgnoreCase(m.getRole()) ? "user" : "assistant";
            msgs.add(java.util.Map.of("role", role, "content", m.getContent()));
        }

        msgs.add(java.util.Map.of("role", "user", "content", newUserMessage));
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

package com.aryil.chatbot.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    List<Conversation> findByUserIdOrderByCreatedAtDesc(UUID userId);
    boolean existsByIdAndUserId(UUID id, UUID userId);

}

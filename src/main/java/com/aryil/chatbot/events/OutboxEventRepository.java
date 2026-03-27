package com.aryil.chatbot.events;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Query("""
            SELECT e FROM OutboxEvent e
            WHERE e.status = 'PENDING'
            AND (e.nextRetryAt IS NULL OR e.nextRetryAt <= :now)
            ORDER BY e.createdAt ASC
            """)
    List<OutboxEvent> findPendingEvents(OffsetDateTime now);
}

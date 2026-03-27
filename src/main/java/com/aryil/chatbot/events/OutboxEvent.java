package com.aryil.chatbot.events;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "aggregate_type", nullable = false, length = 64)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @Column(name = "payload_json", nullable = false, columnDefinition = "TEXT")
    private String payloadJson;

    @Column(name = "status", nullable = false, length = 32)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "attempts", nullable = false)
    @Builder.Default
    private Integer attempts = 0;

    @Column(name = "next_retry_at")
    private OffsetDateTime nextRetryAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "sent_at")
    private OffsetDateTime sentAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    public void markAsSent() {
        this.status = "SENT";
        this.sentAt = OffsetDateTime.now();
    }

    public void markAsFailed(String error) {
        this.status = "FAILED";
        this.lastError = error;
    }

    public void incrementAttempts() {
        this.attempts++;
    }

    public void scheduleRetry(int delaySeconds) {
        this.status = "PENDING";
        this.nextRetryAt = OffsetDateTime.now().plusSeconds(delaySeconds);
    }
}

package com.aryil.chatbot.policy;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "banned_patterns")
public class BannedPattern {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 500)
    private String pattern;

    @Column(nullable = false, length = 20)
    private String type; // KEYWORD / REGEX

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false, length = 10)
    private String severity; // LOW / MEDIUM / HIGH

    @Column(length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }
}

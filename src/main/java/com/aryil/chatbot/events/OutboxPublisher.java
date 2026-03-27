package com.aryil.chatbot.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxEventRepository repository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.events.exchange:chat.events}")
    private String exchange;

    @Value("${app.outbox.batch-size:50}")
    private int batchSize;

    @Value("${app.outbox.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.outbox.initial-delay-seconds:10}")
    private int initialDelaySeconds;

    public OutboxPublisher(OutboxEventRepository repository,
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:5000}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pending = repository.findPendingEvents(OffsetDateTime.now())
                .stream()
                .limit(batchSize)
                .toList();

        if (pending.isEmpty()) {
            return;
        }

        log.info("Publishing {} pending outbox events", pending.size());

        for (OutboxEvent event : pending) {
            try {
                Object payload = objectMapper.readValue(event.getPayloadJson(), Object.class);
                String routingKey = determineRoutingKey(event.getEventType());

                rabbitTemplate.convertAndSend(exchange, routingKey, payload);

                event.markAsSent();
                repository.save(event);

                log.debug("Published event {} of type {}", event.getId(), event.getEventType());

            } catch (Exception e) {
                handlePublishFailure(event, e);
            }
        }
    }

    private void handlePublishFailure(OutboxEvent event, Exception e) {
        event.incrementAttempts();

        if (event.getAttempts() >= maxAttempts) {
            event.markAsFailed(e.getMessage());
            log.error("Event {} failed after {} attempts: {}", event.getId(), maxAttempts, e.getMessage());
        } else {
            int delaySeconds = calculateBackoff(event.getAttempts());
            event.scheduleRetry(delaySeconds);
            log.warn("Event {} failed (attempt {}), retrying in {} seconds",
                    event.getId(), event.getAttempts(), delaySeconds);
        }

        repository.save(event);
    }

    private int calculateBackoff(int attempts) {
        return initialDelaySeconds * (int) Math.pow(2, attempts - 1);
    }

    private String determineRoutingKey(String eventType) {
        return switch (eventType) {
            case "MESSAGE_RECEIVED" -> "chat.message.received";
            case "REPLY_GENERATED" -> "chat.reply.generated";
            case "MESSAGE_BLOCKED" -> "chat.message.blocked";
            default -> "chat.event";
        };
    }
}

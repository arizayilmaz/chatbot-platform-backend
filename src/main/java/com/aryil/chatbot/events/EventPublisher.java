package com.aryil.chatbot.events;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;

    public EventPublisher(RabbitTemplate rabbitTemplate,
                          @Value("${app.events.exchange:chat.events}") String exchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
    }

    public void publish(String routingKey, ChatEvent event) {
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}

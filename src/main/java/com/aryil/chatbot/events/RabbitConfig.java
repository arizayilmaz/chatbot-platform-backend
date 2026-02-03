package com.aryil.chatbot.events;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    TopicExchange chatExchange(@Value("${app.events.exchange:chat.events}") String exchange) {
        return new TopicExchange(exchange);
    }

    @Bean
    Queue auditQueue(@Value("${app.events.queue:chat.audit}") String queue) {
        return QueueBuilder.durable(queue).build();
    }

    @Bean
    Binding bindAudit(Queue auditQueue, TopicExchange chatExchange) {
        return BindingBuilder.bind(auditQueue).to(chatExchange).with("chat.#");
    }
}

package com.aryil.chatbot.llm;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.llm")
public class LlmProperties {
    private String baseUrl;
    private String model;
    private int timeoutSeconds = 60;
}

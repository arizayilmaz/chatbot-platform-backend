package com.aryil.chatbot.llm;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class OllamaClient {

    private final RestClient restClient;
    private final LlmProperties props;

    public OllamaClient(LlmProperties props) {
        this.props = props;
        this.restClient = RestClient.builder()
                .baseUrl(props.getBaseUrl())
                .build();
    }

    @SuppressWarnings("unchecked")
    public String generate(String systemPrompt, List<Map<String, String>> messages) {
        // Ollama chat endpoint formatı: /api/chat
        Map<String, Object> body = Map.of(
                "model", props.getModel(),
                "stream", false,
                "messages", buildMessages(systemPrompt, messages)
        );

        Map<String, Object> res = restClient.post()
                .uri("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        // response.message.content
        Map<String, Object> message = (Map<String, Object>) res.get("message");
        return message == null ? "" : String.valueOf(message.get("content"));
    }

    private List<Map<String, String>> buildMessages(String systemPrompt, List<Map<String, String>> messages) {
        // system + geçmiş + user
        var system = Map.of("role", "system", "content", systemPrompt);
        return new java.util.ArrayList<>() {{
            add(system);
            addAll(messages);
        }};
    }
}

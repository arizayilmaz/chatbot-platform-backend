package com.aryil.chatbot.common;

import com.aryil.chatbot.auth.JwtProperties;
import com.aryil.chatbot.llm.LlmProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({LlmProperties.class, JwtProperties.class})
public class AppConfig {}

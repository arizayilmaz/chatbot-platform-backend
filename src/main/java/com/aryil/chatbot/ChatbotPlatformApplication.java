package com.aryil.chatbot;

import com.aryil.chatbot.common.DotenvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ChatbotPlatformApplication {

        public static void main(String[] args) {
                DotenvLoader.load();
                SpringApplication.run(ChatbotPlatformApplication.class, args);
        }

}

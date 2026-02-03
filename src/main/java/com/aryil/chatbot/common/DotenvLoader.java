package com.aryil.chatbot.common;

import io.github.cdimascio.dotenv.Dotenv;

public final class DotenvLoader {
    private DotenvLoader() {}

    public static void load() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(e -> {
            if (System.getenv(e.getKey()) == null && System.getProperty(e.getKey()) == null) {
                System.setProperty(e.getKey(), e.getValue());
            }
        });
    }
}

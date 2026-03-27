package com.aryil.chatbot.common.exception;

public class ContentBlockedException extends RuntimeException {
    private final String reason;

    public ContentBlockedException(String message, String reason) {
        super(message);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}

package com.aryil.chatbot.guard;

public record GuardResult(
        boolean allowed,
        boolean flagged,
        String reason) {
    public static GuardResult allow() {
        return new GuardResult(true, false, null);
    }

    public static GuardResult block(String reason) {
        return new GuardResult(false, false, reason);
    }

    public static GuardResult flag(String reason) {
        return new GuardResult(true, true, reason);
    }
}

package com.aryil.chatbot.guard;

public record GuardResult(
        boolean allowed,
        String reason
) {
    public static GuardResult allow() { return new GuardResult(true, null); }
    public static GuardResult block(String reason) { return new GuardResult(false, reason); }
}

import { useState } from "react";
import type { LocalSession } from "@/api/types";

export function useSessions() {
    const [sessions, setSessions] = useState<LocalSession[]>(() => {
        const saved = localStorage.getItem("cb_sessions");
        if (!saved) {
            return [];
        }

        try {
            return JSON.parse(saved) as LocalSession[];
        } catch (e) {
            console.error("Failed to parse sessions", e);
            localStorage.removeItem("cb_sessions");
            return [];
        }
    });

    const saveSessions = (updated: LocalSession[]) => {
        const sorted = [...updated].sort(
            (a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()
        );
        setSessions(sorted);
        localStorage.setItem("cb_sessions", JSON.stringify(sorted));
    };

    const addSession = (conversationId: string, firstMessage: string) => {
        const existing = sessions.find((s) => s.conversationId === conversationId);
        if (existing) return;

        const title = firstMessage.slice(0, 40) + (firstMessage.length > 40 ? "..." : "");
        const newSession: LocalSession = {
            conversationId,
            title,
            updatedAt: new Date().toISOString(),
        };
        saveSessions([newSession, ...sessions]);
    };

    const updateSessionTime = (conversationId: string) => {
        const updated = sessions.map((s) =>
            s.conversationId === conversationId
                ? { ...s, updatedAt: new Date().toISOString() }
                : s
        );
        saveSessions(updated);
    };

    const renameSession = (conversationId: string, newTitle: string) => {
        const updated = sessions.map((s) =>
            s.conversationId === conversationId ? { ...s, title: newTitle } : s
        );
        saveSessions(updated);
    };

    const deleteSession = (conversationId: string) => {
        const updated = sessions.filter((s) => s.conversationId !== conversationId);
        saveSessions(updated);
    };

    return {
        sessions,
        addSession,
        updateSessionTime,
        renameSession,
        deleteSession,
    };
}

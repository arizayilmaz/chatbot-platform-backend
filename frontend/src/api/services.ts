import { apiFetch } from "./api-client";
import type {
    ChatRequest,
    ChatResponse,
    MessageDto,
    PatternDto,
    CreatePatternRequest
} from "./types";

export const chatApi = {
    sendMessage: (conversationId: string | null, message: string) =>
        apiFetch<ChatResponse>("/api/chat", {
            method: "POST",
            body: JSON.stringify({ conversationId, message } as ChatRequest),
        }),

    getMessages: (conversationId: string) =>
        apiFetch<MessageDto[]>(`/api/conversations/${conversationId}/messages`),
};

export const adminApi = {
    getPatterns: () => apiFetch<PatternDto[]>("/api/admin/patterns"),

    createPattern: (req: CreatePatternRequest) =>
        apiFetch<PatternDto>("/api/admin/patterns", {
            method: "POST",
            body: JSON.stringify(req),
        }),

    deletePattern: (id: string) =>
        apiFetch<void>(`/api/admin/patterns/${id}`, {
            method: "DELETE",
        }),

    togglePattern: (id: string, enabled: boolean) =>
        apiFetch<PatternDto>(`/api/admin/patterns/${id}/toggle?enabled=${enabled}`, {
            method: "PUT",
        }),
};

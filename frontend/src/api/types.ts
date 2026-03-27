// ── Auth ──
export interface AuthResponse {
    token: string;
}

export interface LoginRequest {
    email: string;
    password: string;
}

export interface RegisterRequest {
    email: string;
    password: string;
}

// ── Chat ──
export interface ChatRequest {
    conversationId: string | null;
    message: string;
}

export interface ChatResponse {
    conversationId: string;
    reply: string;
    blocked: boolean;
    reason?: string | null;        // backend field name
    blockedReason?: string | null;  // handle alternative name
}

export interface MessageDto {
    id: string;
    role: "USER" | "ASSISTANT";
    content: string;
    createdAt: string;
    blocked: boolean;
    blockedReason?: string | null;
}

// ── Admin Patterns ──
export interface PatternDto {
    id: string;
    pattern: string;
    type: "KEYWORD" | "REGEX";
    enabled: boolean;
    severity: "LOW" | "MEDIUM" | "HIGH";
    notes?: string | null;
    createdAt: string;
}

export interface CreatePatternRequest {
    pattern: string;
    type: "KEYWORD" | "REGEX";
    severity: "LOW" | "MEDIUM" | "HIGH";
    notes?: string;
}

// ── Error ──
export interface ApiError {
    timestamp: string;
    path: string;
    code: string;
    message: string;
    details?: Record<string, unknown> | null;
}

// ── JWT ──
export interface JwtPayload {
    sub: string;
    role: "USER" | "ADMIN";
    iat: number;
    exp: number;
}

// ── Local Session ──
export interface LocalSession {
    conversationId: string;
    title: string;
    updatedAt: string;
}

-- UUID
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- USERS
CREATE TABLE IF NOT EXISTS users (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
    );

-- CONVERSATIONS
CREATE TABLE IF NOT EXISTS conversations (
                                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(200),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
    );

-- MESSAGES
CREATE TABLE IF NOT EXISTS messages (
                                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL, -- USER / ASSISTANT
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    blocked BOOLEAN NOT NULL DEFAULT false,
    blocked_reason VARCHAR(120)
    );

-- BANNED PATTERNS
CREATE TABLE IF NOT EXISTS banned_patterns (
                                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pattern VARCHAR(500) NOT NULL,
    type VARCHAR(20) NOT NULL, -- KEYWORD / REGEX
    enabled BOOLEAN NOT NULL DEFAULT true,
    severity VARCHAR(10) NOT NULL DEFAULT 'MEDIUM', -- LOW / MEDIUM / HIGH
    notes VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS idx_conversations_user_id ON conversations(user_id);
CREATE INDEX IF NOT EXISTS idx_messages_conversation_id ON messages(conversation_id);
CREATE INDEX IF NOT EXISTS idx_banned_patterns_enabled ON banned_patterns(enabled);
